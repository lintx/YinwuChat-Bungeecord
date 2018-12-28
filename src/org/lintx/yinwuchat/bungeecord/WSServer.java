/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import java.net.InetSocketAddress;
import java.util.UUID;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.lintx.yinwuchat.bungeecord.json.BaseInputJSON;
import org.lintx.yinwuchat.bungeecord.json.InputCheckToken;
import org.lintx.yinwuchat.bungeecord.json.InputMessage;
import org.lintx.yinwuchat.bungeecord.json.SendMessage;
import org.lintx.yinwuchat.bungeecord.util.ChatUtil;
import org.lintx.yinwuchat.bungeecord.util.PlayerUtil;
import org.lintx.yinwuchat.bungeecord.util.WsClientHelper;
import org.lintx.yinwuchat.bungeecord.util.WsClientUtil;

/**
 *
 * @author jjcbw01
 */
public class WSServer extends WebSocketServer {

    public WSServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
        
    }

    @Override
    public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        //Yinwuchat.getPlugin().getLogger().info("ws on close");
        WsClientHelper.remove(conn);
    }

    @Override
    public void onMessage(org.java_websocket.WebSocket conn, String message) {
        BaseInputJSON object = BaseInputJSON.getObject(message);
        if (object instanceof InputCheckToken) {
            InputCheckToken o = (InputCheckToken)object;
            
            WsClientUtil clientUtil = new WsClientUtil(o.getToken());
            
            conn.send(o.getJSON());
            if (!o.getIsvaild()) {
                conn.send(o.getTokenJSON());
            }
            else{
                if (o.getIsbind()) {
                    clientUtil.setUUID(o.getUuid());
                }
            }
            WsClientHelper.add(conn, clientUtil);
        }
        else if (object instanceof InputMessage) {
            InputMessage o = (InputMessage)object;
            WsClientUtil util = WsClientHelper.get(conn);
            if (util instanceof WsClientUtil && util.getUuid() instanceof UUID) {
                Yinwuchat.getPlugin().getProxy().broadcast(ChatUtil.formatMessage(util.getUuid(), o.getMessage()));
                
                //转发消息给其他webclient
                String player_name = PlayerUtil.getPlayerName(util.getUuid());
                String server_name = "WebClient";
                SendMessage sendmessage = new SendMessage(player_name, o.getMessage(),server_name);
                WSServer server = Yinwuchat.getWSServer();
                if (server!=null) {
                    server.broadcast(sendmessage.getJSON());
                }
            }
        }
    }

    @Override
    public void onError(org.java_websocket.WebSocket conn, Exception ex) {
        Yinwuchat.getPlugin().getLogger().info("ws on error");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Yinwuchat.getPlugin().getLogger().info("ws on start");
    }
    
    
}
