/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author PanRyba.pl
 */
public class Messages {
    
    private final FileConfiguration config;
    private final FileConfiguration defaults;
    
    public Messages(FileConfiguration config, FileConfiguration defaults) {
        this.config = config;
        this.defaults = defaults;
    }
    
    private String getString(String name) {
        String template = this.config.getString(name);
        
        if(template == null) {
            template = this.defaults.getString(name);
        }
        
        return ColorUtils.replaceColors(template);
    }
    
    public String getLoggedIn() {
        return getString("logged_id");
    }

    String getLoginFailed() {
        return getString("login_failed");
    }

    String getLoginInvalid() {
        return getString("login_invalid");
    }

    String getLoginRequied() {
        return getString("login_required");
    }

    String getLoginTimeout() {
        return getString("login_timeout");
    }

    String getRegistrationRequired() {
        return getString("registration_required");
    }

    String getRegistrationFailed() {
        return getString("registration_failed");
    }

    String getRegistrationSuccess() {
        return getString("registration_success");
    }

    String getRegistrationAlreadyAuthenticated() {
        return getString("registration_already_auth");
    }

    String getRegistrationAwaitingLogin() {
        return getString("registration_awaiting_login");
    }

    String getRegistrationNotAwaiting() {
        return getString("registration_not_awaiting");
    }

    String getRegistrationStarted() {
        return getString("registration_started");
    }

    String getCannotChangePassNotAuth() {
        return getString("pass_change_not_auth");
    }

    String getPasswordChanged() {
        return getString("pass_changed");
    }

    String getInvalidPasswordChange() {
        return getString("pass_change_invalid");
    }

    String getFailedPasswordChange() {
        return getString("pass_change_failed");
    }
    
    String getPasswordChanging() {
        return getString("pass_changing");
    }

    String getPasswordConfirmationMismatch() {
        return getString("pass_confirmation_mismatch");
    }
}
