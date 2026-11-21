/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.model.Transaction;
import carwash.model.TransactionLine;

/**
 *
 * @author argor
 */
public class StrukBuilder {

    private StrukBuilder() {}

    /** Struk saat pembayaran (lengkap dengan potongan dan status loyalitas). */
    public static String buat(Transaction tx, String namaPelanggan, String namaKasir,
                              LoyaltyResult hasil) {
        StringBuilder status = new StringBuilder();
        if (!hasil.isAktifHariIni()) {
            status.append("Program loyalitas tidak aktif hari ini (Minggu/tanggal merah)\n");
        } else {
            if (hasil.getJumlahGratis() > 0) {
                status.append("Cuci GRATIS (reward) digunakan: ")
                      .append(hasil.getJumlahGratis()).append(" unit\n");
            }
            status.append("Cuci berbayar: ").append(hasil.getHitunganAkhir())
                  .append("/").append(LoyaltyTracker.TARGET).append('\n');
        }
        return susun(tx, namaPelanggan, namaKasir, hasil.getPotongan(), status.toString());
    }

    /** Salinan dari riwayat. Potongan dan hitungan tidak tersimpan, jadi tidak ditampilkan. */
    public static String buatUlang(Transaction tx, String namaPelanggan, String namaKasir) {
        String status = "*** SALINAN STRUK ***\n";
        if (tx.getJumlahReward() > 0) {
            status += "Cuci GRATIS (reward) digunakan: " + tx.getJumlahReward() + " unit\n";
        }
        return susun(tx, namaPelanggan, namaKasir, null, status);
    }

    private static String susun(Transaction tx, String namaPelanggan, String namaKasir,
                                Double potongan, String status) {
        String garis = "-".repeat(49);
        StringBuilder sb = new StringBuilder();

        sb.append("CAR WASH - STRUK\n").append(garis).append('\n');
        sb.append("No. Transaksi : ").append(tx.getId()).append('\n');
        sb.append("Tanggal       : ").append(tx.getTanggal()).append('\n');
        sb.append("Pelanggan     : ").append(namaPelanggan).append('\n');
        sb.append("Kasir         : ").append(namaKasir).append('\n');
        sb.append(garis).append('\n');

        for (TransactionLine l : tx.getBaris()) {
            String kiri = l.getNamaItem() + " x" + l.getQty()
                    + (l.isReward() ? " (GRATIS - reward)" : "");
            sb.append(baris(kiri, Rupiah.format(l.getSubtotal())));
        }

        sb.append(garis).append('\n');
        if (potongan != null) {
            sb.append(baris("Subtotal", Rupiah.format(tx.getTotal() + potongan)));
            sb.append(baris("Potongan", Rupiah.format(potongan)));
        }
        sb.append(baris("TOTAL", Rupiah.format(tx.getTotal())));
        sb.append(baris("Uang diterima", Rupiah.format(tx.getUangDiterima())));
        sb.append(baris("Kembalian", Rupiah.format(tx.getKembalian())));
        sb.append(garis).append('\n');
        sb.append(status);
        sb.append("Terima kasih\n");
        return sb.toString();
    }

    private static String baris(String kiri, String kanan) {
        return String.format("%-36s %12s\n", kiri, kanan);
    }
}
