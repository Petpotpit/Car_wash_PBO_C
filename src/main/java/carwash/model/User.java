/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.model;

/**
 *
 * @author argor
 */
public class User {
    private final String id;
    private final String username;
    private final String passwordHash; // format salt:hash
    private final Role role;
    private final String nama;

    public User(String id, String username, String passwordHash, Role role, String nama) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.nama = nama;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getNama() { return nama; }

    @Override
    public String toString() {
        // sengaja tanpa passwordHash
        return id + " | " + username + " | " + role + " | " + nama;
    }
}
