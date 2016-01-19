/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import com.avaje.ebean.EbeanServer;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author PanRyba.pl
 */

@Entity
@Table(name = "players")
public class PlayerEntity {
    
    static PlayerEntity find(EbeanServer database, String name) {
        return database.find(PlayerEntity.class).where().eq("name", name).findUnique();
    }
    
    static PlayerEntity find(EbeanServer database, String name, String passwordHash)
    {
        return database.find(PlayerEntity.class).where().eq("name", name).eq("password_hash", passwordHash).findUnique();
    }

    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "premium")
    private Boolean premium;
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId()
    {
        return this.id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setPremium(Boolean premium) {
        this.premium = premium;
    }
    
    public Boolean getPremium()
    {
        return this.premium;
    }
}
