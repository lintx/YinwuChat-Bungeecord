/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.util;

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
            e.printStackTrace();
        }
        return player;
    }
}
