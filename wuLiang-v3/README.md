<div align="center">
    <img src="http://gchat.qpic.cn/gchatpic_new/2094085327/2083469072-2384229104-54123951E638DEFCE53F8A296AFF9012/0?term&#61;3" alt="logo" style="width:233px ;height:233px;border-radius:50%"/>
    <p>
</div>


该项目为Springboot项目，此项目基于[`simple bot v2`](https://github.com/ForteScarlet/simpler-robot/tree/v2-dev)

## 项目文档

[更新日志](WuLiang-Bot更新日志.md)（暂时没有）

## 项目地址

WuLiang-Bot ->
[GitHub](https://github.com/2094085327/WuLiang-Bot)

## 运行环境

`maven` `Java1.8+` `Mysql 8.0 `

## 你需要掌握的知识

1.能够主观的，能动的，熟练的运用[`百度`](https://www.baidu.com)进行查询

2.对`Springboot`有基础的了解,建议先掌握`Spring`

3.对`Mybatis-Plus`有一定了解，建议先掌握`Mybatis`

# 如何开始?

1.打开`src/main/resources/simbot-bots`文件夹, 在里面创建一个`*.bot`文件, `*`
可以是任意字符, 具体配置参考[此文档](https://www.yuque.com/simpler-robot/simpler-robot-doc/fk6o3e#iUKbX)

v3版本配置如下

```json
{
  "component": "simbot.mirai",
  "code": 123456,
  "passwordInfo": {
    "type": "text",
    "text": "123456"
  },
  "config": {
    "workingDir": ".",
    "heartbeatPeriodMillis": 60000,
    "statHeartbeatPeriodMillis": 300000,
    "heartbeatTimeoutMillis": 5000,
    "heartbeatStrategy": "STAT_HB",
    "reconnectionRetryTimes": 2147483647,
    "autoReconnectOnForceOffline": false,
    "protocol": "ipad",
    "highwayUploadCoroutineCount": 16,
    "deviceInfo": {
      "type": "auto"
    },
    "noNetworkLog": false,
    "noBotLog": false,
    "isShowingVerboseEventLog": false,
    "cacheDir": "cache",
    "contactListCache": {
      "saveIntervalMillis": 60000,
      "friendListCacheEnabled": false,
      "groupMemberListCacheEnabled": false
    },
    "loginCacheEnabled": true,
    "convertLineSeparator": true,
    "recallMessageCacheStrategy": "MEMORY_LRU"
  }
}
```

其中code为你的账号，"passwordInfo"中"text"为你的密码,密码同时支持md5加密，具体格式为:

```json
{
  "passwordInfo": {
    "type": "md5",
    "text": "md5加密后的密码"
  }
}
```

2.导入`wuliang.sql`文件，打开`src/main/resources/SQLFile`文件夹，将其中的`wuliang.sql`文件导入到数据库或自己重写

3.打开`\src\main\resources\`文件夹, 在里面创建一个`application-*.yml`文件, 如果你会`springboot`, 应该能看懂这一步, 下面是本项目所需要的配置项

```yaml
#配置数据源
server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wuliang?useSSL=FALSE&serverTimezone=UTC
    username: root
    password:
    type: com.alibaba.druid.pool.DruidDataSource 
```

4.配置Bot基础配置

```yaml
wuLiang:
  config:
    # 图床路径，用于自动更新图片
    gallery: "https://cdn.jsdelivr.net/gh/2094085327/GenShinImage/"
    # 原神本地图片路径
    localPath: "resources/Image/Genshin/"
```

注意：

①若未配置图床路径，则可能导致部分图片功能无法自动更新

②若未配置原神图片到本地，那么首次启动抽卡分析时会有较长的时间从图床中获取图片到本地，具体时间视运行环境网络情况决定