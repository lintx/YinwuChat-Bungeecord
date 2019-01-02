/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.lintx.yinwuchat.bungeecord.MySql;
import org.lintx.yinwuchat.bungeecord.Yinwuchat;

/**
 *
 * @author jjcbw01
 */
public class Chat2SqlUtil {
    private static int offline_message_expire = 0;
    public static void setExpireDay(int expire){
        offline_message_expire = expire;
    }
    
    public static int getExpireDay(){
        return offline_message_expire;
    }
    
    public static int newMessage(UUID player_uuid,String server_name,String message){
        int player_id = 0;
        try {
            Map<String,Object> playerUser = PlayerUtil.getUserFromSql(player_uuid, false);
            if (playerUser!=null) {
                player_id = (int)playerUser.get("id");
            }
        } catch (Exception e) {
        }
        if (player_id==0) {
            return -1;
        }
        return newMessage(player_id, 0, server_name, message);
    }
    
    public static int newMessage(UUID player_uuid,UUID to_player_uuid,String server_name,String message){
        int player_id = 0;
        int to_player_id = 0;
        try {
            Map<String,Object> playerUser = PlayerUtil.getUserFromSql(player_uuid, false);
            if (playerUser!=null) {
                player_id = (int)playerUser.get("id");
            }
            Map<String,Object> to_playerUser = PlayerUtil.getUserFromSql(to_player_uuid, false);
            if (to_playerUser!=null) {
                to_player_id = (int)to_playerUser.get("id");
            }
        } catch (Exception e) {
        }
        if (player_id==0 || to_player_id==0) {
            return -1;
        }
        return newMessage(player_id, to_player_id, server_name, message);
    }
    
    private static int newMessage(int player_id,int to_player_id,String server_name,String message){
        MySql mysql = Yinwuchat.getMySql();
        String sql = "insert into `chat_message` (`player_id`,`to_player_id`,`server`,`message`) values(?,?,?,?)";
        return mysql.insert(sql, player_id,to_player_id,server_name,message);
    }
}
