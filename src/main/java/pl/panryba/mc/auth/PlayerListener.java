/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.auth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

/**
 *
 * @author PanRyba.pl
 */
class PlayerListener implements Listener {

    private PluginApi api;

    public PlayerListener(PluginApi api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        Location loginLocation = api.getLoginLocation(player);

        boolean teleport = !player.getWorld().equals(loginLocation.getWorld());

        if (!teleport) {
            double spawnDistance = player.getLocation().distance(loginLocation);
            teleport = spawnDistance > 1.0d;
        }

        if(!teleport)
            return;
        
        Location playerLocation = player.getLocation();
        Location tpLocation = new Location(loginLocation.getWorld(), loginLocation.getX(), loginLocation.getY(), loginLocation.getZ(),
                playerLocation.getYaw(), playerLocation.getPitch());

        player.teleport(tpLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {        
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {        
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }
            
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }            

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerUnleash(PlayerUnleashEntityEvent event) {
        Player player = event.getPlayer();
        
        if(api.isAuthenticated(player)) {
            return;
        }
        
        event.setCancelled(true);
    }            
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().split(" ")[0];
        String[] allowedCmds = {
            "/login", "/register",
            "/loguj", "/zaloguj",
            "/logowanie", "/rejestruj",
            "/zarejestruj", "/rejestracja"
        };

        for(String allowedCmd : allowedCmds) {
            if(cmd.equalsIgnoreCase(allowedCmd)) {
                return;
            }
        }
        
        Player player = event.getPlayer();
        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR) {
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        }

        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setResult(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatMonitor(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChatHigh(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChatHighes(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChatLow(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChatLowest(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChatNormal(AsyncPlayerChatEvent event) {
        handleChatEvent(event);
    }

    private void handleChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinLowest(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        api.protectInventory(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        api.joined(player);
    }
    
    @EventHandler
    public void onPlayerQuitLowest(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        api.unprotectInventory(player);
    }    

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        api.quit(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (api.isAuthenticated(player)) {
            return;
        }

        player.setFireTicks(0);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getTarget();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setTarget(null);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityInteractMonitor(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractLowest(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();

        if (api.isAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        AllowPlayerReason result = api.allowPlayerLogin(player);
        if(result == AllowPlayerReason.ALLOWED) {
            return;
        }
        
        switch(result) {
            case ALREADY_PLAYING:
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Gracz z podanym nickiem juz jest zalogowany");
                break;
        }
   }
}
