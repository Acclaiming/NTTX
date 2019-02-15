package io.kurumi.ntt;

public class BotConf {
    
    /**
    
    名称
    
    */
    public static final String BOT_NAME = "NTTBot";
    
    /**
    
    Bot的Token 填入即可
    
    */
    public static final String BOT_TOEKN = "729889711:AAEbpHqfuubWgMLep0hKU1ys92NotjXmKA0";
    
    /**
    
    创始人ID
    
    */
    public static final String FOUNDER = "HiedaNaKan";
    
    /**
    
    Bot服务器的域名 注意 Bot不会绑定此域名
    
    而且是应该用Nginx等反向代理服务器代理该域名
    
    下面设置的本地端口
    
    注意 : 不应该带有 "http(s)://" 前缀 和 "/" 后缀
    
    */
    public static final String SERVER_DOMAIN = "ntt.kurumi.io";
    
    /**
    
    本地服务器端口 请将上方的域名反向代理到此端口
    
    */
    public static final int LOCAL_PORT = 18964;
    
    /**
    
    Twitter API Key
    
    注意 : 应用回调地址必须为 https://[上方填写的域名]/callback
    
    */
    
    public static final String TWITTER_CUSTOM_KEY = "pLkoUI2q5ZncKKIm7dQNqtpXT";
    
    /**
    
    Twutter Key Sec
    
    */
    public static final String TWITTER_CUSTOM_KEY_SEC = "Yp59pXMXoHKD8dj2g1m6RUc7VNIJybBHH1NtM70MhOB0OKl00S";
    
    
    /**
    
    Bot 日志频道 不带@ (必须是公开频道 且Bot可访问)
    
    */
    public static final String LOG_CHANNEL = "NTTSpamPublic";
    
    public static final String LOG_CHANNEL_ID = "@" + LOG_CHANNEL;
    
    public static final String LOG_CHANNEL_URL = "https://" + LOG_CHANNEL + "/";

    /**
    
    Redis 数据库主机
    
    */
    public static final String REDIS_HOST = "localhost";
    
    /**
    
    Redis 数据库端口
    
    */
    public static final int REDIS_PORT = 6379;
    
    /**

     Redis 数据库密码

     */
    public static final String REDIS_PSWD = null;
    
    /**
    
    Redis NTTBot 数据库 默认 0
    
    */
    public static final int REDIS_DB = 0;
    
    
    
}
