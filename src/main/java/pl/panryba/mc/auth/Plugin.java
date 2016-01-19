package pl.panryba.mc.auth;

import com.avaje.ebean.EbeanServer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.panryba.mc.db.FishDbPlugin;

public class Plugin extends FishDbPlugin {
    
    private PluginApi api;

    private class HandleLimboTask implements Runnable {
        private final PluginApi api;

        private HandleLimboTask(PluginApi api) {
            this.api = api;
        }

        @Override
        public void run() {
            api.handleLimbo();
        }
        
    }
    
    @Override
    public void onEnable() {
        EbeanServer database = getCustomDatabase();
        FileConfiguration config = getConfig();
        
        String locale = config.getString("locale", "en");
        if(locale == null || locale.isEmpty()) {
            locale = "en";
        }
        
        YamlConfiguration defaultConfig = new YamlConfiguration();
        InputStream defaultStream = getResource("default_messages.yml");
        
        try {
            defaultConfig.load(defaultStream);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        YamlConfiguration messagesConfig = new YamlConfiguration();
        File messagesFile = new File(getDataFolder(), "messages_" + locale + ".yml");
        
        try {
            messagesConfig.load(messagesFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Messages messages = new Messages(messagesConfig, defaultConfig);

        this.api = new PluginApi(this, database, config, messages);
        
        LoginCommand loginCmd = new LoginCommand(api, messages);
        getCommand("login").setExecutor(loginCmd);
        
        RegisterCommand registerCmd = new RegisterCommand(api, messages);
        getCommand("register").setExecutor(registerCmd);
        
        ChangePasswordCommand changePassCmd = new ChangePasswordCommand(api, messages);
        getCommand("change_password").setExecutor(changePassCmd);

        PlayerListener playerListener = new PlayerListener(api);
        getServer().getPluginManager().registerEvents(playerListener, this);
        
        HandleLimboTask task = new HandleLimboTask(api);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 0, 20 * 10);
        
        for(Player player : getServer().getOnlinePlayers()) {
            this.api.joined(player);
        }
    }

    @Override
    public void onDisable() {
        for(Player player : getServer().getOnlinePlayers()) {
            try {
                api.unprotectInventory(player);
            } catch(Exception ex) {
                Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        api.reset();
    }
    

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = super.getDatabaseClasses();
       
        list.add(PlayerEntity.class);
        return list;
    }        
}
