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
public class LoginCommand implements CommandExecutor {
    private final PluginApi api;
    private final Messages messages;
    
    public LoginCommand(PluginApi api, Messages messages) {
        this.api = api;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }
        
        if(args.length != 1) {
            return false;
        }
        
        Player player = (Player)sender;
        if(api.isAuthenticated(player)) {
            return true;
        }
               
        AuthResult authResult = this.api.authenticate(player, args[0]);
        
        if(authResult.isSuccess()) {
            player.sendMessage(messages.getLoggedIn());
            return true;
        }
        
        switch(authResult.getReason()) {
            case FAILED:
                player.sendMessage(messages.getLoginFailed());
                break;
            case INVALID:
                player.sendMessage(messages.getLoginInvalid());
                break;
        }
        
        return true;
    }
    
}
