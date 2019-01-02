/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author jjcbw01
 */
public class WsClientUtil {
    private UUID uuid;
    private String token;
    private Date lastDate;
    private String player_name;
    
    public WsClientUtil(String token,UUID uuid){
        this.token = token;
        this.uuid = uuid;
        this.lastDate = new Date();
        if (uuid!=null) {
            player_name = PlayerUtil.getPlayerName(uuid);
        }
    }
    
    public WsClientUtil(String token){
        this(token, null);
    }
    
    public void setUUID(UUID uuid){
        this.uuid = uuid;
        if (uuid!=null) {
            player_name = PlayerUtil.getPlayerName(uuid);
        }
    }
    
    public UUID getUuid(){
        return uuid;
    }
    
    public String getToken(){
        return token;
    }
    
    public Date getLastDate(){
        return lastDate;
    }
    
    public void updateLastDate(){
        lastDate = new Date();
    }
    
    public String getPlayerName(){
        return player_name;
    }
}
