/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carwash.logic;

import java.text.NumberFormat;
import java.util.Locale;
/**
 *
 * @author argor
 */
public class Rupiah {
    private static final NumberFormat FMT = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

    private Rupiah() {}

    public static String format(double nilai) {
        return "Rp " + FMT.format((long) nilai);
    }
}
