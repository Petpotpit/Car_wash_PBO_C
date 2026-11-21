/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;


import carwash.exception.InputTidakValidException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class Validator {
    private Validator() {}

    public static LocalDate parseTanggal(String teks) throws InputTidakValidException {
        if (teks == null || teks.trim().isEmpty()) {
            throw new InputTidakValidException("Tanggal tidak boleh kosong");
        }
        try {
            return LocalDate.parse(teks.trim()); // ISO: yyyy-MM-dd, 2026-02-30 ditolak
        } catch (DateTimeParseException e) {
            throw new InputTidakValidException(
                    "Format tanggal harus yyyy-MM-dd, contoh: 2026-12-25");
        }
    }
    
    /** Mengembalikan nama yang sudah di-trim. */
    public static String validasiNama(String nama) throws InputTidakValidException {
        String n = (nama == null) ? "" : nama.trim();
        if (n.isEmpty()) {
            throw new InputTidakValidException("Nama tidak boleh kosong");
        }
        if (n.length() < 2 || n.length() > 50) {
            throw new InputTidakValidException("Nama harus 2 sampai 50 karakter");
        }
        if (n.contains(",")) {
            throw new InputTidakValidException("Nama tidak boleh mengandung koma");
        }
        return n;
    }

    /** Hanya angka, 10 sampai 13 digit, diawali 0. Keunikan dicek di CustomerService. */
    public static String validasiTelepon(String telepon) throws InputTidakValidException {
        String t = (telepon == null) ? "" : telepon.trim();
        if (!t.matches("0\\d{9,12}")) {
            throw new InputTidakValidException(
                    "Telepon harus berupa angka 10 sampai 13 digit dan diawali 0");
        }
        return t;
    }
    
    public static String validasiNamaItem(String nama) throws InputTidakValidException {
    String n = (nama == null) ? "" : nama.trim();
    if (n.isEmpty()) {
        throw new InputTidakValidException("Nama item tidak boleh kosong");
    }
    if (n.contains(",")) {
        throw new InputTidakValidException("Nama item tidak boleh mengandung koma");
    }
    return n;
}

/** Bilangan bulat lebih dari 0. */
public static double validasiHarga(String harga) throws InputTidakValidException {
    String pesan = "Harga harus berupa bilangan bulat lebih dari 0";
    long v = parseBulat(harga, pesan);
    if (v <= 0) throw new InputTidakValidException(pesan);
    return v;
}

/** Bilangan bulat, tidak negatif. */
public static int validasiStok(String stok) throws InputTidakValidException {
    String pesan = "Stok harus berupa bilangan bulat, tidak negatif";
    long v = parseBulat(stok, pesan);
    if (v < 0 || v > Integer.MAX_VALUE) throw new InputTidakValidException(pesan);
    return (int) v;
}

/** Bilangan bulat lebih dari 0. */
public static int validasiRestock(String jumlah) throws InputTidakValidException {
    String pesan = "Jumlah restock harus berupa bilangan bulat lebih dari 0";
    long v = parseBulat(jumlah, pesan);
    if (v <= 0 || v > Integer.MAX_VALUE) throw new InputTidakValidException(pesan);
    return (int) v;
}

private static long parseBulat(String s, String pesan) throws InputTidakValidException {
    try {
        return Long.parseLong(s == null ? "" : s.trim());
    } catch (NumberFormatException e) {
        throw new InputTidakValidException(pesan);
    }
}

/** Bilangan bulat lebih dari 0. */
public static int validasiQty(String qty) throws InputTidakValidException {
    String pesan = "Qty harus berupa bilangan bulat lebih dari 0";
    long v = parseBulat(qty, pesan);
    if (v <= 0 || v > Integer.MAX_VALUE) throw new InputTidakValidException(pesan);
    return (int) v;
}

/** Bilangan bulat rupiah, tidak negatif. */
public static long validasiUang(String uang) throws InputTidakValidException {
    String pesan = "Uang diterima harus berupa angka bulat, tidak negatif";
    long v = parseBulat(uang, pesan);
    if (v < 0) throw new InputTidakValidException(pesan);
    return v;
}
/** Bilangan bulat 0 sampai 10. */
public static int validasiHitungan(String s) throws InputTidakValidException {
    String pesan = "Hitungan harus berupa bilangan bulat 0 sampai 10";
    long v = parseBulat(s, pesan);
    if (v < 0 || v > 10) throw new InputTidakValidException(pesan);
    return (int) v;
}

/** Wajib diisi, tanpa koma (karena disimpan di CSV). */
public static String validasiAlasan(String alasan) throws InputTidakValidException {
    String a = (alasan == null) ? "" : alasan.trim();
    if (a.isEmpty()) throw new InputTidakValidException("Alasan wajib diisi");
    if (a.contains(",")) throw new InputTidakValidException("Alasan tidak boleh mengandung koma");
    return a;
}

}