/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public class TransactionLine {
    private final String namaItem;
    private final double hargaSatuan;   // 0 untuk baris reward
    private final int qty;
    private final boolean reward;

    public TransactionLine(String namaItem, double hargaSatuan, int qty, boolean reward) {
        this.namaItem = namaItem;
        this.hargaSatuan = hargaSatuan;
        this.qty = qty;
        this.reward = reward;
    }

    public String getNamaItem() { return namaItem; }
    public double getHargaSatuan() { return hargaSatuan; }
    public int getQty() { return qty; }
    public boolean isReward() { return reward; }
    public double getSubtotal() { return hargaSatuan * qty; }

    @Override
    public String toString() {
        return namaItem + " x" + qty + "  Rp " + (long) hargaSatuan
                + (reward ? "  (GRATIS - reward)" : "");
    }
}