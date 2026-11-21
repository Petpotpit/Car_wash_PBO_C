package carwash;

import carwash.data.CsvRepository;
import carwash.exception.*;
import carwash.logic.HolidayCalendar;
import carwash.logic.LoyaltyTracker;
import carwash.logic.Validator;
import carwash.model.KoreksiLoyalitas;
import carwash.model.Role;
import carwash.model.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TesKoreksi {

    interface Aksi { void jalankan() throws Exception; }

    public static void main(String[] args) throws Exception {
        CsvRepository palsu = new CsvRepository() {
            @Override public void simpanLoyalty(Map<String, Integer> d) throws IOException { }
            @Override public void simpanKoreksi(List<KoreksiLoyalitas> d) throws IOException { }
        };
        CsvRepository gagal = new CsvRepository() {
            @Override public void simpanLoyalty(Map<String, Integer> d) throws IOException { }
            @Override public void simpanKoreksi(List<KoreksiLoyalitas> d) throws IOException {
                throw new IOException("simulasi disk penuh");
            }
        };
        HolidayCalendar kal = new HolidayCalendar(List.of());
        User kasir   = new User("U002", "kasir", "x", Role.KASIR, "Budi Kasir");
        User manager = new User("U001", "manager", "x", Role.MANAGER, "Pak Hendra");

        LoyaltyTracker t = new LoyaltyTracker(palsu, kal, Map.of("C001", 8));

        KoreksiLoyalitas k = t.koreksi(kasir, "C001", 3, "salah input kasir lama");
        cek("K1 koreksi 8 -> 3, log 1, kasirId U002",
                t.getHitungan("C001") == 3 && t.getLogKoreksi().size() == 1
                && k.getNilaiLama() == 8 && k.getNilaiBaru() == 3 && k.getKasirId().equals("U002"));

        tolak("K2 Manager", AksesDitolakException.class, () -> t.koreksi(manager, "C001", 5, "x"));
        tolak("K3 Nilai 11", InputTidakValidException.class, () -> t.koreksi(kasir, "C001", 11, "x"));
        tolak("K3b Teks abc", InputTidakValidException.class, () -> Validator.validasiHitungan("abc"));
        tolak("K4 Alasan kosong", InputTidakValidException.class, () -> t.koreksi(kasir, "C001", 5, "  "));
        tolak("K4b Alasan koma", InputTidakValidException.class, () -> t.koreksi(kasir, "C001", 5, "a,b"));
        tolak("K5 Nilai sama", InputTidakValidException.class, () -> t.koreksi(kasir, "C001", 3, "x"));
        cek("K2-K5 data tidak berubah", t.getHitungan("C001") == 3 && t.getLogKoreksi().size() == 1);

        LoyaltyTracker t2 = new LoyaltyTracker(gagal, kal, Map.of("C001", 3));
        tolak("K6 Simpan gagal", PenyimpananGagalException.class,
                () -> t2.koreksi(kasir, "C001", 9, "uji rollback"));
        cek("K6 rollback: hitungan 3, log kosong",
                t2.getHitungan("C001") == 3 && t2.getLogKoreksi().isEmpty());

        CsvRepository asli = new CsvRepository();      // menulis data/koreksi.csv
        asli.simpanKoreksi(t.getLogKoreksi());
        List<KoreksiLoyalitas> muat = asli.muatKoreksi();
        cek("K7 muat ulang: 1 baris, alasan dan nilai sama",
                muat.size() == 1 && muat.get(0).getAlasan().equals("salah input kasir lama")
                && muat.get(0).getNilaiBaru() == 3);
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