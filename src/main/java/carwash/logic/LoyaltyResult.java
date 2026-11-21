/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;


import carwash.model.TransactionLine;
import java.util.List;
/**
 *
 * @author argor
 */
public class LoyaltyResult {
    private final int hitunganAwal;
    private final int hitunganAkhir;
    private final boolean aktifHariIni;
    private final int jumlahGratis;
    private final double potongan;
    private final double total;
    private final List<TransactionLine> baris;

    public LoyaltyResult(int hitunganAwal, int hitunganAkhir, boolean aktifHariIni,
                         int jumlahGratis, double potongan, double total,
                         List<TransactionLine> baris) {
        this.hitunganAwal = hitunganAwal;
        this.hitunganAkhir = hitunganAkhir;
        this.aktifHariIni = aktifHariIni;
        this.jumlahGratis = jumlahGratis;
        this.potongan = potongan;
        this.total = total;
        this.baris = baris;
    }

    public int getHitunganAwal() { return hitunganAwal; }
    public int getHitunganAkhir() { return hitunganAkhir; }
    public boolean isAktifHariIni() { return aktifHariIni; }
    public int getJumlahGratis() { return jumlahGratis; }
    public double getPotongan() { return potongan; }
    public double getTotal() { return total; }

    /** Baris sudah digabung per status (berbayar/reward). Dipakai keranjang dan struk. */
    public List<TransactionLine> getBarisTampil() { return baris; }
}
