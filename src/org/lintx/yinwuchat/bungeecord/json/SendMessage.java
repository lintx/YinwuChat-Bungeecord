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
public class SendMessage {
    private String action = "send_message";
    private String player = "";
    private String message = "";
    
    public SendMessage(String player,String message){
        this.player = player;
        this.message = message;
    }
    
    public String getJSON(){
        JsonObject json = new JsonObject();
        json.addProperty("action", action);
        json.addProperty("player", player);
        json.addProperty("message", message);
        json.addProperty("time", new Date().getTime());
        return new Gson().toJson(json);
    }
}
