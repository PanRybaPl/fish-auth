/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

/**
 *
 * @author PanRyba.pl
 */
public class AuthResult {
    private final boolean success;
    private final AuthReason reason;
    
    public AuthResult(boolean success, AuthReason reason) {
        this.success = success;
        this.reason = reason;
    }
    
    public static AuthResult Success() {
        AuthResult result = new AuthResult(true, AuthReason.SUCCESS);
        return result;
    }
    
    public static AuthResult Invalid() {
        AuthResult result = new AuthResult(false, AuthReason.INVALID);
        return result;
    }

    public static AuthResult Failed() {
        AuthResult result = new AuthResult(false, AuthReason.FAILED);
        return result;
    }
    
    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the reason
     */
    public AuthReason getReason() {
        return reason;
    }
    
    
}
