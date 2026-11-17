/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.main;
import carwash.data.CsvRepository;
import carwash.exception.StokTidakCukupException;
import carwash.logic.CatalogService;
import carwash.model.Item;
import carwash.model.Product;
import carwash.model.Role;
import carwash.model.User;
/**
 *
 * @author argor
 */
public class TesKatalog {

    interface Aksi { void jalankan() throws Exception; }

    public static void main(String[] args) throws Exception {
        CsvRepository repo = new CsvRepository();
        CatalogService cat = new CatalogService(repo, repo.muatKatalog());

        User kasir   = new User("U002", "kasir", "x", Role.KASIR, "K");
        User manager = new User("U001", "manager", "x", Role.MANAGER, "M");

        System.out.println("--- Katalog ---");
        for (Item it : cat.getDaftar()) {
            System.out.println(it.getId() + " | " + it.getNama() + " | "
                    + it.getKategori() + " | " + (long) it.hitungHarga(1));
        }
        System.out.println("3x S01 = " + (long) cat.cariById("S01").hitungHarga(3));

        System.out.println("Tambah: " + cat.tambahService("Cuci Kolong", "35000", false, kasir).getId());
        System.out.println("Tambah: " + cat.tambahProduct("Lap Microfiber", "12000", "8", kasir).getId());

        cat.restock("P01", "5", kasir);
        Product p1 = (Product) cat.cariById("P01");
        System.out.println("Stok P01 setelah restock 5: " + p1.getStok());

        try {
            p1.kurangiStok(100);
        } catch (StokTidakCukupException e) {
            System.out.println("OK  " + e.getMessage());
        }

        cat.ubahItem("P02", "Semir Ban", "21000", "12", kasir);
        Product p2 = (Product) cat.cariById("P02");
        System.out.println("P02: harga " + (long) p2.getHarga() + ", stok " + p2.getStok());

        cat.hapus("S05", kasir);
        System.out.println("S05 setelah hapus: " + cat.cariById("S05"));

        coba("Harga 0", () -> cat.tambahService("X", "0", true, kasir));
        coba("Nama berkoma", () -> cat.tambahService("A,B", "1000", true, kasir));
        coba("Stok negatif", () -> cat.tambahProduct("Y", "1000", "-1", kasir));
        coba("Restock 0", () -> cat.restock("P01", "0", kasir));
        coba("Restock jasa", () -> cat.restock("S01", "5", kasir));
        coba("Hapus tak ada", () -> cat.hapus("Z99", kasir));
        coba("Manager menambah", () -> cat.tambahService("Z", "1000", true, manager));
    }

    private static void coba(String label, Aksi aksi) {
        try {
            aksi.jalankan();
            System.out.println("GAGAL TES: " + label + " seharusnya ditolak!");
        } catch (Exception e) {
            System.out.println("OK  " + label + " -> " + e.getMessage());
        }
    }
}
