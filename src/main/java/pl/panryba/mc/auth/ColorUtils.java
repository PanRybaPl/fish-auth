/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.auth;
import org.bukkit.ChatColor;

/**
 *
 * @author PanRyba.pl
 */
public class ColorUtils {
    public static String replaceColors(String message) {
        message = message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
        message = message.replaceAll("(?i)&r", ChatColor.RESET.toString());

        return message;
    }
    
}
