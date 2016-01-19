/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PanRyba.pl
 */
public class RegisterCommand implements CommandExecutor {
    private PluginApi api;
    private Messages messages;
    
    public RegisterCommand(PluginApi api, Messages messages) {
        this.api = api;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!(cs instanceof Player)) {
            return false;
        }
        
        if(strings.length == 2) {
            if(!strings[0].equals(strings[1])) {
                cs.sendMessage(this.messages.getPasswordConfirmationMismatch());
                return true;
            }
        } else if(strings.length != 1) {
            return false;
        }
        
        Player player = (Player)cs;
        
        if(api.isAuthenticated(player)) {
            player.sendMessage(this.messages.getRegistrationAlreadyAuthenticated());//ChatColor.YELLOW + "Nie musisz sie rejestrowac poniewaz juz jestes zalogowany.");
            return true;
        }
        
        if(api.isAwaitingLogin(player)) {
            player.sendMessage(this.messages.getRegistrationAwaitingLogin());//ChatColor.YELLOW + "Juz jestes zarejestrowany. Zaloguj sie - /login <haslo>");
            return true;
        }
        
        if(!api.isAwaitingRegistration(player)) {
            player.sendMessage(this.messages.getRegistrationNotAwaiting());//ChatColor.YELLOW + "Nie mozesz sie teraz zarejestrowac");
            return true;
        }
        
        player.sendMessage(this.messages.getRegistrationStarted());//ChatColor.GRAY + "Trwa rejestracja..");
        api.register(player, strings[0]);
        return true;
    }
    
}
