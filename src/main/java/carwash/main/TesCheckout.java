/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.main;

import carwash.data.CsvRepository;
import carwash.exception.*;
import carwash.logic.*;
import carwash.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author argor
 */
public class TesCheckout {

    /** Repo yang tidak menulis file apa pun. */
    static class RepoPalsu extends CsvRepository {
        @Override public void simpanKatalog(Collection<Item> d) throws IOException { }
        @Override public void simpanLoyalty(Map<String, Integer> d) throws IOException { }
        @Override public void simpanTransaksi(List<Transaction> d) throws IOException { }
    }

    /** Repo yang selalu gagal menulis transaksi. */
    static class RepoGagal extends RepoPalsu {
        @Override public void simpanTransaksi(List<Transaction> d) throws IOException {
            throw new IOException("simulasi disk penuh");
        }
    }

    interface Aksi { void jalankan() throws Exception; }

    static final LocalDate RABU   = LocalDate.of(2026, 11, 18);
    static final LocalDate MINGGU = LocalDate.of(2026, 11, 22);

    public static void main(String[] args) throws Exception {
        Service reguler = new Service("S01", "Cuci Reguler", 50000, true);
        Service poles   = new Service("S03", "Poles", 100000, false);
        Product pewangi = new Product("P01", "Pewangi", 15000, 5);
        Customer budi = new Customer("C001", "Budi Santoso", "081234567890");
        User kasir   = new User("U002", "kasir", "x", Role.KASIR, "Budi Kasir");
        User manager = new User("U001", "manager", "x", Role.MANAGER, "Pak Hendra");

        CsvRepository palsu = new RepoPalsu();
        CatalogService catalog = new CatalogService(palsu, List.of(reguler, poles, pewangi));
        LoyaltyTracker loyalty = new LoyaltyTracker(palsu, new HolidayCalendar(List.of()), Map.of("C001", 8));
        CheckoutService co = new CheckoutService(catalog, loyalty, palsu, List.of());
        Cart cart = new Cart();

        // ---- T1: kasus C + produk, bayar sukses ----
        cart.tambah(reguler, 4);
        cart.tambah(pewangi, 2);
        System.out.println("Subtotal keranjang: " + (long) cart.subtotal());
        LoyaltyResult pv = co.previewCheckout(budi, cart, RABU);
        System.out.println("Preview: total " + (long) pv.getTotal() + " | potongan "
                + (long) pv.getPotongan() + " | hitungan " + pv.getHitunganAwal()
                + " -> " + pv.getHitunganAkhir());
        Transaction t1 = co.checkout(kasir, budi, cart, 200000, pv.getTotal(), RABU);
        cetak(t1);
        cek("T1 hitungan 1, stok 3, riwayat 1, keranjang kosong",
                loyalty.getHitungan("C001") == 1 && pewangi.getStok() == 3
                && co.getRiwayat().size() == 1 && cart.isKosong());

        // ---- T2-T5: penolakan, data tidak boleh berubah ----
        cart.tambah(reguler, 1);
        tolak("T2 Manager checkout", AksesDitolakException.class,
                () -> co.checkout(manager, budi, cart, 50000, 50000, RABU));
        tolak("T3 Keranjang kosong", InputTidakValidException.class,
                () -> co.checkout(kasir, budi, new Cart(), 50000, 0, RABU));
        tolak("T4 Uang kurang", InputTidakValidException.class,
                () -> co.checkout(kasir, budi, cart, 40000, 50000, RABU));
        tolak("T5 Total berubah", TotalBerubahException.class,
                () -> co.checkout(kasir, budi, cart, 50000, 45000, RABU));
        cek("T2-T5 data tidak berubah",
                loyalty.getHitungan("C001") == 1 && pewangi.getStok() == 3
                && co.getRiwayat().size() == 1 && !cart.isKosong());

        // ---- T6: aturan Cart ----
        Cart c2 = new Cart();
        c2.tambah(pewangi, 2);
        tolak("T6a Stok akumulatif", StokTidakCukupException.class, () -> c2.tambah(pewangi, 2));
        tolak("T6b Qty 0", InputTidakValidException.class, () -> c2.tambah(pewangi, 0));

        // ---- T7: simpan gagal -> rollback ----
        cart.tambah(pewangi, 1);
        CheckoutService coGagal = new CheckoutService(catalog, loyalty, new RepoGagal(), co.getRiwayat());
        tolak("T7 Simpan gagal", PenyimpananGagalException.class,
                () -> coGagal.checkout(kasir, budi, cart, 100000, 65000, RABU));
        cek("T7 rollback: hitungan 1, stok 3, riwayat 1, keranjang tetap ada",
                loyalty.getHitungan("C001") == 1 && pewangi.getStok() == 3
                && coGagal.getRiwayat().size() == 1 && !cart.isKosong());

        // ---- T8: hari Minggu, hitungan tidak berubah ----
        Transaction t8 = co.checkout(kasir, budi, cart, 70000, 65000, MINGGU);
        cetak(t8);
        cek("T8 Minggu: hitungan tetap 1, stok 2, riwayat 2, keranjang kosong",
                loyalty.getHitungan("C001") == 1 && pewangi.getStok() == 2
                && co.getRiwayat().size() == 2 && cart.isKosong());

        // ---- T9: simpan dan muat ulang dengan repo ASLI (menulis file di data/) ----
        CsvRepository asli = new CsvRepository();
        asli.simpanTransaksi(co.getRiwayat());
        List<Transaction> muat = asli.muatTransaksi();
        cek("T9 muat ulang: 2 transaksi, T0001 punya 4 baris, kembalian T0002 = 5000",
                muat.size() == 2 && muat.get(0).getBaris().size() == 4
                && muat.get(1).getKembalian() == 5000);
    }

    static void cetak(Transaction t) {
        System.out.println("Transaksi " + t.getId() + " | total " + (long) t.getTotal()
                + " | reward " + t.getJumlahReward() + " | uang " + (long) t.getUangDiterima()
                + " | kembalian " + (long) t.getKembalian());
        for (TransactionLine l : t.getBaris()) System.out.println("  " + l);
    }

    static void cek(String label, boolean ok) {
        System.out.println((ok ? "OK   " : "GAGAL ") + label);
    }

    static void tolak(String label, Class<? extends Exception> tipe, Aksi aksi) {
        try {
            aksi.jalankan();
            System.out.println("GAGAL " + label + " seharusnya ditolak!");
        } catch (Exception e) {
            System.out.println((tipe.isInstance(e) ? "OK   " : "GAGAL ") + label
                    + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
