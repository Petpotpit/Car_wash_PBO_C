/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.data;

import carwash.logic.AuthService;
import carwash.model.Role;
import carwash.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author argor
 */
public class CsvRepository {

    private static final Path DIR_DATA = Paths.get("data");
    private static final Path FILE_USERS = DIR_DATA.resolve("users.csv");


    public List<User> muatUsers() throws IOException {
        if (!Files.exists(FILE_USERS)) {
            List<User> seed = buatSeed();
            simpanUsers(seed);
            return seed;
        }

        List<User> hasil = new ArrayList<>();
        List<String> baris = Files.readAllLines(FILE_USERS, StandardCharsets.UTF_8);
        for (int i = 0; i < baris.size(); i++) {
            String line = baris.get(i).trim();
            if (line.isEmpty()) continue;
            try {
                String[] c = line.split(",", -1);
                if (c.length != 5) throw new IllegalArgumentException("jumlah kolom != 5");
                hasil.add(new User(c[0].trim(), c[1].trim(), c[2].trim(),
                        Role.valueOf(c[3].trim()), c[4].trim()));
            } catch (IllegalArgumentException e) {
                System.err.println("users.csv baris " + (i + 1) + " dilewati: " + e.getMessage());
            }
        }
        return hasil;
    }

    private List<User> buatSeed() {
        List<User> seed = new ArrayList<>();
        seed.add(new User("U001", "manager", AuthService.buatHash("manager123"),
                Role.MANAGER, "Pak Hendra"));
        seed.add(new User("U002", "kasir", AuthService.buatHash("kasir123"),
                Role.KASIR, "Budi Kasir"));
        return seed;
    }

    private void simpanUsers(List<User> users) throws IOException {
        Files.createDirectories(DIR_DATA);
        List<String> baris = new ArrayList<>();
        for (User u : users) {
            baris.add(String.join(",", u.getId(), u.getUsername(),
                    u.getPasswordHash(), u.getRole().name(), u.getNama()));
        }
        Files.write(FILE_USERS, baris, StandardCharsets.UTF_8);
    }
}