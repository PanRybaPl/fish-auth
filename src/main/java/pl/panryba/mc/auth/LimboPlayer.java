package pl.panryba.mc.auth;

import java.util.Date;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author PanRyba.pl
 */
public class LimboPlayer {
    private final Player player;
    private final long joinedAt;
    private final Location returnLocation;

    private LimboMode limboMode;
    
    public LimboPlayer(Player player, Location returnLocation) {
        this.limboMode = LimboMode.UNKNOWN;
        this.player = player;
        this.joinedAt = new Date().getTime();
        
        if(returnLocation != null) {
            this.returnLocation = returnLocation.clone();
        } else {
            this.returnLocation = null;
        }
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public boolean isOlderThan(long time) {
        Date now = new Date();
        return (now.getTime() - this.joinedAt) > time;
    }
    
    public Location getReturnLocation() {
        return this.returnLocation;
    }

    public void setMode(LimboMode mode) {
        this.limboMode = mode;
    }
    
    public LimboMode getMode() {
        return this.limboMode;
    }
}
