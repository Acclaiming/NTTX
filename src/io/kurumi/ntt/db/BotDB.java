package io.kurumi.ntt.db;

import io.kurumi.ntt.BotConf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import cn.hutool.json.JSONArray;

public class BotDB {

    public static final Jedis jedis;

    static {

        if (BotConf.REDIS_PORT == 6379) {

            jedis = new Jedis(BotConf.REDIS_HOST);

        } else {

            jedis = new Jedis(BotConf.REDIS_HOST, BotConf.REDIS_PORT);

        }

        if (BotConf.REDIS_PSWD != null) {

            jedis.auth(BotConf.REDIS_PSWD);

        }

   }
    
}
