/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PanRyba.pl
 */
public class ProtectedInventory {
    private final ItemStack[] armor;
    private final ItemStack[] inventory;
    
    public ProtectedInventory(Player player) {
        this.armor = player.getInventory().getArmorContents();
        this.inventory = player.getInventory().getContents();
    }
    
    public ItemStack[] getArmor() {
        return this.armor;
    }
    
    public ItemStack[] getInventory() {
        return this.inventory;
    }
}
