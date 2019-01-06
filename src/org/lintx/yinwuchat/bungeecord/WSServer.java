/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.lintx.yinwuchat.bungeecord.json.BaseInputJSON;
import org.lintx.yinwuchat.bungeecord.json.InputCheckToken;
import org.lintx.yinwuchat.bungeecord.json.InputMessage;
import org.lintx.yinwuchat.bungeecord.json.InputOfflineMessage;
import org.lintx.yinwuchat.bungeecord.json.PlayerListJSON;
import org.lintx.yinwuchat.bungeecord.json.PlayerStatusJSON;
import org.lintx.yinwuchat.bungeecord.json.PrivateMessageJSON;
import org.lintx.yinwuchat.bungeecord.json.SendMessage;
import org.lintx.yinwuchat.bungeecord.json.ServerMessageJSON;
import org.lintx.yinwuchat.bungeecord.util.Chat2SqlUtil;
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
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        PlayerListJSON.sendGamePlayerList();
        PlayerListJSON.sendWebPlayerList();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        //Yinwuchat.getPlugin().getLogger().info("ws on close");
        WsClientUtil util = WsClientHelper.get(conn);
        if (util instanceof WsClientUtil && util.getUuid() instanceof UUID) {
            Yinwuchat.getWSServer().broadcast((new PlayerStatusJSON(util.getPlayerName(),PlayerStatusJSON.PlayerStatus.WEB_JOIN)).getWebStatusJSON());
            Yinwuchat.getPlugin().getProxy().broadcast(ChatUtil.formatLeaveMessage(util.getUuid()));
        }
        WsClientHelper.remove(conn);
        PlayerListJSON.sendWebPlayerList();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
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
                    WsClientHelper.kickOtherWS(conn, o.getUuid());
                    String player_name = PlayerUtil.getPlayerName(o.getUuid());
                    Yinwuchat.getWSServer().broadcast((new PlayerStatusJSON(player_name,PlayerStatusJSON.PlayerStatus.WEB_JOIN)).getWebStatusJSON());
                    Yinwuchat.getPlugin().getProxy().broadcast(ChatUtil.formatJoinMessage(o.getUuid()));
                }
            }
            WsClientHelper.add(conn, clientUtil);
            PlayerListJSON.sendWebPlayerList();
        }
        else if (object instanceof InputMessage) {
            InputMessage o = (InputMessage)object;
            if (o.getMessage().equalsIgnoreCase("")) {
                return;
            }
            
            WsClientUtil util = WsClientHelper.get(conn);
            if (util instanceof WsClientUtil && util.getUuid() instanceof UUID) {
                long diff = new Date().getTime() - util.getLastDate().getTime();
                if (diff < ChatUtil.getInterval()) {
                    conn.send(ServerMessageJSON.errorJSON("发送消息过快（当前设置为每条消息之间最少间隔"+(ChatUtil.getInterval())+"毫秒）").getJSON());
                    return;
                }
                util.updateLastDate();
                int message_id = -1;
                
                if (o.getMessage().startsWith("/")) {
                    String[] command = o.getMessage().replaceFirst("^/", "").split("\\s");
                    if (command[0].equalsIgnoreCase("yinwuchat")) {
                        if (command.length>=4) {
                            if (command[1].equalsIgnoreCase("msg")) {
                                String to_player_name = command[2];
                                List<String> tmpList = new ArrayList<>();
                                for (int i = 3; i < command.length; i++) {
                                    tmpList.add(command[i]);
                                }
                                String msg = String.join(" ", tmpList);
                                
                                if (util.getPlayerName().equalsIgnoreCase(to_player_name)) {
                                    conn.send(ServerMessageJSON.errorJSON("你不能向自己发送私聊信息").getJSON());
                                    return;
                                }

                                boolean issend = false;
                                String server_name = "WebClient";
                                ProxiedPlayer toPlayer = Yinwuchat.getPlugin().getProxy().getPlayer(to_player_name);
                                if (toPlayer!=null && toPlayer instanceof ProxiedPlayer) {
                                    toPlayer.sendMessage(ChatUtil.formatPrivateMessage(util.getUuid(), msg));
                                    issend = true;
                                    message_id = Chat2SqlUtil.newMessage(util.getUuid(), toPlayer.getUniqueId(), server_name, msg);
                                }
                                WebSocket ws = WsClientHelper.getWebSocketAsPlayerName(to_player_name);
                                PrivateMessageJSON msgJSON = new PrivateMessageJSON(util.getPlayerName(),to_player_name, msg, server_name);
                                if (ws!=null && ws instanceof WebSocket) {
                                    if (!issend) {
                                        message_id = Chat2SqlUtil.newMessage(util.getUuid(), WsClientHelper.get(ws).getUuid(), server_name, msg);
                                    }
                                    ws.send(msgJSON.getJSON(message_id));
                                    issend = true;
                                }
                                if (!issend) {
                                    conn.send(ServerMessageJSON.errorJSON("玩家" + to_player_name + "不在线").getJSON());
                                }
                                else{
                                    conn.send(msgJSON.getMeJSON(message_id));
                                }
                            }
                            else{
                                conn.send(ServerMessageJSON.errorJSON("发送私聊消息的正确格式为/yinwuchat msg 玩家名 消息").getJSON());
                                conn.send(ServerMessageJSON.errorJSON("其他命令暂不支持").getJSON());
                            }
                        }
                        else{
                            conn.send(ServerMessageJSON.errorJSON("发送私聊消息的正确格式为/yinwuchat msg 玩家名 消息").getJSON());
                            conn.send(ServerMessageJSON.errorJSON("其他命令暂不支持").getJSON());
                        }
                    }
                    else{
                        conn.send(ServerMessageJSON.errorJSON("YinwuChat目前只支持/yinwuchat msg 命令").getJSON());
                    }
                    return;
                }
                Yinwuchat.getPlugin().getProxy().broadcast(ChatUtil.formatMessage(util.getUuid(), o.getMessage()));
                
                //转发消息给其他webclient
                String player_name = PlayerUtil.getPlayerName(util.getUuid());
                String server_name = "WebClient";
                SendMessage sendmessage = new SendMessage(player_name, o.getMessage(),server_name);
                message_id = Chat2SqlUtil.newMessage(util.getUuid(), server_name, o.getMessage());
                WSServer server = Yinwuchat.getWSServer();
                if (server!=null) {
                    server.broadcast(sendmessage.getJSON(message_id));
                }
            }
        }
        else if (object instanceof InputOfflineMessage) {
            WsClientUtil util = WsClientHelper.get(conn);
            if (util instanceof WsClientUtil && util.getUuid() instanceof UUID) {
                if (ChatUtil.getInterval()>0) {
                    long diff = new Date().getTime() - util.getLastDate().getTime();
                    if (diff < ChatUtil.getInterval()) {
                        conn.send(ServerMessageJSON.errorJSON("发送消息过快（当前设置为每条消息之间最少间隔"+(ChatUtil.getInterval())+"毫秒）").getJSON());
                        return;
                    }
                }
                
                util.updateLastDate();
                
                if (Chat2SqlUtil.getExpireDay()>0) {
                    try {
                        Date now = new Date();
                        Timestamp timestamp = new Timestamp(now.getTime() - Chat2SqlUtil.getExpireDay() * 24 * 60 * 60 * 1000);
                        Yinwuchat.getMySql().execute("delete from `chat_message` where time<?", timestamp);
                    } catch (Exception e) {
                    }
                }
                Map<String,Object> player = PlayerUtil.getUserFromSql(util.getUuid());
                if (player==null) {
                    return;
                }
                int player_id = (int)player.get("id");
                InputOfflineMessage o = (InputOfflineMessage)object;
                List<Map<String, Object>> messages = null;
                if (o.getLastId()>0) {
                    String sql = "select `chat_message`.*,`c1`.`display_name` as `player_name`,`c2`.`display_name` as `to_player_name` from `chat_message` LEFT JOIN `chat_users` `c1` ON `c1`.`id` = `chat_message`.`player_id` LEFT JOIN `chat_users` `c2` ON `c2`.`id` = `chat_message`.`to_player_id` where `chat_message`.`id` < ? and (`chat_message`.`to_player_id`=0 or `chat_message`.`to_player_id`=? or `chat_message`.`player_id`=?) order by `id` desc limit 50";
                    messages = Yinwuchat.getMySql().query(sql, o.getLastId(),player_id,player_id);
                }
                else if (o.getLastId()==0) {
                    String sql = "select `chat_message`.*,`c1`.`display_name` as `player_name`,`c2`.`display_name` as `to_player_name` from `chat_message` LEFT JOIN `chat_users` `c1` ON `c1`.`id` = `chat_message`.`player_id` LEFT JOIN `chat_users` `c2` ON `c2`.`id` = `chat_message`.`to_player_id` where `chat_message`.`to_player_id`=0 or `chat_message`.`to_player_id`=? or `chat_message`.`player_id`=? order by `id` desc limit 50";
                    messages = Yinwuchat.getMySql().query(sql, player_id,player_id);
                }
                if (messages==null || messages.isEmpty()) {
                    conn.send(ServerMessageJSON.errorJSON("已经获取了所有聊天记录",1001).getJSON());
                    return;
                }
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < messages.size(); i++) {
                    Map<String, Object> m = messages.get(i);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("message", (String)m.get("message"));
                    jsonObject.addProperty("server", (String)m.get("server"));
                    jsonObject.addProperty("message_id", (int)m.get("id"));
                    jsonObject.addProperty("time", ((Date)m.get("time")).getTime());
                    if ((int)m.get("to_player_id")==0) {
                        jsonObject.addProperty("action", "send_message");
                        jsonObject.addProperty("player", (String)m.get("player_name"));
                    }
                    else{
                        if ((int)m.get("player_id")==player_id) {
                            jsonObject.addProperty("action", "me_private_message");
                            jsonObject.addProperty("player", (String)m.get("to_player_name"));
                        }
                        else if ((int)m.get("to_player_id")==player_id) {
                            jsonObject.addProperty("action", "private_message");
                            jsonObject.addProperty("player", (String)m.get("player_name"));
                        }
                    }
                    jsonArray.add(jsonObject);
                }
                JsonObject resultJsonObject = new JsonObject();
                resultJsonObject.addProperty("action", "offline_message");
                resultJsonObject.add("messages", jsonArray);
                conn.send(new Gson().toJson(resultJsonObject));
            }
            
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Yinwuchat.getPlugin().getLogger().info("ws on error");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Yinwuchat.getPlugin().getLogger().info("ws on start");
    }
    
}