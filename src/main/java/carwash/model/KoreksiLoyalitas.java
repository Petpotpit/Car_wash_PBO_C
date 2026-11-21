/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

import java.time.LocalDate;

/**
 *
 * @author argor
 */
public class KoreksiLoyalitas {
    private final LocalDate tanggal;
    private final String customerId;
    private final String kasirId;
    private final int nilaiLama;
    private final int nilaiBaru;
    private final String alasan;

    public KoreksiLoyalitas(LocalDate tanggal, String customerId, String kasirId,
                            int nilaiLama, int nilaiBaru, String alasan) {
        this.tanggal = tanggal;
        this.customerId = customerId;
        this.kasirId = kasirId;
        this.nilaiLama = nilaiLama;
        this.nilaiBaru = nilaiBaru;
        this.alasan = alasan;
    }

    public LocalDate getTanggal() { return tanggal; }
    public String getCustomerId() { return customerId; }
    public String getKasirId() { return kasirId; }
    public int getNilaiLama() { return nilaiLama; }
    public int getNilaiBaru() { return nilaiBaru; }
    public String getAlasan() { return alasan; }
}
