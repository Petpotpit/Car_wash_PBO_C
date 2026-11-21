/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 * @author argor
 */
public class Transaction {
    private final String id;
    private final LocalDate tanggal;
    private final String customerId;
    private final String kasirId;
    private final List<TransactionLine> baris;
    private final double total;
    private final int jumlahReward;
    private final double uangDiterima;
    private final double kembalian;

    public Transaction(String id, LocalDate tanggal, String customerId, String kasirId,
                       List<TransactionLine> baris, double total, int jumlahReward,
                       double uangDiterima, double kembalian) {
        this.id = id;
        this.tanggal = tanggal;
        this.customerId = customerId;
        this.kasirId = kasirId;
        this.baris = Collections.unmodifiableList(new ArrayList<>(baris)); // salinan
        this.total = total;
        this.jumlahReward = jumlahReward;
        this.uangDiterima = uangDiterima;
        this.kembalian = kembalian;
    }

    public String getId() { return id; }
    public LocalDate getTanggal() { return tanggal; }
    public String getCustomerId() { return customerId; }
    public String getKasirId() { return kasirId; }
    public List<TransactionLine> getBaris() { return baris; }
    public double getTotal() { return total; }
    public int getJumlahReward() { return jumlahReward; }
    public double getUangDiterima() { return uangDiterima; }
    public double getKembalian() { return kembalian; }
}
