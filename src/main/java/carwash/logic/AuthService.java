package carwash.logic;

import carwash.exception.InputTidakValidException;
import carwash.exception.LoginGagalException;
import carwash.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;

public class AuthService {

    private static final String PESAN_GAGAL = "Username atau password salah";

    // key = username
    private final HashMap<String, User> users = new HashMap<>();

    public AuthService(List<User> daftarUser) {
        for (User u : daftarUser) {
            users.putIfAbsent(u.getUsername(), u); // username unik: duplikat diabaikan
        }
    }

    /**
     * @return User jika berhasil
     * @throws InputTidakValidException jika username/password kosong
     * @throws LoginGagalException      jika username tidak ada ATAU password salah
     *                                  (pesan sama agar tidak membocorkan username)
     */
    public User login(String username, String password)
            throws InputTidakValidException, LoginGagalException {

        if (username == null || username.trim().isEmpty()) {
            throw new InputTidakValidException("Username tidak boleh kosong");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InputTidakValidException("Password tidak boleh kosong");
        }

        User user = users.get(username.trim());
        if (user == null || !verifikasi(password, user.getPasswordHash())) {
            throw new LoginGagalException(PESAN_GAGAL);
        }
        return user;
    }

    /** Untuk laporan/struk nanti: cari nama kasir berdasarkan id. */
    public User cariById(String id) {
        for (User u : users.values()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    // ---------- Hash: SHA-256 + salt ----------

    private static String hashDenganSalt(String password, String saltHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest((saltHex + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String buatHash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        return saltHex + ":" + hashDenganSalt(password, saltHex);
    }

    public static boolean verifikasi(String password, String tersimpan) {
        if (tersimpan == null) return false;
        String[] p = tersimpan.split(":");
        if (p.length != 2) return false; // format rusak -> anggap tidak cocok
        byte[] a = hashDenganSalt(password, p[0]).getBytes(StandardCharsets.UTF_8);
        byte[] b = p[1].getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b); // perbandingan waktu-konstan
    }
}