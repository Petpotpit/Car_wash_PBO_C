/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public class CartItem {
    private final Item item;
    private final int qty;

    public CartItem(Item item, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Qty harus lebih dari 0");
        this.item = item;
        this.qty = qty;
    }

    public Item getItem() { return item; }
    public int getQty() { return qty; }
}