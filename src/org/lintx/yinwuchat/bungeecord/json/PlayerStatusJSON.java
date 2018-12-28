/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Date;

/**
 *
 * @author jjcbw01
 */
public class PlayerStatusJSON {
    public enum PlayerStatus{
        JOIN,LEAVE,SWITCH_SERVER
    }
    
    private String player_name;
    private PlayerStatus status;
    private String server_name;
    
    public PlayerStatusJSON(String player_name,String server_name,PlayerStatus status){
        this.player_name = player_name;
        this.server_name = server_name;
        this.status = status;
    }
    
    public String getJSON(){
        JsonObject json = new JsonObject();
        String action = "";
        switch (status){
            case JOIN:
                action = "player_join";
                break;
            case LEAVE:
                action = "player_leave";
                break;
            case SWITCH_SERVER:
                action = "player_switch_server";
                break;
        }
        json.addProperty("action", action);
        json.addProperty("player", player_name);
        json.addProperty("server", server_name);
        json.addProperty("time", new Date().getTime());
        return new Gson().toJson(json);
    }
}
