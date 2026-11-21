/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.model.KoreksiLoyalitas;
import carwash.model.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author argor
 */
public class LaporanService {

    private final CheckoutService checkout;
    private final LoyaltyTracker loyalty;

    public LaporanService(CheckoutService checkout, LoyaltyTracker loyalty) {
        this.checkout = checkout;
        this.loyalty = loyalty;
    }

    public List<Transaction> riwayatPelanggan(String customerId) {
        List<Transaction> hasil = new ArrayList<>();
        for (Transaction t : checkout.getRiwayat()) {
            if (t.getCustomerId().equals(customerId)) hasil.add(t);
        }
        return hasil;
    }

    /** kasirId null = semua kasir. awal/akhir null = tanpa batas. Rentang inklusif. */
    public List<Transaction> filterTransaksi(String kasirId, LocalDate awal, LocalDate akhir) {
        List<Transaction> hasil = new ArrayList<>();
        for (Transaction t : checkout.getRiwayat()) {
            if (cocokKasir(kasirId, t.getKasirId()) && dalamRentang(t.getTanggal(), awal, akhir)) {
                hasil.add(t);
            }
        }
        return hasil;
    }

    public List<KoreksiLoyalitas> filterKoreksi(String kasirId, LocalDate awal, LocalDate akhir) {
        List<KoreksiLoyalitas> hasil = new ArrayList<>();
        for (KoreksiLoyalitas k : loyalty.getLogKoreksi()) {
            if (cocokKasir(kasirId, k.getKasirId()) && dalamRentang(k.getTanggal(), awal, akhir)) {
                hasil.add(k);
            }
        }
        return hasil;
    }

    private boolean cocokKasir(String filter, String id) {
        return filter == null || filter.equals(id);
    }

    private boolean dalamRentang(LocalDate t, LocalDate awal, LocalDate akhir) {
        return (awal == null || !t.isBefore(awal)) && (akhir == null || !t.isAfter(akhir));
    }
}
