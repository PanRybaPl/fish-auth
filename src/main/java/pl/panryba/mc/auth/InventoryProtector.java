/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PanRyba.pl
 */
public class InventoryProtector {
    private final Map<Player, ProtectedInventory> protections;
    
    public InventoryProtector() {
        this.protections = new HashMap<>();
    }
    
    public void protect(Player player) {
        synchronized(this.protections) {
            if(this.protections.containsKey(player)) {
                Bukkit.getLogger().log(Level.INFO, "{0} inventory is already protected", player.getName());
                return;
            }
            
            ProtectedInventory protectedInv = new ProtectedInventory(player);
            this.protections.put(player, protectedInv);
            
            player.getInventory().setArmorContents(null);
            player.getInventory().setContents(new ItemStack[0]);
        }
    }
    
    public void unprotect(Player player) {
        synchronized(this.protections) {
            if(!this.protections.containsKey(player)) {
                return;
            }
            
            ProtectedInventory protectedInv = this.protections.remove(player);
            
            player.getInventory().setArmorContents(protectedInv.getArmor());
            player.getInventory().setContents(protectedInv.getInventory());
        }
    }
}
