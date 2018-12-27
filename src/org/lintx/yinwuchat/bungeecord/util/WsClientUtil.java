/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.UUID;

/**
 *
 * @author jjcbw01
 */
public class WsClientUtil {
    private UUID uuid;
    private String token;
    
    public WsClientUtil(String token,UUID uuid){
        this.token = token;
        this.uuid = uuid;
    }
    
    public WsClientUtil(String token){
        this.token = token;
        this.uuid = null;
    }
    
    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }
    
    public UUID getUuid(){
        return uuid;
    }
    
    public String getToken(){
        return token;
    }
}
