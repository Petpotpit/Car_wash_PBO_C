/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.main;

import carwash.data.CsvRepository;
import carwash.logic.HolidayCalendar;
import carwash.logic.LoyaltyResult;
import carwash.logic.LoyaltyTracker;
import carwash.model.CartItem;
import carwash.model.Service;
import carwash.model.TransactionLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 *
 * @author argor
 */
public class TesLoyalty {

    static final Service REGULER = new Service("S01", "Cuci Reguler", 50000, true);
    static final Service SUV     = new Service("S02", "Cuci SUV", 80000, true);
    static final Service POLES   = new Service("S03", "Poles", 100000, false);

    static final LocalDate RABU   = LocalDate.of(2026, 11, 18); // hari aktif
    static final LocalDate MINGGU = LocalDate.of(2026, 11, 22); // non-aktif

    static final CsvRepository repo = new CsvRepository();
    static final HolidayCalendar kalender = new HolidayCalendar(List.of());

    public static void main(String[] args) throws Exception {
        uji("A", 7,  RABU,   List.of(new CartItem(REGULER, 1)), 50000, 8, 0);
        uji("B", 10, RABU,   List.of(new CartItem(REGULER, 1), new CartItem(SUV, 1)), 80000, 1, 1);
        uji("C", 8,  RABU,   List.of(new CartItem(REGULER, 4)), 150000, 1, 1);
        uji("D", 10, MINGGU, List.of(new CartItem(REGULER, 1)), 50000, 10, 0);
        uji("E", 10, RABU,   List.of(new CartItem(POLES, 1)), 100000, 10, 0);

        // Tambahan: urutan keranjang tidak boleh mempengaruhi hasil
        uji("B-terbalik", 10, RABU, List.of(new CartItem(SUV, 1), new CartItem(REGULER, 1)), 80000, 1, 1);

        // Tambahan: commit dan simpan
        LoyaltyTracker t = new LoyaltyTracker(repo, kalender, Map.of("C001", 8));
        LoyaltyResult r = t.preview("C001", List.of(new CartItem(REGULER, 4)), RABU);
        System.out.println("Sebelum commit: " + t.getHitungan("C001"));   // 8 (preview tidak mengubah)
        t.commit("C001", r);
        System.out.println("Setelah commit: " + t.getHitungan("C001"));   // 1
        try {
            t.commit("C001", r);                                          // hasil kedaluwarsa
            System.out.println("GAGAL TES: commit ganda seharusnya ditolak!");
        } catch (IllegalStateException e) {
            System.out.println("OK  commit ganda -> " + e.getMessage());
        }
        t.simpan();
        System.out.println("Dimuat ulang dari file: " + repo.muatLoyalty());  // {C001=1}
    }

    static void uji(String kasus, int awal, LocalDate tgl, List<CartItem> isi,
                    double totalDiharap, int akhirDiharap, int gratisDiharap) {
        LoyaltyTracker t = new LoyaltyTracker(repo, kalender, Map.of("C001", awal));
        LoyaltyResult r = t.preview("C001", isi, tgl);

        boolean lolos = r.getTotal() == totalDiharap
                && r.getHitunganAkhir() == akhirDiharap
                && r.getJumlahGratis() == gratisDiharap
                && t.getHitungan("C001") == awal;   // preview tidak boleh mengubah data

        System.out.println((lolos ? "OK   " : "GAGAL ") + "Kasus " + kasus
                + " | awal " + awal + " -> akhir " + r.getHitunganAkhir()
                + " | total " + (long) r.getTotal()
                + " | gratis " + r.getJumlahGratis()
                + " | reward siap: " + t.rewardSiap("C001", tgl));
        for (TransactionLine l : r.getBarisTampil()) {
            System.out.println("       " + l);
        }
    }
}
