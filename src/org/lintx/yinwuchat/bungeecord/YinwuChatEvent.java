/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.lintx.yinwuchat.bungeecord.json.SendMessage;


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
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer)event.getSender();
            player_name = player.getDisplayName();
        }
        SendMessage message = new SendMessage(player_name, chat);
        WSServer server = Yinwuchat.getWSServer();
        if (server!=null) {
            server.broadcast(message.getJSON());
        }
    }
}
