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

```
message:
    # 玩家在Web客户端向游戏内发送聊天内容时，游戏内玩家所看到的样式
    # 具体样式为identification.text + prefix + player_name + separator + message + suffix
    # identification字段有tooltip(鼠标移动上去时的提示，内容为identification.tooltips字段的内容)，且可以点击，点击将打开网页（identification.click_url）
    # 本插件的文字样式代码必须使用`§`,使用`&`将会直接显示出来
    identification: 
        text: '§7[YinwuChat] '
        tooltips: '点击打开YinwuChat网页'
        click_url: 'https://chat.yinwurealm.org'
    prefix: '§b'
    separator: ' §7> §f'
    suffix: ''
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
```
{
    "action":"check_token",
    "token":"待检查的token，token由服务器下发，初次连接时可以使用空字符串"
}
```
2. 发送消息
```
{
    "action":"send_message",
    "message":"需要发送的消息，注意，格式代码必须使用§"
}
```

#### 发往Web客户端的数据：
1. 更新token（接收到客户端发送的check_token数据，然后检查token失败时下发，收到该数据应提醒玩家在游戏内输入/yinwuchat token title命令绑定token）
```
{
    "action":"update_token",
    "token":"一个随机的token"
}
```
2. token校验结果（检查token成功后返回，或玩家在游戏内绑定成功后，token对应的WebSocket在线时主动发送，只有接收到了这个数据，且数据中的status为true，且数据中的isbind为true时才可以向服务器发送send_message数据）
```
{
    "action":"check_token",
    "status":true/false,        //表示该token是否有效
    "message":"成功时为success，失败时为原因，并同时发送一个更新token数据",
    "isbind":false/true         //表示该token是否被玩家绑定
}
```
3. 玩家在游戏内发送了消息
```
{
    "action":"send_message",
    "time":unix时间戳，单位为毫秒（java/JavaScript时间戳）,
    "player":"玩家名",
    "message":"消息内容"
}
```

### 命令
1. 控制台命令
    - `yinwuchat reload`：重新加载配置
2. 游戏内命令
    - `/yinwuchat`：插件帮助（其他未识别的命令也都将显示帮助）
    - `/yinwuchat reload`：重新加载配置文件，执行这个命令需要你具有`yinwuchat.reload`权限
    - `/yinwuchat bind token title`：绑定token，`token`是插件下发给web客户端的，玩家从web客户端获取token后到游戏内使用命令将玩家和token进行绑定，`title`是自定义标识，可以省略，绑定了标识时可以在使用`list`命令时看到，以便玩家绑定了多个token时进行识别
    - `/yinwuchat list`：列出玩家已绑定的token，每个token将显示id、过期时间和title共3种信息，id用于解绑，title用于玩家识别
    - `/yinwuchat unbind id`：解绑token，当你需要解绑某个token时使用（如在公共场合绑定了token，或者不想用这个token了，或者绑定的token达到上限了等），id为使用`list`命令时查询到的token id

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
2018-12-28 插件重构到Bungeecord完成
2018-12-27 YinwuChat(Bukkit)0.0.1开发完成，测试中发现DeluxeChat无法获取Bungeecord其他服务器的聊天事件，开始将插件重构到Bungeecord插件
2018-12-25 开始开发YinwuChat，是一个Bukkit插件，需要DeluxeChat和PlaceHolderAPI作为前置插件