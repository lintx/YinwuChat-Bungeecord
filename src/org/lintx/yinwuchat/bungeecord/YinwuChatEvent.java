/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.lintx.yinwuchat.bungeecord.json.PlayerStatusJSON;
import org.lintx.yinwuchat.bungeecord.json.SendMessage;
import org.lintx.yinwuchat.bungeecord.util.PlayerUtil;


/**
 *
 * @author jjcbw01
 */
public class YinwuChatEvent implements Listener{
    
    @EventHandler
    public void onChat(ChatEvent event){
        String chat = event.getMessage();
        if (chat.startsWith("/")) {
            return;
        }
        String player_name = "Unknown Player";
        String server_name = "";
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer)event.getSender();
            player_name = player.getDisplayName();
            
            server_name = player.getServer().getInfo().getName();
        }
        SendMessage message = new SendMessage(player_name, chat,server_name);
        WSServer server = Yinwuchat.getWSServer();
        if (server!=null) {
            server.broadcast(message.getJSON());
        }
    }
    
    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        if (event.getPlayer() instanceof ProxiedPlayer) {
            ProxiedPlayer player = event.getPlayer();
            PlayerUtil.saveUserToSql(player);
            
            String server_name = "";
            try {
                server_name = player.getServer().getInfo().getName();
            } catch (Exception e) {
            }
            PlayerStatusJSON obj = new PlayerStatusJSON(player.getDisplayName(), server_name, PlayerStatusJSON.PlayerStatus.JOIN);
            WSServer server = Yinwuchat.getWSServer();
            if (server!=null) {
                server.broadcast(obj.getJSON());
            }
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event){
        if (event.getPlayer() instanceof ProxiedPlayer) {
            ProxiedPlayer player = event.getPlayer();
            PlayerUtil.saveUserToSql(player);
            
            String server_name = "";
            try {
                server_name = player.getServer().getInfo().getName();
            } catch (Exception e) {
            }
            PlayerStatusJSON obj = new PlayerStatusJSON(player.getDisplayName(), server_name, PlayerStatusJSON.PlayerStatus.LEAVE);
            WSServer server = Yinwuchat.getWSServer();
            if (server!=null) {
                server.broadcast(obj.getJSON());
            }
        }
    }
    
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event){
        if (event.getPlayer() instanceof ProxiedPlayer) {
            ProxiedPlayer player = event.getPlayer();
            PlayerUtil.saveUserToSql(player);
            
            String server_name = "";
            try {
                server_name = player.getServer().getInfo().getName();
            } catch (Exception e) {
            }
            PlayerStatusJSON obj = new PlayerStatusJSON(player.getDisplayName(), server_name, PlayerStatusJSON.PlayerStatus.SWITCH_SERVER);
            WSServer server = Yinwuchat.getWSServer();
            if (server!=null) {
                server.broadcast(obj.getJSON());
            }
        }
    }
}
