/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import org.java_websocket.WebSocket;
import org.lintx.yinwuchat.bungeecord.json.ServerMessageJSON;

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
    
    public static List<String> getAllPlayer(){
        List<String> list = new ArrayList<>();
        for (Map.Entry<WebSocket, WsClientUtil> entry : clients.entrySet()) {
            WsClientUtil value = entry.getValue();
            list.add(value.getPlayerName());
        }
        return list;
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
    
    public static WebSocket getWebSocketAsPlayerName(String name){
        for (Map.Entry<WebSocket, WsClientUtil> entry : clients.entrySet()) {
            WebSocket key = entry.getKey();
            WsClientUtil value = entry.getValue();
            if (value.getPlayerName().equalsIgnoreCase(name)) {
                return key;
            }
        }
        return null;
    }
    
    public static List<WebSocket> getWebSocket(UUID uuid){
        List<WebSocket> list = new ArrayList<>();
        for (Map.Entry<WebSocket, WsClientUtil> entry : clients.entrySet()) {
            WebSocket key = entry.getKey();
            WsClientUtil value = entry.getValue();
            if (value.getUuid().toString().equalsIgnoreCase(uuid.toString())) {
                list.add(key);
            }
        }
        return list;
    }
    
    public static void kickOtherWS(WebSocket ws,UUID uuid){
        List<WebSocket> oldSockets = WsClientHelper.getWebSocket(uuid);
        if (!oldSockets.isEmpty()) {
            for (int i = 0; i < oldSockets.size(); i++) {
                WebSocket oldSocket = oldSockets.get(i);
                if (oldSocket!=null && oldSocket instanceof WebSocket && oldSocket != ws) {
                    oldSocket.send(ServerMessageJSON.errorJSON("你的帐号已经在其他地方上线，你已经被踢下线").getJSON());
                    oldSocket.close(3000,"");
                    WsClientHelper.remove(oldSocket);
                }
            }
        }
    }
}
