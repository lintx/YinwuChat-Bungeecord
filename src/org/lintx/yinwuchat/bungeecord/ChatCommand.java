/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.java_websocket.WebSocket;
import org.lintx.yinwuchat.bungeecord.json.InputCheckToken;
import org.lintx.yinwuchat.bungeecord.util.PlayerUtil;
import org.lintx.yinwuchat.bungeecord.util.WsClientHelper;

/**
 *
 * @author jjcbw01
 */
public class ChatCommand extends Command{

    public ChatCommand(String name) {
        super(name);
    }
    
    
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length>=1 && strings[0].equalsIgnoreCase("reload")) {
            if (commandSender instanceof ProxiedPlayer) {
                if (((ProxiedPlayer)commandSender).hasPermission("yinwuchat.reload")) {
                    commandSender.sendMessage(buildMessage(ChatColor.GREEN + "YinwuChat插件重载"));
                    Yinwuchat.getPlugin().reloadConf();
                }
                else{
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "权限不足"));
                    return;
                }
            }
            else{
                Yinwuchat.getPlugin().reloadConf();
            }
            return;
        }
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(buildMessage("Must use command in-game"));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer)commandSender;
        final UUID playerUUID = player.getUniqueId();
        if (playerUUID==null) {
            Yinwuchat.getPlugin().getLogger().info("Player " + commandSender.getName() + "has a null UUID");
            commandSender.sendMessage(buildMessage(ChatColor.RED + "You can't use that command right now. (No UUID)"));
            return;
        }
        if (strings.length>=1) {
            String first = strings[0];
            if (first.equalsIgnoreCase("bind")) {
                if (strings.length>=2) {
                    String token = strings[1];
                    String title = "";
                    if (strings.length>=3) {
                        title = strings[2];
                    }
                    InputCheckToken tokenObj = new InputCheckToken(token,false);
                    if (!tokenObj.getIsvaild()) {
                        commandSender.sendMessage(buildMessage(ChatColor.RED + tokenObj.getMessage() + "（token从网页客户端获取）"));
                    }
                    else{
                        if (tokenObj.getIsbind()) {
                            commandSender.sendMessage(buildMessage(ChatColor.RED + "该token已绑定，不能重复绑定"));
                        }
                        else{
                            MySql mysql = Yinwuchat.getMySql();
                            String sql = "";
                            Map<String,Object> userMap = PlayerUtil.getUserFromSql(playerUUID);
                            if (userMap==null) {
                                commandSender.sendMessage(buildMessage(ChatColor.RED + "绑定失败，你可以重试几次，如果持续失败，请联系OP，错误代码：001"));
                                return;
                            }
                            autoDeleteExpireToken(playerUUID);
                            List<Map<String,Object>> tokens = getTokens(playerUUID);
                            if (tokens.size()>=Yinwuchat.getMaxToken()) {
                                commandSender.sendMessage(buildMessage(ChatColor.RED + "你绑定的token已经达到上限（"+Yinwuchat.getMaxToken()+"），无法继续绑定token"));
                                commandSender.sendMessage(buildMessage(ChatColor.RED + "你也可以使用/yinwuchat unbind命令解绑一些token之后再试，输入/yinwuchat help 查看帮助"));
                                return;
                            }
                            
                            int user_id = (int)userMap.get("id");
                            sql = "update `chat_token` set user=?,title=? where token=?";
                            if (mysql.execute(sql, user_id,title,token)) {
                                commandSender.sendMessage(buildMessage(ChatColor.GREEN + "绑定成功"));
                                WebSocket ws = WsClientHelper.getWebSocket(token);
                                if (ws!=null && (ws instanceof WebSocket)) {
                                    WsClientHelper.get(ws).setUUID(playerUUID);
                                    ws.send((new InputCheckToken(token,false)).getJSON());
                                }
                            }
                            else{
                                commandSender.sendMessage(buildMessage(ChatColor.RED + "绑定失败，你可以重试几次，如果持续失败，请联系OP，错误代码：002"));
                            }
                        }
                    }
                }
                else{
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "命令格式：/yinwuchat token title"));
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "缺少token（token从网页客户端获取）"));
                }
                return;
            }
            else if (first.equalsIgnoreCase("list")) {
                autoDeleteExpireToken(playerUUID);
                List<Map<String,Object>> tokens = getTokens(playerUUID);
                if (tokens.isEmpty()) {
                    commandSender.sendMessage(buildMessage(ChatColor.GREEN + "你没有绑定任何token"));
                    return;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                commandSender.sendMessage(buildMessage(ChatColor.GREEN + "你一共绑定了"+tokens.size()+"个token，详情如下："));
                for (int i = 0; i < tokens.size(); i++) {
                    Map<String,Object> token = tokens.get(i);
                    String exptime = format.format(((Date)token.get("time")).getTime()+Yinwuchat.getExpireTime());
                    commandSender.sendMessage(buildMessage(ChatColor.GREEN + "id:" + (int)token.get("id") + "  过期时间:" + exptime + "  title:" + (String)token.get("title")));
                }
                return;
            }
            else if (first.equalsIgnoreCase("unbind")) {
                if (strings.length>=2) {
                    int id = Integer.parseInt(strings[1]);
                    
                    MySql mysql = Yinwuchat.getMySql();
                    String sql = "";
                    Map<String,Object> userMap = PlayerUtil.getUserFromSql(playerUUID);
                    if (userMap==null) {
                        commandSender.sendMessage(buildMessage(ChatColor.RED + "解绑出错，你可以重试几次，如果持续失败，请联系OP，错误代码：001"));
                        return;
                    }
                    List<Map<String,Object>> tokens = getTokens(playerUUID);
                    for (int i = 0; i < tokens.size(); i++) {
                        Map<String,Object> tokenMap = tokens.get(i);
                        if ((int)tokenMap.get("id")==id) {
                            sql = "delete from `chat_token` where id=?";
                            if (Yinwuchat.getMySql().execute(sql, id)) {
                                commandSender.sendMessage(buildMessage(ChatColor.GREEN + "解绑成功"));
                            }
                            else{
                                commandSender.sendMessage(buildMessage(ChatColor.RED + "解绑失败，你可以重试几次，如果持续失败，请联系OP，错误代码：003"));
                            }
                            return;
                        }
                    }
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "在你已绑定的token中没有找到对应的数据，解绑失败"));
                }
                else{
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "命令格式：/yinwuchat unbind id"));
                    commandSender.sendMessage(buildMessage(ChatColor.RED + "缺少id（id可以从/yinwuchat list中获取）"));
                }
                return;
            }
        }
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "YinwuChat Version "+ Yinwuchat.getPlugin().getDescription().getVersion() + ",Author:"+Yinwuchat.getPlugin().getDescription().getAuthor()));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "插件帮助："));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "绑定：/yinwuchat bind <token> [title]"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "token为web端获取，title为标记"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "例：/yinwuchat bind 12345 电脑token"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "查询：/yinwuchat list"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "可以查询到你绑定的所有token"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "解绑：/yinwuchat unbind <id>"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "可以解绑对应的token，id为查询结果中的id"));
        commandSender.sendMessage(buildMessage(ChatColor.GOLD + "例：/yinwuchat unbind 1"));
        return;
    }
    
    private TextComponent buildMessage(String message){
        return new TextComponent(message);
    }
    
    private List<Map<String,Object>> getTokens(UUID uuid){
        Map<String,Object> userMap = PlayerUtil.getUserFromSql(uuid);
        if (userMap==null) {
            return new ArrayList<>();
        }
        int user_id = (int)userMap.get("id");
        String sql = "select * from `chat_token` where user=?";
        List<Map<String,Object>> list = Yinwuchat.getMySql().query(sql, user_id);
        return list;
    }
    
    private void autoDeleteExpireToken(UUID uuid){
        String sql = "";
        List<Map<String,Object>> list = getTokens(uuid);
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Map<String,Object> tokenMap = list.get(i);
                
                Date time = (Date)tokenMap.get("time");
                long diff = new Date().getTime() - time.getTime();
                if (diff>=Yinwuchat.getExpireTime()) {
                    sql = "delete from `chat_token` where token=?";
                    Yinwuchat.getMySql().execute(sql, (String)tokenMap.get("token"));
                }
            }
        }
    }
}
