/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.lintx.yinwuchat.bungeecord.Yinwuchat;

/**
 *
 * @author jjcbw01
 */
public class PlayerUtil {
    public static ProxiedPlayer getPlayer(UUID uuid){
        ProxiedPlayer player = null;
        try {
            player = Yinwuchat.getPlugin().getProxy().getPlayer(uuid);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return player;
    }
    
    public static String getPlayerName(UUID uuid){
        String unknown = "Unknown Player";
        ProxiedPlayer player = getPlayer(uuid);
        if (player instanceof ProxiedPlayer) {
            return player.getDisplayName();
        }
        else{
            Map<String,Object> user = getUserFromSql(uuid, false);
            if (user==null) {
                return unknown;
            }
            return (String)user.get("display_name");
        }
    }
    
    public static void saveUserToSql(ProxiedPlayer player){
        try {
            UUID uuid = player.getUniqueId();
            String uuidStr = uuid.toString();
            String player_name = player.getDisplayName();
            Map<String,Object> user = getUserFromSql(uuid,false);
            if (user==null) {
                addUserToSql(uuidStr, player_name);
            }
            else{
                if (!(player_name.equalsIgnoreCase((String)user.get("display_name")))) {
                    updateUserToSql(uuidStr, player_name);
                }
            }
            
        } catch (Exception e) {
        }
    }
    
    private static void addUserToSql(String uuid,String player_name){
        String sql = "insert into `chat_users` (`uuid`,`display_name`) values(?,?)";
        Yinwuchat.getMySql().execute(sql, uuid,player_name);
    }
    
    private static void updateUserToSql(String uuid,String player_name){
        String sql = "update `chat_users` set display_name=? where uuid=?";
        Yinwuchat.getMySql().execute(sql, player_name, uuid);
    }
    
    public static Map<String,Object> getUserFromSql(UUID uuid){
        return getUserFromSql(uuid, true);
    }
    
    public static Map<String,Object> getUserFromSql(UUID uuid,Boolean autoSaveUser){
        String sql = "select * from `chat_users` where uuid=?";
        List<Map<String,Object>> list = Yinwuchat.getMySql().query(sql, uuid.toString());
        if (list != null && list.isEmpty()) {
            if (autoSaveUser) {
                try {
                    ProxiedPlayer player = Yinwuchat.getPlugin().getProxy().getPlayer(uuid);
                    saveUserToSql(player);
                    list = Yinwuchat.getMySql().query(sql, uuid.toString());
                    return list.get(0);
                } catch (Exception e) {
                    return null;
                }
            }
            else{
                return null;
            }
        }
        return list.get(0);
    }
}
