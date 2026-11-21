/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.data;

import carwash.logic.AuthService;
import carwash.model.Role;
import carwash.model.User;
import carwash.model.Customer;
import carwash.model.Item;
import carwash.model.Product;
import carwash.model.Service;
import carwash.model.Transaction;
import carwash.model.TransactionLine;
import carwash.model.KoreksiLoyalitas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
/**
 *
 * @author argor
 */
public class CsvRepository {

    private static final Path DIR_DATA = Paths.get("data");
    private static final Path FILE_USERS = DIR_DATA.resolve("users.csv");


    public List<User> muatUsers() throws IOException {
        if (!Files.exists(FILE_USERS)) {
            List<User> seed = buatSeed();
            simpanUsers(seed);
            return seed;
        }

        List<User> hasil = new ArrayList<>();
        List<String> baris = Files.readAllLines(FILE_USERS, StandardCharsets.UTF_8);
        for (int i = 0; i < baris.size(); i++) {
            String line = baris.get(i).trim();
            if (line.isEmpty()) continue;
            try {
                String[] c = line.split(",", -1);
                if (c.length != 5) throw new IllegalArgumentException("jumlah kolom != 5");
                hasil.add(new User(c[0].trim(), c[1].trim(), c[2].trim(),
                        Role.valueOf(c[3].trim()), c[4].trim()));
            } catch (IllegalArgumentException e) {
                System.err.println("users.csv baris " + (i + 1) + " dilewati: " + e.getMessage());
            }
        }
        return hasil;
    }

    private List<User> buatSeed() {
        List<User> seed = new ArrayList<>();
        seed.add(new User("U001", "manager", AuthService.buatHash("manager123"),
                Role.MANAGER, "Pak Hendra"));
        seed.add(new User("U002", "kasir", AuthService.buatHash("kasir123"),
                Role.KASIR, "Budi Kasir"));
        return seed;
    }

    private void simpanUsers(List<User> users) throws IOException {
        Files.createDirectories(DIR_DATA);
        List<String> baris = new ArrayList<>();
        for (User u : users) {
            baris.add(String.join(",", u.getId(), u.getUsername(),
                    u.getPasswordHash(), u.getRole().name(), u.getNama()));
        }
        Files.write(FILE_USERS, baris, StandardCharsets.UTF_8);
    }
    
    private static final Path FILE_HOLIDAYS = DIR_DATA.resolve("holidays.csv");
    
public List<LocalDate> muatTanggalMerah() throws IOException {
    List<LocalDate> hasil = new ArrayList<>();

    if (!Files.exists(FILE_HOLIDAYS)) {
        int tahun = LocalDate.now().getYear();
        hasil.add(LocalDate.of(tahun, 1, 1));    
        hasil.add(LocalDate.of(tahun, 8, 17));  
        hasil.add(LocalDate.of(tahun, 12, 25)); 
        return hasil;
    }

    List<String> baris = Files.readAllLines(FILE_HOLIDAYS, StandardCharsets.UTF_8);
    for (int i = 0; i < baris.size(); i++) {
        String line = baris.get(i).trim();
        if (line.isEmpty()) continue;
        try {
            hasil.add(LocalDate.parse(line));   // format yyyy-MM-dd
        } catch (DateTimeParseException e) {
            System.err.println("holidays.csv baris " + (i + 1) + " dilewati: " + line);
        }
    }
    return hasil;
}

public void simpanTanggalMerah(Collection<LocalDate> daftar) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> baris = new ArrayList<>();
    for (LocalDate t : daftar) {
        baris.add(t.toString());                // otomatis yyyy-MM-dd
    }
    Files.write(FILE_HOLIDAYS, baris, StandardCharsets.UTF_8);
}

//----------

private static final Path FILE_CUSTOMERS = DIR_DATA.resolve("customers.csv");

/** Format: id,nama,telepon. File belum ada -> daftar kosong. Baris rusak dilewati. */
public List<Customer> muatCustomers() throws IOException {
    List<Customer> hasil = new ArrayList<>();
    if (!Files.exists(FILE_CUSTOMERS)) return hasil;

    List<String> baris = Files.readAllLines(FILE_CUSTOMERS, StandardCharsets.UTF_8);
    for (int i = 0; i < baris.size(); i++) {
        String line = baris.get(i).trim();
        if (line.isEmpty()) continue;
        String[] c = line.split(",", -1);
        if (c.length != 3) {
            System.err.println("customers.csv baris " + (i + 1) + " dilewati: " + line);
            continue;
        }
        hasil.add(new Customer(c[0].trim(), c[1].trim(), c[2].trim()));
    }
    return hasil;
}

public void simpanCustomers(Collection<Customer> daftar) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> baris = new ArrayList<>();
    for (Customer c : daftar) {
        baris.add(String.join(",", c.getId(), c.getNama(), c.getTelepon()));
    }
    Files.write(FILE_CUSTOMERS, baris, StandardCharsets.UTF_8);
}

private static final Path FILE_CATALOG = DIR_DATA.resolve("catalog.csv");

/**
 * Format: SERVICE,id,nama,harga,isCuci  atau  PRODUCT,id,nama,harga,stok
 * Jika file belum ada, dibuat katalog awal (seed). Baris rusak dilewati.
 */
public List<Item> muatKatalog() throws IOException {
    if (!Files.exists(FILE_CATALOG)) {
        List<Item> seed = buatSeedKatalog();
        simpanKatalog(seed);
        return seed;
    }

    List<Item> hasil = new ArrayList<>();
    List<String> baris = Files.readAllLines(FILE_CATALOG, StandardCharsets.UTF_8);
    for (int i = 0; i < baris.size(); i++) {
        String line = baris.get(i).trim();
        if (line.isEmpty()) continue;
        try {
            String[] c = line.split(",", -1);
            if (c.length != 5) throw new IllegalArgumentException("jumlah kolom != 5");
            String id = c[1].trim();
            String nama = c[2].trim();
            long harga = Long.parseLong(c[3].trim());
            switch (c[0].trim()) {
                case "SERVICE" -> hasil.add(new Service(id, nama, harga, Boolean.parseBoolean(c[4].trim())));
                case "PRODUCT" -> hasil.add(new Product(id, nama, harga, Integer.parseInt(c[4].trim())));
                default -> throw new IllegalArgumentException("tipe tidak dikenal");
            }
        } catch (IllegalArgumentException e) {   // termasuk NumberFormatException
            System.err.println("catalog.csv baris " + (i + 1) + " dilewati: " + line);
        }
    }
    return hasil;
}

public void simpanKatalog(Collection<Item> daftar) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> baris = new ArrayList<>();
    for (Item it : daftar) {
        if (it instanceof Service s) {
            baris.add(String.join(",", "SERVICE", s.getId(), s.getNama(),
                    String.valueOf((long) s.getHarga()), String.valueOf(s.isCuci())));
        } else if (it instanceof Product p) {
            baris.add(String.join(",", "PRODUCT", p.getId(), p.getNama(),
                    String.valueOf((long) p.getHarga()), String.valueOf(p.getStok())));
        }
    }
    Files.write(FILE_CATALOG, baris, StandardCharsets.UTF_8);
}

private List<Item> buatSeedKatalog() {
    List<Item> seed = new ArrayList<>();
    seed.add(new Service("S01", "Cuci Reguler", 50000, true));
    seed.add(new Service("S02", "Cuci SUV", 80000, true));
    seed.add(new Service("S03", "Poles", 100000, false));
    seed.add(new Service("S04", "Vakum Interior", 30000, false));
    seed.add(new Product("P01", "Pewangi", 15000, 20));
    seed.add(new Product("P02", "Semir Ban", 20000, 15));
    seed.add(new Product("P03", "Pembersih Kaca", 25000, 10));
    return seed;
}

private static final Path FILE_LOYALTY = DIR_DATA.resolve("loyalty.csv");

/** Format: customerId,hitungan. File belum ada -> kosong. Baris rusak dilewati. */
public Map<String, Integer> muatLoyalty() throws IOException {
    Map<String, Integer> hasil = new HashMap<>();
    if (!Files.exists(FILE_LOYALTY)) return hasil;

    List<String> baris = Files.readAllLines(FILE_LOYALTY, StandardCharsets.UTF_8);
    for (int i = 0; i < baris.size(); i++) {
        String line = baris.get(i).trim();
        if (line.isEmpty()) continue;
        try {
            String[] c = line.split(",", -1);
            if (c.length != 2) throw new IllegalArgumentException("jumlah kolom != 2");
            int n = Integer.parseInt(c[1].trim());
            if (n < 0 || n > 10) throw new IllegalArgumentException("hitungan di luar 0..10");
            hasil.put(c[0].trim(), n);
        } catch (IllegalArgumentException e) {   // termasuk NumberFormatException
            System.err.println("loyalty.csv baris " + (i + 1) + " dilewati: " + line);
        }
    }
    return hasil;
}

public void simpanLoyalty(Map<String, Integer> data) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> baris = new ArrayList<>();
    for (Map.Entry<String, Integer> e : new TreeMap<>(data).entrySet()) {  // urut by id
        baris.add(e.getKey() + "," + e.getValue());
    }
    Files.write(FILE_LOYALTY, baris, StandardCharsets.UTF_8);
}

private static final Path FILE_TRANSAKSI = DIR_DATA.resolve("transactions.csv");
private static final Path FILE_BARIS = DIR_DATA.resolve("transaction_lines.csv");

/**
 * transactions.csv: id,tanggal,customerId,kasirId,total,jumlahReward,uangDiterima,kembalian
 * transaction_lines.csv: transactionId,namaItem,hargaSatuan,qty,isReward
 * File belum ada -> kosong. Baris rusak dilewati.
 */
public List<Transaction> muatTransaksi() throws IOException {
    List<Transaction> hasil = new ArrayList<>();
    if (!Files.exists(FILE_TRANSAKSI)) return hasil;

    // 1. baris transaksi dikelompokkan per transactionId
    Map<String, List<TransactionLine>> barisPerTx = new HashMap<>();
    if (Files.exists(FILE_BARIS)) {
        List<String> teks = Files.readAllLines(FILE_BARIS, StandardCharsets.UTF_8);
        for (int i = 0; i < teks.size(); i++) {
            String line = teks.get(i).trim();
            if (line.isEmpty()) continue;
            try {
                String[] c = line.split(",", -1);
                if (c.length != 5) throw new IllegalArgumentException("jumlah kolom != 5");
                barisPerTx.computeIfAbsent(c[0].trim(), k -> new ArrayList<>())
                        .add(new TransactionLine(c[1].trim(), Long.parseLong(c[2].trim()),
                                Integer.parseInt(c[3].trim()), Boolean.parseBoolean(c[4].trim())));
            } catch (IllegalArgumentException e) {
                System.err.println("transaction_lines.csv baris " + (i + 1) + " dilewati: " + line);
            }
        }
    }

    // 2. transaksi
    List<String> teks = Files.readAllLines(FILE_TRANSAKSI, StandardCharsets.UTF_8);
    for (int i = 0; i < teks.size(); i++) {
        String line = teks.get(i).trim();
        if (line.isEmpty()) continue;
        try {
            String[] c = line.split(",", -1);
            if (c.length != 8) throw new IllegalArgumentException("jumlah kolom != 8");
            String id = c[0].trim();
            hasil.add(new Transaction(id, LocalDate.parse(c[1].trim()), c[2].trim(), c[3].trim(),
                    barisPerTx.getOrDefault(id, new ArrayList<>()),
                    Long.parseLong(c[4].trim()), Integer.parseInt(c[5].trim()),
                    Long.parseLong(c[6].trim()), Long.parseLong(c[7].trim())));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.err.println("transactions.csv baris " + (i + 1) + " dilewati: " + line);
        }
    }
    return hasil;
}

public void simpanTransaksi(List<Transaction> daftar) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> tx = new ArrayList<>();
    List<String> baris = new ArrayList<>();
    for (Transaction t : daftar) {
        tx.add(String.join(",", t.getId(), t.getTanggal().toString(), t.getCustomerId(),
                t.getKasirId(), String.valueOf((long) t.getTotal()),
                String.valueOf(t.getJumlahReward()), String.valueOf((long) t.getUangDiterima()),
                String.valueOf((long) t.getKembalian())));
        for (TransactionLine l : t.getBaris()) {
            baris.add(String.join(",", t.getId(), l.getNamaItem(),
                    String.valueOf((long) l.getHargaSatuan()),
                    String.valueOf(l.getQty()), String.valueOf(l.isReward())));
        }
    }
    Files.write(FILE_TRANSAKSI, tx, StandardCharsets.UTF_8);
    Files.write(FILE_BARIS, baris, StandardCharsets.UTF_8);
}

private static final Path FILE_KOREKSI = DIR_DATA.resolve("koreksi.csv");

/** Format: tanggal,customerId,kasirId,nilaiLama,nilaiBaru,alasan. Baris rusak dilewati. */
public List<KoreksiLoyalitas> muatKoreksi() throws IOException {
    List<KoreksiLoyalitas> hasil = new ArrayList<>();
    if (!Files.exists(FILE_KOREKSI)) return hasil;

    List<String> baris = Files.readAllLines(FILE_KOREKSI, StandardCharsets.UTF_8);
    for (int i = 0; i < baris.size(); i++) {
        String line = baris.get(i).trim();
        if (line.isEmpty()) continue;
        try {
            String[] c = line.split(",", -1);
            if (c.length != 6) throw new IllegalArgumentException("jumlah kolom != 6");
            hasil.add(new KoreksiLoyalitas(LocalDate.parse(c[0].trim()), c[1].trim(), c[2].trim(),
                    Integer.parseInt(c[3].trim()), Integer.parseInt(c[4].trim()), c[5].trim()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.err.println("koreksi.csv baris " + (i + 1) + " dilewati: " + line);
        }
    }
    return hasil;
}

public void simpanKoreksi(List<KoreksiLoyalitas> daftar) throws IOException {
    Files.createDirectories(DIR_DATA);
    List<String> baris = new ArrayList<>();
    for (KoreksiLoyalitas k : daftar) {
        baris.add(String.join(",", k.getTanggal().toString(), k.getCustomerId(), k.getKasirId(),
                String.valueOf(k.getNilaiLama()), String.valueOf(k.getNilaiBaru()), k.getAlasan()));
    }
    Files.write(FILE_KOREKSI, baris, StandardCharsets.UTF_8);
}

}