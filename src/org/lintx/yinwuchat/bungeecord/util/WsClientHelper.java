/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.HashMap;
import java.util.Map;
import org.java_websocket.WebSocket;

/**
 *
 * @author jjcbw01
 */
public class WsClientHelper {
    private static HashMap<WebSocket,WsClientUtil> clients = new HashMap<WebSocket,WsClientUtil>();
    
    public static void add(WebSocket socket,WsClientUtil client){
        remove(socket);
        clients.put(socket, client);
    }
    
    public static void remove(WebSocket socket){
        if (clients.containsKey(socket)) {
            clients.remove(socket);
        }
    }
    
    public static WsClientUtil get(WebSocket socket){
        WsClientUtil client = null;
        if (clients.containsKey(socket)) {
            client = clients.get(socket);
        }
        return client;
    }
    
    public static void clear(){
        clients.clear();
    }
    
    public static WebSocket getWebSocket(String token){
        for (Map.Entry<WebSocket, WsClientUtil> entry : clients.entrySet()) {
            WebSocket key = entry.getKey();
            WsClientUtil value = entry.getValue();
            if (value.getToken().equalsIgnoreCase(token)) {
                return key;
            }
        }
        return null;
    }
}
