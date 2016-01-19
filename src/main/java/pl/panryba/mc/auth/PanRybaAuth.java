/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import com.avaje.ebean.EbeanServer;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author PanRyba.pl
 */
public class PanRybaAuth {
    private final String baseUrl;
    private final EbeanServer database;
    
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }    
    
    public PanRybaAuth(EbeanServer database, String baseUrl) {
        this.database = database;
        this.baseUrl = baseUrl;
    }
    
    public boolean register(String username, String password) throws IOException {
        String encodedUser = URLEncoder.encode(username, "UTF-8");
        String encodedPass = URLEncoder.encode(password, "UTF-8");

        String result = UrlHelper.getUrl(baseUrl + "register?name=" + encodedUser + "&pass=" + encodedPass);
        return result.equals("OK");
    }
    
    public boolean auth(String username, String password) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));
        
        String passHash = bytesToHex(hashBytes);
        return PlayerEntity.find(this.database, username, passHash) != null;        
    }
    
    public PremiumResult checkPremium(String username) throws IOException {
        PlayerEntity entity = PlayerEntity.find(this.database, username);
        if(entity == null) {
            return PremiumResult.UNKNOWN;
        }
        
        if(entity.getPremium() == null) {
            return PremiumResult.UNKNOWN;
        }
        
        return entity.getPremium() ?
                PremiumResult.PREMIUM :
                PremiumResult.NON_PREMIUM;
    }

    public ChangePasswordResult changePassword(String username, String oldPass, String newPass) throws IOException {
        String encodedUser = URLEncoder.encode(username, "UTF-8");
        String encodedOldPass = URLEncoder.encode(oldPass, "UTF-8");
        String encodedNewPass = URLEncoder.encode(newPass, "UTF-8");
        
        String result = UrlHelper.getUrl(baseUrl + "change_password?name=" + encodedUser + "&old=" + encodedOldPass + "&new=" + encodedNewPass);
        
        switch(result) {
            case "OK":
                return ChangePasswordResult.CHANGED;
            case "INVALID":
                return ChangePasswordResult.INVALID;
        }
        
        return ChangePasswordResult.FAIL;
    }
}
