/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.data.CsvRepository;
import carwash.model.CartItem;
import carwash.model.Item;
import carwash.model.Service;
import carwash.model.TransactionLine;
import carwash.model.KoreksiLoyalitas;
import carwash.model.Role;
import carwash.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;


/**
 *
 * @author argor
 */
public class LoyaltyTracker {

    public static final int TARGET = 10;

    private final CsvRepository repo;
    private final HolidayCalendar kalender;
    private final HashMap<String, Integer> hitungan = new HashMap<>(); // key = customerId
    private final ArrayList<KoreksiLoyalitas> log = new ArrayList<>();

public LoyaltyTracker(CsvRepository repo, HolidayCalendar kalender, Map<String, Integer> awal) {
    this(repo, kalender, awal, List.of());
}

public LoyaltyTracker(CsvRepository repo, HolidayCalendar kalender,
                      Map<String, Integer> awal, List<KoreksiLoyalitas> logAwal) {
    this.repo = repo;
    this.kalender = kalender;
    this.hitungan.putAll(awal);
    this.log.addAll(logAwal);
}
    public int getHitungan(String customerId) {
        return hitungan.getOrDefault(customerId, 0);
    }

    /** Untuk badge "Reward siap dipakai". */
    public boolean rewardSiap(String customerId, LocalDate tanggal) {
        return kalender.isProgramAktif(tanggal) && getHitungan(customerId) == TARGET;
    }

    /** Hanya menghitung. Tidak mengubah data apa pun. */
    public LoyaltyResult preview(String customerId, List<CartItem> isi, LocalDate tanggal) {
        boolean aktif = kalender.isProgramAktif(tanggal);
        int awal = getHitungan(customerId);

        // 1. Pisahkan: baris non-cuci apa adanya, cuci dipecah per unit
        List<TransactionLine> barisLain = new ArrayList<>();
        List<Item> unit = new ArrayList<>();
        for (CartItem ci : isi) {
            Item it = ci.getItem();
            if (it instanceof Service s && s.isCuci()) {
                for (int i = 0; i < ci.getQty(); i++) unit.add(s);
            } else {
                barisLain.add(new TransactionLine(it.getNama(), it.getHarga(), ci.getQty(), false));
            }
        }

        // 2. Hari aktif: termurah dulu
        if (aktif) {
            unit.sort(Comparator.comparingDouble(Item::getHarga));
        }

        // 3. Proses tiap unit dengan hitungan berjalan
        int c = awal;
        int gratis = 0;
        double potongan = 0;
        List<TransactionLine> barisCuci = new ArrayList<>();

        for (Item u : unit) {
            boolean free = false;
            if (aktif) {
                if (c == TARGET) {
                    free = true;
                    c = 0;
                    gratis++;
                    potongan += u.getHarga();
                } else {
                    c++;
                }
            }
            tambahGabung(barisCuci, u, free);
        }

        // 4. Susun hasil
        List<TransactionLine> semua = new ArrayList<>(barisCuci);
        semua.addAll(barisLain);
        double total = 0;
        for (TransactionLine l : semua) total += l.getSubtotal();

        return new LoyaltyResult(awal, c, aktif, gratis, potongan, total, semua);
    }

    /** Menerapkan hasil preview ke memori. Belum menulis file. */
    public void commit(String customerId, LoyaltyResult hasil) {
        int akhir = hasil.getHitunganAkhir();
        if (akhir < 0 || akhir > TARGET) {
            throw new IllegalStateException("Hitungan akhir di luar 0 sampai " + TARGET);
        }
        if (hasil.getHitunganAwal() != getHitungan(customerId)) {
            throw new IllegalStateException("Hitungan berubah sejak preview, ulangi preview");
        }
        hitungan.put(customerId, akhir);
    }

    // ---- penyimpanan dan rollback (dipakai CheckoutService nanti) ----

    public void simpan() throws IOException {
        repo.simpanLoyalty(hitungan);
    }

    public Map<String, Integer> salinan() {
        return new HashMap<>(hitungan);
    }

    public void gantiSemua(Map<String, Integer> nilaiLama) {
        hitungan.clear();
        hitungan.putAll(nilaiLama);
    }

    // ---- bantu ----

    /** Gabungkan dengan baris cuci terakhir jika nama dan status sama. */
    private void tambahGabung(List<TransactionLine> daftar, Item u, boolean free) {
        double harga = free ? 0 : u.getHarga();
        int last = daftar.size() - 1;
        if (last >= 0) {
            TransactionLine l = daftar.get(last);
            if (l.getNamaItem().equals(u.getNama()) && l.isReward() == free) {
                daftar.set(last, new TransactionLine(l.getNamaItem(), harga, l.getQty() + 1, free));
                return;
            }
        }
        daftar.add(new TransactionLine(u.getNama(), harga, 1, free));
    }

    public List<KoreksiLoyalitas> getLogKoreksi() {
    return Collections.unmodifiableList(log);
}

/** Hanya Kasir. Mengubah hitungan, mencatat log, menyimpan, rollback jika gagal. */
public KoreksiLoyalitas koreksi(User kasir, String customerId, int nilaiBaru, String alasan)
        throws AksesDitolakException, InputTidakValidException, PenyimpananGagalException {

    if (kasir == null || kasir.getRole() != Role.KASIR) {
        throw new AksesDitolakException("Hanya Kasir yang boleh mengoreksi hitungan");
    }
    if (nilaiBaru < 0 || nilaiBaru > TARGET) {
        throw new InputTidakValidException("Hitungan harus berupa bilangan bulat 0 sampai 10");
    }
    String a = Validator.validasiAlasan(alasan);
    int lama = getHitungan(customerId);
    if (nilaiBaru == lama) {
        throw new InputTidakValidException("Hitungan baru sama dengan hitungan saat ini");
    }

    Map<String, Integer> hitunganLama = salinan();              // snapshot
    KoreksiLoyalitas k = new KoreksiLoyalitas(LocalDate.now(), customerId,
            kasir.getId(), lama, nilaiBaru, a);

    hitungan.put(customerId, nilaiBaru);                        // ubah di memori
    log.add(k);
    try {
        repo.simpanLoyalty(hitungan);                           // tulis file
        repo.simpanKoreksi(log);
    } catch (IOException e) {
        gantiSemua(hitunganLama);                               // rollback
        log.remove(k);
        try { repo.simpanLoyalty(hitungan); } catch (IOException ignored) { /* samakan file dengan memori */ }
        throw new PenyimpananGagalException("Gagal menyimpan koreksi, perubahan dibatalkan", e);
    }
    return k;
}
}
