/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

import carwash.exception.StokTidakCukupException;
/**
 *
 * @author argor
 */
public class Product extends Item {
    private int stok;

    public Product(String id, String nama, double harga, int stok) {
        super(id, nama, harga);
        this.stok = stok;
    }

    public int getStok() { return stok; }

    public void setStok(int stok) {
        if (stok < 0) throw new IllegalArgumentException("Stok tidak boleh negatif");
        this.stok = stok;
    }

    public boolean cekStok(int qty) { return stok >= qty; }

    public void kurangiStok(int qty) throws StokTidakCukupException {
        if (!cekStok(qty)) {
            throw new StokTidakCukupException(
                    "Stok " + getNama() + " tidak cukup (tersisa " + stok + ")");
        }
        stok -= qty;
    }

    public void tambahStok(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Jumlah restock harus lebih dari 0");
        stok += qty;
    }

    @Override public double hitungHarga(int qty) { return getHarga() * qty; }
    @Override public String getKategori() { return "Produk"; }
    @Override public Item salin() { return new Product(getId(), getNama(), getHarga(), stok); }
}
