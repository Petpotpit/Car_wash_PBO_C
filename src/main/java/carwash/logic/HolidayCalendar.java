/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.model.Role;
import carwash.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author argor
 */
public class HolidayCalendar {

    private final HashSet<LocalDate> tanggalMerah = new HashSet<>();

    public HolidayCalendar(Collection<LocalDate> awal) {
        tanggalMerah.addAll(awal);
    }

    /** Minggu dihitung dari DayOfWeek, tidak disimpan di set. */
    public boolean isProgramAktif(LocalDate tanggal) {
        return tanggal.getDayOfWeek() != DayOfWeek.SUNDAY
                && !tanggalMerah.contains(tanggal);
    }

    public boolean adaTanggalUntukTahun(int tahun) {
        for (LocalDate t : tanggalMerah) {
            if (t.getYear() == tahun) return true;
        }
        return false;
    }

    public void tambahTanggal(LocalDate tanggal, User user)
            throws AksesDitolakException, InputTidakValidException {
        periksaManager(user);
        if (!tanggalMerah.add(tanggal)) { // add() false = sudah ada
            throw new InputTidakValidException("Tanggal sudah terdaftar");
        }
    }

    public void hapusTanggal(LocalDate tanggal, User user)
            throws AksesDitolakException, InputTidakValidException {
        periksaManager(user);
        if (!tanggalMerah.remove(tanggal)) {
            throw new InputTidakValidException("Tanggal tidak ditemukan");
        }
    }

    public List<LocalDate> getDaftarUrut() {
        List<LocalDate> daftar = new ArrayList<>(tanggalMerah);
        Collections.sort(daftar);
        return daftar;
    }

    // ---- untuk rollback ----
    public Set<LocalDate> salinan() {
        return new HashSet<>(tanggalMerah);
    }

    public void gantiSemua(Set<LocalDate> nilaiLama) {
        tanggalMerah.clear();
        tanggalMerah.addAll(nilaiLama);
    }

    private void periksaManager(User user) throws AksesDitolakException {
        if (user == null || user.getRole() != Role.MANAGER) {
            throw new AksesDitolakException("Hanya Manager yang boleh mengubah tanggal merah");
        }
    }
}