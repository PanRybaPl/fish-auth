package pl.panryba.mc.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PanRyba.pl
 */
public class ChangePasswordCommand implements CommandExecutor {
    private PluginApi api;
    private Messages messages;
    
    public ChangePasswordCommand(PluginApi api, Messages messages) {
        this.api = api;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!(cs instanceof Player)) {
            return false;
        }
        
        if(strings.length != 2) {
            return false;
        }
        
        String oldPass = strings[0];
        String newPass = strings[1];
        
        if(oldPass.isEmpty() || newPass.isEmpty()) {
            return false;
        }
        
        Player player = (Player)cs;
        if(!api.isAuthenticated(player)) {
            this.messages.getCannotChangePassNotAuth();
        }
        
        api.beginChangePass(player, oldPass, newPass);
        return true;
    }
    
}
