/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 *
 * @author jjcbw01
 */
public class ChatUtil {
    private static String prefix = "";
    private static String suffix = "";
    private static String identification = "";
    private static String tooltip = "";
    private static String url = "";
    private static String separator = "";
    private static int interval = 1000;
    private static String private_separator = "";
    private static String me_private_separator1 = "";
    private static String me_private_separator2 = "";
    
    private static String join_name_color = "";
    private static String join_message = "";
    private static String leave_name_color = "";
    private static String leave_message = "";
    
    public static void setPrefix(String prefix){
        if (prefix==null) {
            prefix = "";
        }
        ChatUtil.prefix = prefix;
    }
    
    public static void setSuffix(String suffix){
        if (suffix==null) {
            suffix = "";
        }
        ChatUtil.suffix = suffix;
    }
    
    public static void setIdentification(String identification){
        if (identification==null) {
            identification = "";
        }
        ChatUtil.identification = identification;
    }
    
    public static void setTooltip(String tooltip){
        if (tooltip==null) {
            tooltip = "";
        }
        ChatUtil.tooltip = tooltip;
    }
    
    public static void setUrl(String url){
        if (url==null) {
            url = "";
        }
        ChatUtil.url = url;
    }
    
    public static void setSeparator(String separator){
        if (separator==null) {
            separator = "";
        }
        ChatUtil.separator = separator;
    }
    
    public static void setPrivateSeparator(String separator){
        if (separator==null) {
            separator = "";
        }
        ChatUtil.private_separator = separator;
    }
    
    public static void setMePrivateSeparator1(String separator){
        if (separator==null) {
            separator = "";
        }
        ChatUtil.me_private_separator1 = separator;
    }
    
    public static void setMePrivateSeparator2(String separator){
        if (separator==null) {
            separator = "";
        }
        ChatUtil.me_private_separator2 = separator;
    }
    
    public static void setInterval(int interval){
        ChatUtil.interval = interval;
    }
    
    public static int getInterval(){
        return ChatUtil.interval;
    }
    
    public static void setJoinNameColor(String color){
        if (color==null) {
            color = "";
        }
        ChatUtil.join_name_color = color;
    }
    
    public static void setJoinMessage(String message){
        if (message==null) {
            message = "";
        }
        ChatUtil.join_message = message;
    }
    
    public static void setLeaveNameColor(String color){
        if (color==null) {
            color = "";
        }
        ChatUtil.leave_name_color = color;
    }
    
    public static void setLeaveMessage(String message){
        if (message==null) {
            message = "";
        }
        ChatUtil.leave_message = message;
    }
    
    public static TextComponent formatMessage(UUID playerUUID,String message){
        String player_name = PlayerUtil.getPlayerName(playerUUID);
        
        TextComponent chat = new TextComponent();
        
        chat.addExtra(pluginTextComponent());
        chat.addExtra(prefix);
        chat.addExtra(playerNameTextComponent("", player_name, true));
        chat.addExtra(separator);
        chat.addExtra(message);
        chat.addExtra(suffix);
        return chat;
    }
    
    public static TextComponent formatPrivateMessage(String player_name,String message){
        TextComponent chat = new TextComponent();
        
        chat.addExtra(pluginTextComponent());
        chat.addExtra(prefix);
        chat.addExtra(playerNameTextComponent("", player_name, true));
        chat.addExtra(private_separator);
        chat.addExtra(message);
        chat.addExtra(suffix);
        return chat;
    }
    
    public static TextComponent formatMePrivateMessage(String player_name,String message){
        TextComponent chat = new TextComponent();
        
        chat.addExtra(pluginTextComponent());
        chat.addExtra(prefix);
        chat.addExtra(me_private_separator1);
        chat.addExtra(playerNameTextComponent("", player_name, true));
        chat.addExtra(me_private_separator2);
        chat.addExtra(message);
        chat.addExtra(suffix);
        return chat;
    }
    
    public static TextComponent formatJoinMessage(UUID playerUUID){
        String player_name = PlayerUtil.getPlayerName(playerUUID);
        
        TextComponent chat = new TextComponent();
        
        chat.addExtra(pluginTextComponent());
        chat.addExtra(" ");
        chat.addExtra(playerNameTextComponent(join_name_color, player_name, true));
        chat.addExtra(join_message);
        return chat;
    }
    
    public static TextComponent formatLeaveMessage(UUID playerUUID){
        String player_name = PlayerUtil.getPlayerName(playerUUID);
        
        TextComponent chat = new TextComponent();
        
        chat.addExtra(pluginTextComponent());
        chat.addExtra(" ");
        chat.addExtra(playerNameTextComponent(leave_name_color, player_name, false));
        chat.addExtra(leave_message);
        return chat;
    }
    
    private static TextComponent pluginTextComponent(){
        TextComponent iden = new TextComponent(identification);
        iden.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        iden.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tooltip).create()));
        return iden;
    }
    
    private static TextComponent playerNameTextComponent(String color,String player_name,Boolean canClick){
        TextComponent iden = new TextComponent(color + player_name);
        if (canClick) {
            iden.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/yinwuchat msg " + player_name + ""));
            iden.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("点击发送私聊").create()));
        }
        return iden;
    }
}
