/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.exception.InputTidakValidException;
import carwash.exception.StokTidakCukupException;
import carwash.model.CartItem;
import carwash.model.Item;
import carwash.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author argor
 */
public class Cart {

    private final ArrayList<CartItem> isi = new ArrayList<>();

    public void tambah(Item item, int qty)
            throws InputTidakValidException, StokTidakCukupException {
        cekQty(qty);
        int idx = cari(item.getId());
        int total = qty + (idx >= 0 ? isi.get(idx).getQty() : 0);
        cekStok(item, total);
        if (idx >= 0) isi.set(idx, new CartItem(item, total));
        else isi.add(new CartItem(item, qty));
    }

    public void ubahQty(String itemId, int qtyBaru)
            throws InputTidakValidException, StokTidakCukupException {
        cekQty(qtyBaru);
        int idx = cari(itemId);
        if (idx < 0) throw new InputTidakValidException("Item tidak ada di keranjang");
        Item item = isi.get(idx).getItem();
        cekStok(item, qtyBaru);
        isi.set(idx, new CartItem(item, qtyBaru));
    }

    public void hapus(String itemId) {
        int idx = cari(itemId);
        if (idx >= 0) isi.remove(idx);
    }

    public double subtotal() {
        double s = 0;
        for (CartItem ci : isi) s += ci.getItem().hitungHarga(ci.getQty());
        return s;
    }

    public boolean isKosong() { return isi.isEmpty(); }
    public void kosongkan() { isi.clear(); }
    public List<CartItem> getIsi() { return Collections.unmodifiableList(isi); }

    // ---- bantu ----

    private int cari(String itemId) {
        for (int i = 0; i < isi.size(); i++) {
            if (isi.get(i).getItem().getId().equals(itemId)) return i;
        }
        return -1;
    }

    private void cekQty(int qty) throws InputTidakValidException {
        if (qty <= 0) throw new InputTidakValidException("Qty harus lebih dari 0");
    }

    private void cekStok(Item item, int qtyTotal) throws StokTidakCukupException {
        if (item instanceof Product p && !p.cekStok(qtyTotal)) {
            throw new StokTidakCukupException(
                    "Stok " + p.getNama() + " tidak cukup (tersisa " + p.getStok() + ")");
        }
    }
}
