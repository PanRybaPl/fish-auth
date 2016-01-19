package pl.panryba.mc.auth;

import com.avaje.ebean.EbeanServer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author PanRyba.pl
 */
class PluginApi {
    private final ExecutorService asyncPool;
    private final Messages messages;
    private final Plugin plugin;
    private final EbeanServer database;

    private Set<Player> authenticated;
    private Map<Player, LimboPlayer> limbo;
    private InventoryProtector protector;
    private PlayerRegistry playerRegistry;    

    private PanRybaAuth authenticator;
    private Location loginLocation;
    private long maxLimboTime;
    private boolean useProtector;
    private boolean assumeOnlyPremium;

    public PluginApi(Plugin plugin, EbeanServer database, FileConfiguration config, Messages messages) {
        this.plugin = plugin;
        this.database = database;
        this.messages = messages;
        this.authenticator = null;
        this.playerRegistry = new PlayerRegistry();
        this.asyncPool = Executors.newFixedThreadPool(2);
        
        reset();

        reloadConfig(config);
    }

    AuthResult authenticate(Player player, String password) {
        if (this.isAuthenticated(player)) {
            return AuthResult.Success();
        }

        String username = player.getName();
        boolean result;

        try {
            result = this.authenticator.auth(username, password);
        } catch (Exception ex) {
            Bukkit.getLogger().info(ex.toString());
            return AuthResult.Failed();
        }

        if (!result) {
            return AuthResult.Invalid();
        }

        this.setAuthenticated(player);

        return AuthResult.Success();
    }

    void unauthenticate(Player player) {
        this.authenticated.remove(player);
    }

    boolean isAuthenticated(Player player) {
        return this.authenticated.contains(player);
    }

    private void reloadConfig(FileConfiguration config) {
        String baseUrl = config.getString("api_url");
        this.authenticator = new PanRybaAuth(this.database, baseUrl);

        ConfigurationSection loginLocSection = config.getConfigurationSection("login_location");
        this.loginLocation = readLocation(loginLocSection);

        this.maxLimboTime = config.getLong("max_limbo_seconds", 30) * 1000;
        this.useProtector = config.getBoolean("use_inventory_protector", false);
        this.assumeOnlyPremium = config.getBoolean("assume_only_premium", false);
    }

    Location getLoginLocation(Player player) {
        return this.loginLocation;
    }

    private void setAuthenticated(Player player) {
        this.authenticated.add(player);
        this.removeLimbo(player);
        this.unprotectInventory(player);
    }

    public PremiumResult checkPremium(Player player) {
        try {
            return this.authenticator.checkPremium(player.getName());
        } catch (IOException ex) {
            return PremiumResult.FAIL;
        }
    }

    void handleLimbo() {
        String loginMessage = this.messages.getLoginRequied();
        String registrationMessage = this.messages.getRegistrationRequired();

        Set<Player> toKick = new HashSet<>();
        for (LimboPlayer limboPlayer : this.limbo.values()) {
            Player player = limboPlayer.getPlayer();

            if (limboPlayer.isOlderThan(this.maxLimboTime)) {
                toKick.add(player);
                continue;
            }

            switch (limboPlayer.getMode()) {
                case LOGIN:
                    player.sendMessage(loginMessage);
                    break;
                case REGISTRATION:
                    player.sendMessage(registrationMessage);
                    break;
            }
        }

        String kickMessage = this.messages.getLoginTimeout();
        for (Player player : toKick) {
            player.kickPlayer(kickMessage);
        }
    }

    private void removeLimbo(Player player) {
        LimboPlayer limboPlayer = this.limbo.remove(player);

        if (limboPlayer != null) {
            Location afterAuthLocation = limboPlayer.getReturnLocation();

            if (afterAuthLocation != null) {
                player.teleport(afterAuthLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);
            }
        }
    }

    private void setLimboMode(Player player, LimboMode mode) {
        LimboPlayer limboPlayer = this.limbo.get(player);
        if (limboPlayer == null) {
            return;
        }

        limboPlayer.setMode(mode);
    }

    boolean isAwaitingLogin(Player player) {
        LimboPlayer limboPlayer = this.limbo.get(player);
        if (limboPlayer == null) {
            return false;
        }

        return limboPlayer.getMode() == LimboMode.LOGIN;
    }

    boolean isAwaitingRegistration(Player player) {
        LimboPlayer limboPlayer = this.limbo.get(player);
        if (limboPlayer == null) {
            return false;
        }

        return limboPlayer.getMode() == LimboMode.REGISTRATION;
    }

    private void handleRegistrationResult(Player player, boolean result) {
        if (!result) {
            player.sendMessage(this.messages.getRegistrationFailed());//ChatColor.RED + "Rejestracja nie powiodla sie. Sprobuj ponownie za chwile.");
            return;
        }

        player.sendMessage(this.messages.getRegistrationSuccess());//ChatColor.GREEN + "Zostales zarejestrowany i zalogowany. Milej gry!");
        setAuthenticated(player);

        triggerPlayerRegistered(player);
    }

    private void handlePasswordChangedResult(Player player, ChangePasswordResult result) {
        switch (result) {
            case CHANGED:
                player.sendMessage(this.messages.getPasswordChanged());
                break;
            case INVALID:
                player.sendMessage(this.messages.getInvalidPasswordChange());
                break;
            case FAIL:
                player.sendMessage(this.messages.getFailedPasswordChange());
                break;
        }
    }

    AllowPlayerReason allowPlayerLogin(Player player) {
        for (Player authPlayer : this.authenticated) {
            if (authPlayer.getName().equalsIgnoreCase(player.getName())) {
                return AllowPlayerReason.ALREADY_PLAYING;
            }
        }

        return AllowPlayerReason.ALLOWED;
    }

    private void triggerPlayerRegistered(Player player) {
        try {
            PlayerRegisteredEvent event = new PlayerRegisteredEvent(player);
            Bukkit.getServer().getPluginManager().callEvent(event);
        } catch (IllegalStateException ex) {
            Logger.getLogger(PluginApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void protectInventory(Player player) {
        if(!this.useProtector) {
            return;
        }
        
        this.protector.protect(player);
    }

    void unprotectInventory(Player player) {
        if(!this.useProtector) {
            return;
        }
        
        this.protector.unprotect(player);
    }

    final void reset() {
        this.authenticated = new HashSet<>();
        this.limbo = new HashMap<>();
        this.protector = new InventoryProtector();
    }

    private class PlayerChangedPasswordTask implements Runnable {

        private final PluginApi api;
        private final Player player;
        private final ChangePasswordResult result;

        public PlayerChangedPasswordTask(PluginApi api, Player player, ChangePasswordResult result) {
            this.api = api;
            this.player = player;
            this.result = result;
        }

        @Override
        public void run() {
            api.handlePasswordChangedResult(player, result);
        }
    }

    private void handleChangePass(Player player, String oldPass, String newPass) {
        ChangePasswordResult result;
        try {
            result = this.authenticator.changePassword(player.getName(), oldPass, newPass);
        } catch (IOException ex) {
            result = ChangePasswordResult.FAIL;
        }

        Runnable task = new PlayerChangedPasswordTask(this, player, result);
        this.plugin.getServer().getScheduler().runTask(plugin, task);
    }

    private class PlayerChangePasswordTask implements Runnable {

        private final Player player;
        private final String oldPass;
        private final String newPass;
        private final PluginApi api;

        public PlayerChangePasswordTask(PluginApi api, Player player, String oldPass, String newPass) {
            this.api = api;
            this.player = player;
            this.oldPass = oldPass;
            this.newPass = newPass;
        }

        @Override
        public void run() {
            api.handleChangePass(player, oldPass, newPass);
        }
    }

    void beginChangePass(Player player, String oldPass, String newPass) {
        player.sendMessage(this.messages.getPasswordChanging());

        Runnable task = new PlayerChangePasswordTask(this, player, oldPass, newPass);
        this.asyncPool.execute(task);
        //this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    private class PlayerRegisterResultTask implements Runnable {

        private PluginApi api;
        private Player player;
        private boolean result;

        public PlayerRegisterResultTask(PluginApi api, Player player, boolean result) {
            this.api = api;
            this.player = player;
            this.result = result;
        }

        @Override
        public void run() {
            this.api.handleRegistrationResult(player, result);
        }
    }

    private void handlePlayerRegistration(Player player, String password) {
        boolean registered = false;

        try {
            registered = this.authenticator.register(player.getName(), password);
        } catch (IOException ex) {
            Logger.getLogger(PluginApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        Runnable task = new PlayerRegisterResultTask(this, player, registered);
        this.plugin.getServer().getScheduler().runTask(plugin, task);
    }

    private class PlayerRegisterTask implements Runnable {

        private String password;
        private Player player;
        private PluginApi api;

        public PlayerRegisterTask(PluginApi api, Player player, String password) {
            this.api = api;
            this.player = player;
            this.password = password;
        }

        @Override
        public void run() {
            this.api.handlePlayerRegistration(player, password);
        }
    }

    void register(Player player, String password) {
        Runnable task = new PlayerRegisterTask(this, player, password);
        this.asyncPool.execute(task);
        //this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    private class PlayerJoinedAsyncTask implements Runnable {

        private PluginApi api;
        private Player player;

        public PlayerJoinedAsyncTask(PluginApi api, Player player) {
            this.api = api;
            this.player = player;
        }

        @Override
        public void run() {
            api.handleAsyncJoined(player);
        }
    }

    private class PlayerCheckedTask implements Runnable {

        private PluginApi api;
        private Player player;
        private PremiumResult result;

        public PlayerCheckedTask(PluginApi api, Player player, PremiumResult result) {
            this.api = api;
            this.player = player;
            this.result = result;
        }

        @Override
        public void run() {
            api.handlePlayerChecked(player, result);
        }
    }

    private void handlePlayerChecked(Player player, PremiumResult result) {
        Bukkit.getLogger().info(player.getName() + ": " + result.toString());
        switch (result) {
            case PREMIUM:
                this.setAuthenticated(player);
                if (!player.hasPlayedBefore()) {
                    triggerPlayerRegistered(player);
                }
                break;
            case NON_PREMIUM:
                this.unauthenticate(player);
                player.sendMessage(this.messages.getLoginRequied());
                setLimboMode(player, LimboMode.LOGIN);
                break;
            case UNKNOWN:
                this.unauthenticate(player);
                player.sendMessage(this.messages.getRegistrationRequired());
                setLimboMode(player, LimboMode.REGISTRATION);
                break;
            case FAIL:
                player.kickPlayer(ChatColor.YELLOW + "Brak polaczenia z API PanRyba\n\nSprobuj ponownie za chwile");
                break;
        }
    }

    void handleAsyncJoined(Player player) {
        PremiumResult result;

        if(this.assumeOnlyPremium) {
            result = PremiumResult.PREMIUM;
        } else {
            result = checkPremium(player);
        }

        Runnable task = new PlayerCheckedTask(this, player, result);
        this.plugin.getServer().getScheduler().runTask(plugin, task);
    }

    void joined(Player player) {
        Set<String> names = this.playerRegistry.Add(player);
        
        if(names != null && names.size() > 1) {
            String namesStr = "";
            for(String name : names) {
                namesStr = namesStr + " " + name;
            }
            
            String msg = "SAME IP PLAYERS ONLINE - " + player.getAddress().getAddress().getHostAddress() + " :" + namesStr;
            Bukkit.getLogger().log(Level.INFO, msg);
        }
        
        Location lastLocation = getLogoutLocation(player);
        LimboPlayer limboPlayer = new LimboPlayer(player, lastLocation);

        this.limbo.put(player, limboPlayer);

        Location playerLoginLocation = getLoginLocation(player);
        player.teleport(playerLoginLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);

        Runnable task = new PlayerJoinedAsyncTask(this, player);
        this.asyncPool.execute(task);
        //this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    void quit(Player player) {
        this.playerRegistry.Remove(player);
        
        this.unauthenticate(player);
        this.removeLimbo(player);
    }

    private Location readLocation(ConfigurationSection section) {
        return new Location(
                Bukkit.getWorld(section.getString("world")),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch")
        );
    }

    private Location getLogoutLocation(Player player) {
        String name = player.getName().toLowerCase();
        YamlConfiguration config = new YamlConfiguration();

        File userDataFolder = new File(this.plugin.getDataFolder(), "../Essentials/userdata/" + name + ".yml");

        try {
            config.load(userDataFolder);
            ConfigurationSection section = config.getConfigurationSection("logoutlocation");

            return readLocation(section);
        } catch (FileNotFoundException ex) {
            // its not an exception if file not found (new player / no last logout location etc.)k
        } catch (Exception ex) {
            Logger.getLogger(PluginApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
