/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.data.CsvRepository;
import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.model.Item;
import carwash.model.Product;
import carwash.model.Role;
import carwash.model.Service;
import carwash.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author argor
 */
public class CatalogService {

    private final CsvRepository repo;
    private final ArrayList<Item> katalog = new ArrayList<>();

    public CatalogService(CsvRepository repo, List<Item> awal) {
        this.repo = repo;
        katalog.addAll(awal);
    }

    // ---------- baca (semua peran) ----------

    public List<Item> getDaftar() {
        return Collections.unmodifiableList(katalog);
    }

    public Item cariById(String id) {
        for (Item it : katalog) {
            if (it.getId().equals(id)) return it;
        }
        return null;
    }

    // ---------- ubah (hanya Kasir) ----------

    public Service tambahService(String nama, String harga, boolean isCuci, User user)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {
        periksaKasir(user);
        String n = Validator.validasiNamaItem(nama);
        double h = Validator.validasiHarga(harga);

        List<Item> lama = salinan();
        Service baru = new Service(buatId("S"), n, h, isCuci);
        katalog.add(baru);
        simpanAtauRollback(lama);
        return baru;
    }

    public Product tambahProduct(String nama, String harga, String stok, User user)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {
        periksaKasir(user);
        String n = Validator.validasiNamaItem(nama);
        double h = Validator.validasiHarga(harga);
        int s = Validator.validasiStok(stok);

        List<Item> lama = salinan();
        Product baru = new Product(buatId("P"), n, h, s);
        katalog.add(baru);
        simpanAtauRollback(lama);
        return baru;
    }

    /** Ubah nama dan harga. Jika item adalah Product, stok ikut diubah (koreksi stok). */
    public void ubahItem(String id, String nama, String harga, String stok, User user)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {
        periksaKasir(user);
        Item it = temukan(id);
        String n = Validator.validasiNamaItem(nama);
        double h = Validator.validasiHarga(harga);
        Integer stokBaru = (it instanceof Product) ? Validator.validasiStok(stok) : null;

        List<Item> lama = salinan();
        it.setNama(n);
        it.setHarga(h);
        if (it instanceof Product p) p.setStok(stokBaru);
        simpanAtauRollback(lama);
    }

    public void restock(String id, String jumlah, User user)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {
        periksaKasir(user);
        Item it = temukan(id);
        if (!(it instanceof Product p)) {
            throw new InputTidakValidException("Item bukan produk berstok");
        }
        int j = Validator.validasiRestock(jumlah);

        List<Item> lama = salinan();
        p.tambahStok(j);
        simpanAtauRollback(lama);
    }

    public void hapus(String id, User user)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {
        periksaKasir(user);
        Item it = temukan(id);

        List<Item> lama = salinan();
        katalog.remove(it);
        simpanAtauRollback(lama);
    }

    // ---------- bantu ----------

    private void periksaKasir(User user) throws AksesDitolakException {
        if (user == null || user.getRole() != Role.KASIR) {
            throw new AksesDitolakException("Hanya Kasir yang boleh mengubah katalog");
        }
    }

    private Item temukan(String id) throws InputTidakValidException {
        Item it = cariById(id);
        if (it == null) throw new InputTidakValidException("Item tidak ditemukan");
        return it;
    }

    private List<Item> salinan() {
        List<Item> s = new ArrayList<>();
        for (Item it : katalog) s.add(it.salin());
        return s;
    }

    private void simpanAtauRollback(List<Item> lama) throws PenyimpananGagalException {
        try {
            repo.simpanKatalog(katalog);
        } catch (IOException e) {
            katalog.clear();
            katalog.addAll(lama);               // rollback
            throw new PenyimpananGagalException(
                    "Gagal menyimpan katalog, perubahan dibatalkan", e);
        }
    }

    /** S01, S02, ... atau P01, P02, ... berdasarkan nomor terbesar yang ada. */
    private String buatId(String prefix) {
        int max = 0;
        for (Item it : katalog) {
            if (it.getId().startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(it.getId().substring(1)));
                } catch (NumberFormatException e) {
                    // id tidak standar, abaikan
                }
            }
        }
        return String.format("%s%02d", prefix, max + 1);
    }
}