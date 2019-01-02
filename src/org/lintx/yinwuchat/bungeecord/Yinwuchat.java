/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Date;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.lintx.yinwuchat.bungeecord.util.Chat2SqlUtil;
import org.lintx.yinwuchat.bungeecord.util.ChatUtil;
import org.lintx.yinwuchat.bungeecord.util.WsClientHelper;

/**
 *
 * @author jjcbw01
 */
public class Yinwuchat extends Plugin{
    private static Yinwuchat plugin;
    private static WSServer server;
    private static MySql sql;
    private static WsClientHelper clients;
    private static int token_expire_time = 0;
    private static int token_max_count = 0;
    private Configuration config = null;
    
    public static Yinwuchat getPlugin(){
        return plugin;
    }
    
    public static WsClientHelper getClientHelper(){
        return clients;
    }
    
    public static MySql getMySql(){
        return sql;
    }
    
    public static WSServer getWSServer(){
        return server;
    }
    
    public static int getExpireTime(){
        return token_expire_time;
    }
    
    public static int getMaxToken(){
        return token_max_count;
    }
    
    private void loadConfig(){
        saveDefaultConfig();
        stopWsServer();
        
        int port = getConfig().getInt("websocket.port", 8888);
        try {
            server = new WSServer(port);
            server.start();
            getLogger().info("WebSocket started on port:" + server.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        token_expire_time = getConfig().getInt("token.expire_time",1296000) * 1000;
        token_max_count = getConfig().getInt("token.player_max_count",5);
        
        setSql();
        setChatConfig();
        
        clearExpireToken();
    }
    
    private void clearExpireToken(){
        getLogger().info("clear expire token");
        Date now = new Date();
        Timestamp timestamp = new Timestamp(now.getTime() - token_expire_time);
        sql.execute("delete from `chat_token` where time<?", timestamp);
    }
    
    public void reloadConf(){
        getLogger().info("reload config");
        config = null;
        loadConfig();
    }
    
    private void setSql(){
        String host = getConfig().getString("mysql.host");
        int port = getConfig().getInt("mysql.port");
        String database = getConfig().getString("mysql.database");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        sql = null;
        
        sql = new MySql(host,port,database,username,password);
    }
    
    private void setChatConfig(){
        ChatUtil.setPrefix(getConfig().getString("message.prefix"));
        ChatUtil.setSuffix(getConfig().getString("message.suffix"));
        ChatUtil.setIdentification(getConfig().getString("message.identification.text"));
        ChatUtil.setTooltip(getConfig().getString("message.identification.tooltips"));
        ChatUtil.setUrl(getConfig().getString("message.identification.click_url"));
        ChatUtil.setSeparator(getConfig().getString("message.separator"));
        ChatUtil.setSeparator(getConfig().getString("message.private_message_separator"));
        ChatUtil.setInterval(getConfig().getInt("message.interval",1000));
        
        ChatUtil.setJoinNameColor(getConfig().getString("message.joinmessage.player_name_color"));
        ChatUtil.setJoinMessage(getConfig().getString("message.joinmessage.message"));
        ChatUtil.setLeaveNameColor(getConfig().getString("message.leavemessage.player_name_color"));
        ChatUtil.setLeaveMessage(getConfig().getString("message.leavemessage.message"));
        
        Chat2SqlUtil.setExpireDay(getConfig().getInt("message.offline_message_expire", 0));
    }
    
    private void stopWsServer(){
        try {
            if (server instanceof WSServer) {
                getLogger().info("WebSocket stoped");
                server.stop();
                server = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
    @Override
    public void onEnable(){
        plugin = this;
        
        loadConfig();
        
        getProxy().getPluginManager().registerCommand(this, new ChatCommand("yinwuchat"));
        getProxy().getPluginManager().registerListener(this, new YinwuChatEvent());
    }
    
    @Override
    public void onDisable(){
        stopWsServer();
    }
    
    private void saveDefaultConfig(){
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                InputStream in = getResourceAsStream("config.yml");
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Configuration getConfig(){
        if (config == null) {
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(),"config.yml"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return config;
    }
}
