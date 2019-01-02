/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.lintx.yinwuchat.bungeecord.Yinwuchat;
import org.lintx.yinwuchat.bungeecord.MySql;

/**
 *
 * @author jjcbw01
 */
public class InputCheckToken extends BaseInputJSON{
    private Boolean isvaild = false;
    private Boolean isbind = false;
    private String message = "";
    private String token = "";
    private UUID uuid = null;
    
    public Boolean getIsvaild(){
        return isvaild;
    }
    
    public Boolean getIsbind(){
        return isbind;
    }
    
    public String getMessage(){
        return message;
    }
    
    public String getToken(){
        return token;
    }
    
    public UUID getUuid(){
        return uuid;
    }
    
    public String getJSON(){
        JsonObject json = new JsonObject();
        json.addProperty("action", "check_token");
        json.addProperty("isbind", isbind);
        json.addProperty("status", isvaild);
        json.addProperty("message", message);
        return new Gson().toJson(json);
    }
    
    public String getTokenJSON(){
        JsonObject json = new JsonObject();
        json.addProperty("action", "update_token");
        json.addProperty("token", token);
        return new Gson().toJson(json);
    }
    
    public InputCheckToken(String token){
        this(token,true);
    }
    
    public InputCheckToken(String token,Boolean autoNewToken){
        MySql mysql = Yinwuchat.getMySql();
        this.token = token;
        if (token==null || token.equalsIgnoreCase("")) {
            if (autoNewToken) {
                message = "生成了新的token";
                newToken();
            }
        }
        else{
            Map<String,Object> oldToken = getTokenMap(token);
            if (oldToken==null) {
                message = "token无效";
                if (autoNewToken) {
                    message += "，生成了新的token";
                    newToken();
                }
            }
            else{
                Date time = (Date)oldToken.get("time");
                long diff = new Date().getTime() - time.getTime();
                if (diff>=Yinwuchat.getExpireTime()) {
                    message = "token过期";
                    if (autoNewToken) {
                        message += "，生成了新的token";
                        newToken();
                    }
                }
                else{
                    isvaild = true;
                    message = "success";
                    int user_id = (int)oldToken.get("user");
                    if (user_id>0) {
                        Map<String,Object> userMap = getUser(user_id);
                        if (userMap!=null) {
                            uuid = UUID.fromString((String)userMap.get("uuid"));
                            isbind = true;
                        }
                    }
                }
            }
        }
    }
    
    private Map<String,Object> getTokenMap(String token){
        String sql = "select * from `chat_token` where token = ?";
        List<Map<String,Object>> list = Yinwuchat.getMySql().query(sql, token);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    private Map<String,Object> getUser(int user_id){
        String sql = "select * from `chat_users` where id = ?";
        List<Map<String,Object>> list = Yinwuchat.getMySql().query(sql, user_id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    private void newToken(){
        MySql mysql = Yinwuchat.getMySql();
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        
        Map<String,Object> oldToken = getTokenMap(token);
        if (oldToken==null) {
            String sql = "insert into `chat_token` (`user`,`token`,`title`) values(0,?,'')";
            if (mysql.execute(sql, token)) {
                this.token = token;
            }
        }
    }
}
