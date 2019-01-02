/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.lintx.yinwuchat.bungeecord.WSServer;
import org.lintx.yinwuchat.bungeecord.Yinwuchat;
import org.lintx.yinwuchat.bungeecord.util.WsClientHelper;

/**
 *
 * @author jjcbw01
 */
public class PlayerListJSON {
    public static void sendGamePlayerList(){
        Collection<ProxiedPlayer> players = Yinwuchat.getPlugin().getProxy().getPlayers();
        JsonArray jsonArray = new JsonArray();
        for (ProxiedPlayer player : players) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("player_name", player.getDisplayName());
            String server_name = "";
            try {
                server_name = player.getServer().getInfo().getName();
            } catch (Exception e) {
            }
            jsonObject.addProperty("server_name", server_name);
            jsonArray.add(jsonObject);
        }
        JsonObject resultJsonObject = new JsonObject();
        resultJsonObject.addProperty("action", "game_player_list");
        resultJsonObject.add("player_list", jsonArray);
        String json = new Gson().toJson(resultJsonObject);
        WSServer server = Yinwuchat.getWSServer();
        if (server!=null) {
            server.broadcast(json);
        }
    }
    
    public static void sendWebPlayerList(){
        JsonArray jsonArray = new JsonArray();
        List<String> players = WsClientHelper.getAllPlayer();
        for (int i = 0; i < players.size(); i++) {
            jsonArray.add(players.get(i));
        }
        JsonObject resultJsonObject = new JsonObject();
        resultJsonObject.addProperty("action", "web_player_list");
        resultJsonObject.add("player_list", jsonArray);
        String json = new Gson().toJson(resultJsonObject);
        WSServer server = Yinwuchat.getWSServer();
        if (server!=null) {
            server.broadcast(json);
        }
    }
}
