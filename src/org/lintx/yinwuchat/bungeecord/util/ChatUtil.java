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
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
    
    public static TextComponent formatMessage(UUID playerUUID,String message){
        ProxiedPlayer player = PlayerUtil.getPlayer(playerUUID);
        //String _prefix = prefix.replaceAll("%player_name%", player.getDisplayName());
        //String _suffix = suffix.replaceAll("%player_name%", player.getDisplayName());
        //return _prefix + message + _suffix;
        
        //TextComponent component = new TextComponent( _prefix + message + _suffix );
        //component.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "http://spigotmc.org" ) );
        //component.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Goto the Spigot website!").create() ) );
        
        
        TextComponent chat = new TextComponent();
        TextComponent iden = new TextComponent(identification);
        iden.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        iden.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tooltip).create()));
        chat.addExtra(iden);
        chat.addExtra(prefix);
        chat.addExtra(player.getDisplayName());
        chat.addExtra(separator);
        chat.addExtra(message);
        chat.addExtra(suffix);
        return chat;
    }
}
