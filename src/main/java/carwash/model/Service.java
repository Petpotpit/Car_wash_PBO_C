/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public class Service extends Item {
    private final boolean cuci;

    public Service(String id, String nama, double harga, boolean cuci) {
        super(id, nama, harga);
        this.cuci = cuci;
    }

    public boolean isCuci() { return cuci; }

    @Override public double hitungHarga(int qty) { return getHarga() * qty; }
    @Override public String getKategori() { return cuci ? "Cuci" : "Layanan Tambahan"; }
    @Override public Item salin() { return new Service(getId(), getNama(), getHarga(), cuci); }
}
