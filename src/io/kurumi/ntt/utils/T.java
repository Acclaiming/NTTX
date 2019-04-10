package io.kurumi.ntt.utils;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import cn.hutool.core.thread.*;
import io.kurumi.ntt.Launcher;
import com.pengrad.telegrambot.response.GetGameHighScoresResponse;

public class T {

    public static boolean isUserContactable(long id) {

        SendResponse resp = new Send(id,"testContactable").disableNotification().sync();

        if (!resp.isOk()) return false;

        new Msg(resp.message()).delete();

        return true;

    }
    
    public static boolean checkNonContactable(UserData user,Msg msg) {
        
        String notContactableMsg = "咱无法给乃发送信息呢，请私聊点击 'start' 启用咱 ~";
        
        if (!msg.isPrivate() && !isUserContactable(user.id)) {
            
            if (msg instanceof Callback) {
                
                ((Callback)msg).alert(notContactableMsg);
                
            } else {
                
                msg.send(user.userName(),notContactableMsg).publicFailed();
                
            }
            
            return true;
            
        }
        
        return false;
        
    }
    
    public static boolean checkUserNonAuth(UserData user,Msg msg) {
        
        String nonAuthMsg = msg.isPrivate() ? "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 使用 /tauth ~" : "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 私聊BOT使用 /tauth ~";
        
        if (!TAuth.exists(user)) {
            
            if (msg instanceof Callback) {

                ((Callback)msg).alert(nonAuthMsg);

            } else {

                msg.send(nonAuthMsg).publicFailed();

            }
            
            
            
            return true;
            
        }
        
        if (!TAuth.avilable(user)) {

            TAuth.auth.remove(user.id.toString());

            TAuth.saveAll();

            msg.send("乃的认证可能已经被取消... 请使用 /tauth 重新认证 :(").exec();

            return true;
            
        }
        
        return false;
        
    }
    
    public static String parseScreenName(String input) {
        
        if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input,"twitter.com/",true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input,"?",false);

            }

        }
        
        return input;
        
    }
    
    public static long parseStatusId(String input) {
        
        Long statusId = -1L;

        if (NumberUtil.isNumber(input)) {

            statusId = NumberUtil.parseLong(input);

        } else if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input,"status/",true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input,"?",false);

            }

            if (NumberUtil.isNumber(input))  {

                statusId = NumberUtil.parseLong(input);

            }

        }
        
        return statusId;
        
    }
	
	public static void tryDelete(final long delay,final Msg... messages) {
		
		ThreadPool.exec(new Runnable() {

				@Override
				public void run() {
		
					ThreadUtil.sleep(delay);
					
					for (Msg message : messages) {
						
						if (message == null) continue;
						
						if (!message.delete()) return;
						
					}
					
				}
				
			});
		
	}
    
}
