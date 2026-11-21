/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package carwash.main;

//import carwash.data.CsvRepository;
//import carwash.exception.InputTidakValidException;
//import carwash.exception.LoginGagalException;
//import carwash.logic.AuthService;
//import carwash.logic.Session;
//import carwash.model.User;
import carwash.logic.AppContext;
import carwash.ui.FormLogin;
import java.io.IOException;
import javax.swing.JOptionPane;

//import java.io.IOException;
/**
 *
 * @author argor
 */
public class Main {

   public static void main(String[] args) {
        try {
            AppContext.muat();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.awt.EventQueue.invokeLater(() -> new FormLogin().setVisible(true));

//
//        try {
//            AuthService auth = new AuthService(new CsvRepository().muatUsers());
//
//            // 1. Login benar
//            User m = auth.login("manager", "manager123");
//            System.out.println("OK  manager -> " + m.getRole() + " (" + m.getNama() + ")");
//            User k = auth.login("kasir", "kasir123");
//            System.out.println("OK  kasir   -> " + k.getRole() + " (" + k.getNama() + ")");
//
//            // 2. Password salah -> harus exception
//            coba(auth, "manager", "salah");
//            // 3. Username tidak ada -> pesan harus identik dengan no. 2
//            coba(auth, "hantu", "apa-saja");
//            // 4. Input kosong -> InputTidakValidException
//            coba(auth, "  ", "x");
//            coba(auth, "kasir", "");
//
//            // 5. Session
//            System.out.println("Sebelum login : " + Session.isLoggedIn());
//            Session.setUser(k);
//            System.out.println("Setelah login : " + Session.isLoggedIn()
//                    + " -> " + Session.getUser().getUsername());
//            Session.logout();
//            System.out.println("Setelah logout: " + Session.isLoggedIn());
//
//        } catch (IOException e) {
//            System.err.println("Gagal baca/tulis users.csv: " + e.getMessage());
//        } catch (InputTidakValidException | LoginGagalException e) {
//            System.err.println("TIDAK DIHARAPKAN: " + e.getMessage());
//        }
//    }
//
//    private static void coba(AuthService auth, String u, String p) {
//        try {
//            auth.login(u, p);
//            System.out.println("GAGAL TES: login(" + u + ") seharusnya ditolak!");
//        } catch (LoginGagalException e) {
//            System.out.println("OK  LoginGagal      : " + e.getMessage());
//        } catch (InputTidakValidException e) {
//            System.out.println("OK  InputTidakValid : " + e.getMessage());
//        }
    }
//    akun admin 
//username = manager
//password = manager123
    
//    akun kasir
//username = kasir
//password = kasir123
}
