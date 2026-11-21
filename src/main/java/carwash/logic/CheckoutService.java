/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.data.CsvRepository;
import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.exception.StokTidakCukupException;
import carwash.exception.TotalBerubahException;
import carwash.model.CartItem;
import carwash.model.Customer;
import carwash.model.Item;
import carwash.model.Product;
import carwash.model.Role;
import carwash.model.Transaction;
import carwash.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author argor
 */
public class CheckoutService {

    private final CatalogService catalog;
    private final LoyaltyTracker loyalty;
    private final CsvRepository repo;
    private final ArrayList<Transaction> riwayat = new ArrayList<>();

    public CheckoutService(CatalogService catalog, LoyaltyTracker loyalty,
                           CsvRepository repo, List<Transaction> riwayatAwal) {
        this.catalog = catalog;
        this.loyalty = loyalty;
        this.repo = repo;
        this.riwayat.addAll(riwayatAwal);
    }

    public List<Transaction> getRiwayat() {
        return Collections.unmodifiableList(riwayat);
    }

    /** Dipanggil form setiap keranjang berubah. Tidak mengubah data. */
    public LoyaltyResult previewCheckout(Customer customer, Cart cart) {
        return previewCheckout(customer, cart, LocalDate.now());
    }

    public LoyaltyResult previewCheckout(Customer customer, Cart cart, LocalDate tanggal) {
        return loyalty.preview(customer.getId(), cart.getIsi(), tanggal);
    }

    public Transaction checkout(User kasir, Customer customer, Cart cart,
                                double uangDiterima, double totalDitampilkan)
            throws AksesDitolakException, InputTidakValidException, StokTidakCukupException,
                   TotalBerubahException, PenyimpananGagalException {
        return checkout(kasir, customer, cart, uangDiterima, totalDitampilkan, LocalDate.now());
    }

    /** Versi dengan tanggal eksplisit, supaya mudah dites. */
    public Transaction checkout(User kasir, Customer customer, Cart cart,
                                double uangDiterima, double totalDitampilkan, LocalDate tanggal)
            throws AksesDitolakException, InputTidakValidException, StokTidakCukupException,
                   TotalBerubahException, PenyimpananGagalException {

        // 0. hak akses
        if (kasir == null || kasir.getRole() != Role.KASIR) {
            throw new AksesDitolakException("Hanya Kasir yang boleh melakukan transaksi");
        }

        // 2. validasi (gagal -> belum ada data yang berubah)
        if (customer == null) throw new InputTidakValidException("Pelanggan belum dipilih");
        if (cart.isKosong()) throw new InputTidakValidException("Keranjang tidak boleh kosong");
        if (uangDiterima < 0) throw new InputTidakValidException("Uang diterima tidak boleh negatif");
        Map<String, Product> produk = produkHidup(cart);

        // 3-5. hitung ulang, bandingkan dengan total di layar, cek uang
        LoyaltyResult hasil = loyalty.preview(customer.getId(), cart.getIsi(), tanggal);
        if (hasil.getTotal() != totalDitampilkan) {
            throw new TotalBerubahException("Total berubah, periksa ulang");
        }
        if (uangDiterima < hasil.getTotal()) {
            throw new InputTidakValidException("Uang diterima kurang dari total");
        }

        // 6. snapshot nilai lama
        Map<String, Integer> hitunganLama = loyalty.salinan();
        Map<String, Integer> stokLama = new HashMap<>();
        for (Product p : produk.values()) stokLama.put(p.getId(), p.getStok());

        // 7. bangun Transaction dari baris tampil
        Transaction tx = new Transaction(idBaru(), tanggal, customer.getId(), kasir.getId(),
                hasil.getBarisTampil(), hasil.getTotal(), hasil.getJumlahGratis(),
                uangDiterima, uangDiterima - hasil.getTotal());

        try {
            // 8. kurangi stok
            for (CartItem ci : cart.getIsi()) {
                Product p = produk.get(ci.getItem().getId());
                if (p != null) p.kurangiStok(ci.getQty());
            }
            // 9. commit hitungan
            loyalty.commit(customer.getId(), hasil);
            // 10. tambah riwayat, lalu tulis semua file
            riwayat.add(tx);
            tulisSemua();
        } catch (IOException e) {
            // 11. rollback
            pulihkan(tx, hitunganLama, produk, stokLama);
            try { tulisSemua(); } catch (IOException ignored) { /* upaya terbaik: samakan file dengan memori */ }
            throw new PenyimpananGagalException("Gagal menyimpan transaksi, perubahan dibatalkan", e);
        } catch (RuntimeException | StokTidakCukupException e) {
            pulihkan(tx, hitunganLama, produk, stokLama);
            throw e;
        }

        // 12. keranjang dikosongkan, transaksi dikembalikan untuk struk
        cart.kosongkan();
        return tx;
    }

    // ---- bantu ----

    /** Ambil Product "hidup" dari katalog dan pastikan stoknya masih cukup. */
    private Map<String, Product> produkHidup(Cart cart)
            throws InputTidakValidException, StokTidakCukupException {
        Map<String, Product> hasil = new HashMap<>();
        for (CartItem ci : cart.getIsi()) {
            if (ci.getItem() instanceof Product) {
                Item live = catalog.cariById(ci.getItem().getId());
                if (!(live instanceof Product p)) {
                    throw new InputTidakValidException(
                            "Item " + ci.getItem().getNama() + " sudah tidak ada di katalog");
                }
                if (!p.cekStok(ci.getQty())) {
                    throw new StokTidakCukupException(
                            "Stok " + p.getNama() + " tidak cukup (tersisa " + p.getStok() + ")");
                }
                hasil.put(p.getId(), p);
            }
        }
        return hasil;
    }

    private void tulisSemua() throws IOException {
        repo.simpanTransaksi(riwayat);
        loyalty.simpan();
        catalog.simpan();
    }

    private void pulihkan(Transaction tx, Map<String, Integer> hitunganLama,
                          Map<String, Product> produk, Map<String, Integer> stokLama) {
        loyalty.gantiSemua(hitunganLama);
        for (Product p : produk.values()) p.setStok(stokLama.get(p.getId()));
        riwayat.remove(tx);
    }

    /** T0001, T0002, ... berdasarkan nomor terbesar yang ada. */
    private String idBaru() {
        int max = 0;
        for (Transaction t : riwayat) {
            try {
                max = Math.max(max, Integer.parseInt(t.getId().substring(1)));
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                // id tidak standar, abaikan
            }
        }
        return String.format("T%04d", max + 1);
    }
}
