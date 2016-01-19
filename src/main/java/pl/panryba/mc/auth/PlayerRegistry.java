/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

public class PlayerRegistry {
    private final Map<String, Set<String>> registry;
    
    public PlayerRegistry() {
        this.registry = new HashMap<>();
    }
    
    public Set<String> Add(Player player) {
        String ip = getPlayerIp(player);
        
        synchronized(this.registry) {
            Set<String> ipNames = this.registry.get(ip);
            
            if(ipNames == null) {
                ipNames = new HashSet<>();
                this.registry.put(ip, ipNames);
            }
            
            ipNames.add(player.getName());
            return ipNames;
        }
    }
    
    public void Remove(Player player) {
        String ip = getPlayerIp(player);
        
        synchronized(this.registry) {
            Set<String> ipNames = this.registry.get(ip);
            
            if(ipNames == null) {
                return;
            }
            
            ipNames.remove(player.getName());
            if(ipNames.isEmpty()) {
                this.registry.remove(ip);
            }
        }
    }

    private String getPlayerIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }
}
