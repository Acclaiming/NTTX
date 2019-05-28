package io.kurumi.ntt.utils;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;

public class NTT {

	/*
	
	public static long[] getChatMembers(Long chat) {
		
		TLRequestMessagesGetFullChat getFullChat = new TLRequestMessagesGetFullChat();

		getFullChat.setChatId(chat.intValue());
		
		try {
			
			TLMessagesChatFull resp = Launcher.INSTANCE.mtp.getKernelComm().doRpcCallSync(getFullChat);

			if (resp != null) {
				
				long[] users = new long[resp.getUsers().size()];
				
				for (int index = 0;index < resp.getUsers().size();index ++) {
					
					users[index] = resp.getUsers().get(index).getId();
					
				}
				
				return users;
				
			}
			
		} catch (ExecutionException e) {
			
			BotLog.info("getfulchat error",e);
			
		} catch (RpcException e) {
			
			BotLog.info("getfulchat error",e);
			
		}
		
		return null;

	}
	
	*/
	
    public static boolean isUserContactable(long id) {

        SendResponse resp = new Send(id,"testContactable").disableNotification().exec();

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
    
    /*

    public static boolean checkUserNonAuth(UserData user,Msg msg) {

        String nonAuthMsg = msg.isPrivate() ? "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 使用 /login ~" : "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 私聊BOT使用 /login ~";

        if (!TAuth.exists(user.id)) {

            if (msg instanceof Callback) {

                ((Callback)msg).alert(nonAuthMsg);

            } else {

                msg.send(nonAuthMsg).publicFailed();

            }



            return true;

        }

        if (!TAuth.avilable(user.id)) {

            synchronized (TAuth.auth) {

                TAuth.auth.remove(user.id.toString());

            }

            TAuth.saveAll();

            msg.send("乃的认证可能已经被取消... 请使用 /login 重新认证 :(").exec();

            return true;

        }

        return false;

    }
    
    */

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

        if (NumberUtil.isLong(input)) {

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
    
    static Timer deleteTimer = new Timer();

	public static void tryDelete(final long delay,final Msg... messages) {

        deleteTimer.schedule(new TimerTask() {

				@Override
				public void run() {
                    
					for (Msg message : messages) {

						if (message == null) continue;

						if (!message.delete()) return;

					}

				}

			},delay);

	}

    public static boolean isGroupAdmin(Long chatId,Long userId) {

        GetChatMemberResponse resp = Launcher.INSTANCE.bot().execute(new GetChatMember(chatId,userId.intValue()));

        if (resp.isOk() && ((resp.chatMember().status() == ChatMember.Status.administrator) || resp.chatMember().status() == ChatMember.Status.creator)) {

            return true;

        }

        return false;

    }
    
    public static boolean checkPrivate(Msg msg) {

        if (!msg.isPrivate()) {
            
            msg.send("请使用私聊 ( ˶‾᷄࿀‾᷅˵ )").publicFailed();
            
            return true;
            
        }
        
        return false;

    }
    
    public static boolean checkGroup(Msg msg) {

        if (!msg.isGroup()) {

            msg.send("请在群组使用 ( ˶‾᷄࿀‾᷅˵ )").exec();

            return true;

        }

        return false;
        
    }
    
    public static boolean checkGroupAdmin(Msg msg) {
        
		if (msg.from().developer()) return false;
		
        if (!isGroupAdmin(msg.chatId(),msg.from().id)) {
            
            if (msg instanceof Callback) {
                
                ((Callback)msg).alert("你不是绒布球 Σ( ﾟωﾟ");
                
            } else {
                
                msg.send("你不是绒布球 Σ( ﾟω。").publicFailed();
                
            }
            
            return true;
            
        }
        
        return false;
        
    }
    
    public static String checkCommand(Msg msg) {
        
        if (msg.isCommand()) {
            
            return msg.command();
            
        }
        
        return "-1";
        
    }

}
