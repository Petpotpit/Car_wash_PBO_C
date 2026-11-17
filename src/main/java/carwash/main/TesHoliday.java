package carwash.main;

import carwash.data.CsvRepository;
import carwash.logic.CustomerService;
import carwash.model.Role;
import carwash.model.User;

public class TesHoliday {
public static void main(String[] args) throws Exception {
        CsvRepository repo = new CsvRepository();
        CustomerService svc = new CustomerService(repo, repo.muatCustomers());

        User kasir   = new User("U002", "kasir", "x", Role.KASIR, "K");
        User manager = new User("U001", "manager", "x", Role.MANAGER, "M");

        System.out.println("Daftar 1: " + svc.daftar("Budi Santoso", "081234567890", kasir));
        System.out.println("Daftar 2: " + svc.daftar("Siti Aminah", "085711112222", kasir));

        coba(svc, "Telepon kembar", "Orang Lain", "081234567890", kasir);
        coba(svc, "Nama kosong", "  ", "081200000001", kasir);
        coba(svc, "Nama ada koma", "Budi, Jr", "081200000001", kasir);
        coba(svc, "Telepon huruf", "Andi", "08abc", kasir);
        coba(svc, "Telepon tak diawali 0", "Andi", "6281234567890", kasir);
        coba(svc, "Manager mendaftar", "Andi", "081200000001", manager);

        System.out.println("Cari 'budi'        : " + svc.cari("budi"));
        System.out.println("Cari '0857'        : " + svc.cari("0857"));
        System.out.println("Cari 'C002'        : " + svc.cari("C002"));
    }

    private static void coba(CustomerService svc, String label,
                             String nama, String tel, User u) {
        try {
            svc.daftar(nama, tel, u);
            System.out.println("GAGAL TES: " + label + " seharusnya ditolak!");
        } catch (Exception e) {
            System.out.println("OK  " + label + " -> " + e.getMessage());
        }
    }
}