/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public class Customer {
    private final String id;
    private final String nama;
    private final String telepon;

    public Customer(String id, String nama, String telepon) {
        this.id = id;
        this.nama = nama;
        this.telepon = telepon;
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getTelepon() { return telepon; }

    @Override
    public String toString() { return id + " | " + nama + " | " + telepon; }
}
