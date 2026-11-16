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
}