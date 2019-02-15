package io.kurumi.ntt.db;

import io.kurumi.ntt.BotConf;
import java.io.File;
import redis.clients.jedis.Jedis;

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
        
        jedis.connect();
        
        jedis.select(BotConf.REDIS_DB);
        
        if (!"pong".equals(jedis.ping().trim().toLowerCase())) {
            
            throw new RuntimeException("无法连接Redis数据库");
            
        }

    }
    
    public static File CONFIG_DIR; static {
        
        CONFIG_DIR = new File(jedis.configGet("dir").get(0));
        
    }
    
    public static File backUp() {
        
        jedis.save();
        
        return new File(CONFIG_DIR,"dump.rdb");

    }

}
