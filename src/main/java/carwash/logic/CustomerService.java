/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.data.CsvRepository;
import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.exception.CustomerNotFoundException;
import carwash.model.Customer;
import carwash.model.Role;
import carwash.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author argor
 */
public class CustomerService {
    private final CsvRepository repo;
    private final HashMap<String, Customer> customers = new HashMap<>(); // key = id

    public CustomerService(CsvRepository repo, List<Customer> awal) {
        this.repo = repo;
        for (Customer c : awal) {
            customers.put(c.getId(), c);
        }
    }

    public Customer daftar(String nama, String telepon, User kasir)
            throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {

        if (kasir == null || kasir.getRole() != Role.KASIR) {
            throw new AksesDitolakException("Hanya Kasir yang boleh mendaftarkan pelanggan");
        }

        String n = Validator.validasiNama(nama);
        String t = Validator.validasiTelepon(telepon);

        Customer ada = cariByTelepon(t);          // telepon harus unik (loop biasa)
        if (ada != null) {
            throw new InputTidakValidException(
                    "Nomor sudah terdaftar atas nama " + ada.getNama());
        }

        Customer baru = new Customer(buatIdBaru(), n, t);
        customers.put(baru.getId(), baru);        // ubah di memori
        try {
            repo.simpanCustomers(daftarUrut());   // tulis file
        } catch (IOException e) {
            customers.remove(baru.getId());       // rollback
            throw new PenyimpananGagalException(
                    "Gagal menyimpan pelanggan, pendaftaran dibatalkan", e);
        }
        return baru;
    }

    public Customer cariById(String id) {
        return customers.get(id);
    }

    public Customer cariByTelepon(String telepon) {
        for (Customer c : customers.values()) {
            if (c.getTelepon().equals(telepon)) return c;
        }
        return null;
    }

    /** Cari lewat ID, telepon, atau sebagian nama (tanpa peduli huruf besar/kecil). */
    public List<Customer> cari(String kata) {
        String k = (kata == null) ? "" : kata.trim().toLowerCase();
        List<Customer> hasil = new ArrayList<>();
        if (k.isEmpty()) return hasil;
        for (Customer c : customers.values()) {
            if (c.getId().toLowerCase().equals(k)
                    || c.getTelepon().contains(k)
                    || c.getNama().toLowerCase().contains(k)) {
                hasil.add(c);
            }
        }
        hasil.sort(Comparator.comparing(Customer::getId));
        return hasil;
    }

    public List<Customer> daftarUrut() {
        List<Customer> daftar = new ArrayList<>(customers.values());
        daftar.sort(Comparator.comparing(Customer::getId));
        return Collections.unmodifiableList(daftar);
    }

    /** C001, C002, ... berdasarkan nomor terbesar yang ada, jadi tidak bentrok. */
    private String buatIdBaru() {
        int max = 0;
        for (String id : customers.keySet()) {
            try {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                // id tidak standar, abaikan
            }
        }
        return String.format("C%03d", max + 1);
    }
    
    /** Cari lewat ID, telepon, atau nama. Pesan error langsung siap ditampilkan. */
public List<Customer> cariWajib(String kata)
        throws InputTidakValidException, CustomerNotFoundException {
    if (kata == null || kata.trim().isEmpty()) {
        throw new InputTidakValidException("Isi ID, telepon, atau nama pelanggan");
    }
    List<Customer> hasil = cari(kata);
    if (hasil.isEmpty()) {
        throw new CustomerNotFoundException("Pelanggan tidak ditemukan");
    }
    return hasil;
}
}
