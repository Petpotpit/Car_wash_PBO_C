/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public abstract class Item {
    private final String id;
    private String nama;
    private double harga;

    protected Item(String id, String nama, double harga) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }
    public void setNama(String nama) { this.nama = nama; }
    public void setHarga(double harga) { this.harga = harga; }

    public abstract double hitungHarga(int qty);
    public abstract String getKategori();
    public abstract Item salin();   // salinan dalam, dipakai untuk snapshot rollback
}