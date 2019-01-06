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
public class PrivateMessageJSON {
    private final String action = "private_message";
    private final String me_action = "me_private_message";
    private String player = "";
    private String to_player = "";
    private String message = "";
    private String server_name = "";
    
    public PrivateMessageJSON(String player,String to_player,String message,String server_name){
        this.player = player;
        this.message = message;
        this.server_name = server_name;
        this.to_player = to_player;
    }
    
    public String getJSON(int message_id){
        JsonObject json = new JsonObject();
        json.addProperty("action", action);
        json.addProperty("player", player);
        json.addProperty("server", server_name);
        json.addProperty("message", message);
        json.addProperty("time", new Date().getTime());
        json.addProperty("message_id", message_id);
        return new Gson().toJson(json);
    }
    
    public String getMeJSON(int message_id){
        JsonObject json = new JsonObject();
        json.addProperty("action", me_action);
        json.addProperty("player", to_player);
        json.addProperty("server", server_name);
        json.addProperty("message", message);
        json.addProperty("time", new Date().getTime());
        json.addProperty("message_id", message_id);
        return new Gson().toJson(json);
    }
}
