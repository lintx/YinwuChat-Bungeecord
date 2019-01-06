# YinwuChat-Bungeecord 说明文档

### 关于YinwuChat-Bungeecord
YinwuChat-Bungeecord是一个Bungeecord插件，可以将Bungeecord群组服内的消息通过WebSocket服务器广播，且可以收到WebSocket客户端的指令在服务器内广播消息，以达到游戏内和Web端同步聊天，且Web端可以发送消息到游戏内的插件。

- 本插件需要YinwuChat-Web插件（Web客户端）的配合以达到同步聊天和发送消息的目的
- 你也可以按照本文档的接口自行开发Web客户端
- 本插件需要MySQL支持，本插件将用于Web客户端用户认证的token和玩家UUID及玩家名保存到数据库
    - 你可以新建数据库，也可以使用已有数据库，但必须使用YinwuChat-Bungeecord.sql的数据表
    - 推荐新建数据库，并在新建的数据库中执行YinwuChat-Bungeecord.sql

### 配置文件
YinwuChat-Bungeecord的默认配置文件内容为：

```yaml
message:
    # 玩家在Web客户端向游戏内发送聊天内容时，游戏内玩家所看到的样式
    # 具体样式为identification.text + prefix + player_name + separator + message + suffix
    # 私聊消息样式为identification.text + prefix + player_name + private_message_separator + message + suffix
    # 我发送的私聊消息样式为identification + prefix + me_private_message_separator1 + player_name + me_private_message_separator2 + message + suffix
    # identification字段有tooltip(鼠标移动上去时的提示，内容为identification.tooltips字段的内容)，且可以点击，点击将打开网页（identification.click_url）
    # 本插件的文字样式代码必须使用`§`,使用`&`将会直接显示出来
    identification: 
        text: '§7[YinwuChat] '
        tooltips: '点击打开YinwuChat网页'
        click_url: 'https://chat.yinwurealm.org'
    prefix: '§b'
    separator: ' §7> §f'
    private_message_separator: ' §7悄悄的对你说: §f'        #私聊消息分隔
    private_message_separator: ' §7悄悄的对你说: §f'
    me_private_message_separator1: ' §7你悄悄的对'
    suffix: ''
    interval: 1000                  #WebClient发送消息最小间隔时间
    joinmessage:
        player_name_color: '§b'
        message: '§6加入了YinwuChat'
    leavemessage:
        player_name_color: '§b'
        message: '§6离开了了YinwuChat'
    #离线消息保存时间，单位：天，0为永久保存
    offline_message_expire: 0
token:
    player_max_count: 5         #单个玩家最多可以同时绑定的token的数量（使用绑定的token可以在Web客户端以绑定的游戏名向游戏内发送消息）
    expire_time: 1296000        #token过期时间，自token生成时计算，超过该时间（单位为秒）的token将无法使用并会被删除
websocket:
    port: 8888                  #WebSocket监听端口，你可以使用nginx或apache等软件将Web客户端的ws请求反向代理到这个端口
mysql:
    host: 127.0.0.1             #mysql地址
    port: 3306                  #mysql端口
    database: chat              #mysql数据库名
    username: root              #mysql用户名
    password:                   #mysql密码
```

插件启用时如果配置文件不存在将创建默认配置文件，创建了默认配置文件后你需要修改mysql设置，你或许还会修改WebSocket监听端口
修改配置并保存后在Bungeecord控制台使用`yinwuchat reload`命令重新加载配置
* 你也可以在游戏中使用`/yinwuchat reload`命令重新加载配置，但这需要你具有`yinwuchat.reload`权限


### 接口

本插件所有信息均由WebSocket通信，格式均为JSON格式，具体数据如下：
#### 发往本插件的数据：
1. 检查token
```js
{
    "action": "check_token",
    "token": "待检查的token，token由服务器下发，初次连接时可以使用空字符串"
}
```
2. 发送消息
```js
{
    "action": "send_message",
    "message": "需要发送的消息，注意，格式代码必须使用§"
}
```
3. 获取历史消息
```js
{
    "action": "offline_message",
    "last_id": "最后一条消息的ID，如从未获取过历史消息，应该为0（没有接收过新消息）或接收到的第一条新消息的id，如已获取过历史消息，则应该是最后一条历史消息id（id最小的那条），数据格式：int"
}
```

#### 发往Web客户端的数据：
1. 更新token（接收到客户端发送的check_token数据，然后检查token失败时下发，收到该数据应提醒玩家在游戏内输入/yinwuchat token title命令绑定token）
```js
{
    "action": "update_token",
    "token": "一个随机的token"
}
```
2. token校验结果（检查token成功后返回，或玩家在游戏内绑定成功后，token对应的WebSocket在线时主动发送，只有接收到了这个数据，且数据中的status为true，且数据中的isbind为true时才可以向服务器发送send_message数据）
```js
{
    "action": "check_token",
    "status": true/false,        //表示该token是否有效
    "message": "成功时为success，失败时为原因，并同时发送一个更新token数据",
    "isbind": false/true         //表示该token是否被玩家绑定
}
```
3. 玩家在游戏内发送了消息
```js
{
    "action": "send_message",
    "time": unix时间戳，单位为毫秒（java/JavaScript时间戳）,
    "player": "玩家名",
    "server": "服务器名",
    "message": "消息内容",
    "message_id":消息id(int)
}
```
4. 玩家登录游戏
```js
{
    "action": "player_join",
    "player": "玩家名",
    "server": "服务器名（可能为空）",
    "time": unix时间戳
}
```
5. 玩家退出游戏
```js
{
    "action": "player_leave",
    "player": "玩家名",
    "server": "服务器名（可能为空）",
    "time": unix时间戳
}
```
6. 玩家切换服务器
```js
{
    "action": "player_switch_server",
    "player": "玩家名",
    "server": "服务器名（可能为空）",
    "time": unix时间戳
}
```
7. 游戏玩家列表（连接到服务器时、玩家进入游戏时、玩家切换服务器时、玩家退出游戏时发送）
```js
{
    "action": "game_player_list",
    "player_list":[
        {
            "player_name": "玩家游戏名",
            "server_name": "玩家所在服务器"
        },
        ……
    ]
}
```
8. WebClient玩家列表（连接到服务器时、玩家进入WebClient时、玩家退出WebClient时发送）
```js
{
    "action": "web_player_list",
    "player_list":[
        "玩家名1",
        "玩家名2",
        ……
    ]
}
```
9. 私聊消息
```js
{
    "action": "private_message",
    "player": "玩家名",
    "server": "玩家所在服务器",
    "message": "消息内容",
    "time": unix时间戳,
    "message_id":消息id(int)
}
```
10. WebClient玩家登录
```js
{
    "action": "player_web_join",
    "player": "玩家名",
    "time": unix时间戳
}
```
11. WebClient玩家断开连接
```js
{
    "action": "player_web_leave",
    "player": "玩家名",
    "time": unix时间戳
}
```
12. 服务器提示消息（一般为和服务器发送数据包后的错误反馈信息）
```js
{
    "action": "server_message",
    "message": "消息内容",
    "time": unix时间戳,
    "status": 状态码，详情见下方表格(int)
}
```
13.历史消息
```js
{
    "action": "offline_message",
    "messages":[
        {
            "action": "send_message 公开消息\
            private_message 私聊消息\
            me_private_message 我发送的私聊消息",
            "player": "玩家名",
            "server": "玩家所在服务器",
            "message": "消息内容",
            "time": unix时间戳,
            "message_id": 消息id(int)
        },
        ……
    ]
}
```
14. 我发送的私聊消息
```js
{
    "action": "me_private_message",
    "player": "玩家名",
    "server": "玩家所在服务器",
    "message": "消息内容",
    "time": unix时间戳,
    "message_id": 消息id(int)
}
```

#### 服务器消息状态码
状态码|具体含义
-:|-
0|一般成功或提示消息
1|一般错误消息
1001|获取历史聊天记录时，内容为空（不可继续获取历史消息）

### 命令
1. 控制台命令
    - `yinwuchat reload`：重新加载配置
2. 游戏内命令
    - `/yinwuchat`：插件帮助（其他未识别的命令也都将显示帮助）
    - `/yinwuchat reload`：重新加载配置文件，执行这个命令需要你具有`yinwuchat.reload`权限
    - `/yinwuchat bind token title`：绑定token，`token`是插件下发给web客户端的，玩家从web客户端获取token后到游戏内使用命令将玩家和token进行绑定，`title`是自定义标识，可以省略，绑定了标识时可以在使用`list`命令时看到，以便玩家绑定了多个token时进行识别
    - `/yinwuchat list`：列出玩家已绑定的token，每个token将显示id、过期时间和title共3种信息，id用于解绑，title用于玩家识别
    - `/yinwuchat unbind id`：解绑token，当你需要解绑某个token时使用（如在公共场合绑定了token，或者不想用这个token了，或者绑定的token达到上限了等），id为使用`list`命令时查询到的token id
    - `/yinwuchat msg 玩家名 消息`：向玩家发送私聊消息
3. WebClient命令
    - `/yinwuchat msg 玩家名 消息`：向玩家发送私聊消息

### 权限
本插件本身不需要权限，但可以给玩家赋予`yinwuchat.reload`权限以使玩家可以在游戏中使用`/yinwuchat reload`命令重新加载插件配置
- 权限需要在Bungeecord中设置，玩家可以在Bungeecord连接到的任何服务器使用这个命令

### 错误信息
有些时候，玩家执行命令的时候可能会碰到一些错误（主要为数据库错误），具体含义为：

错误代码|具体含义
-:|-
001|根据UUID查找用户失败，且新增失败
002|根据token修改token表user和title失败
003|根据id删除token表记录失败

### 其他信息
本插件由国内正版Minecraft服务器[YinwuRealm](https://www.yinwurealm.org/)玩家[LinTx](https://mine.ly/LinTx.1)为服务器开发

### 更新记录
- 2019-01-02 增加新功能：1.过滤空消息，2.发送消息间隔设置，3.处理命令消息，4.离线消息，5.私聊消息，6.在线玩家，7.WebClient上下线通知
- 2018-12-28 玩家登录时将DisplayName保存到数据库，因为Bungeecord无法获取离线玩家的信息
- 2018-12-28 插件重构到Bungeecord完成
- 2018-12-27 YinwuChat(Bukkit)0.0.1开发完成，测试中发现DeluxeChat无法获取Bungeecord其他服务器的聊天事件，开始将插件重构到Bungeecord插件
- 2018-12-25 开始开发YinwuChat，是一个Bukkit插件，需要DeluxeChat和PlaceHolderAPI作为前置插件
