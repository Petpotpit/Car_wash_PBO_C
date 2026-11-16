/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import carwash.model.User;
/**
 *
 * @author argor
 */
public class Session {
    private static User user;

    private Session() {}

    public static void setUser(User u) { user = u; }
    public static User getUser() { return user; }
    public static boolean isLoggedIn() { return user != null; }
    public static void logout() { user = null; }
}
