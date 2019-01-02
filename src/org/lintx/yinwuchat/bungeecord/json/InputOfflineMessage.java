/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.json;

/**
 *
 * @author jjcbw01
 */
public class InputOfflineMessage extends BaseInputJSON{
    private int last_id;
    public InputOfflineMessage(int id){
        last_id = id;
    }
    
    public int getLastId(){
        return last_id;
    }
}
