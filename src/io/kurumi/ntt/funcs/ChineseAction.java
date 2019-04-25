package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import cn.hutool.core.text.*;
import cn.hutool.http.*;
import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import cn.hutool.json.*;
import io.kurumi.ntt.utils.*;

public class ChineseAction extends Function {

    public JSONArray disable = LocalData.getJSONArray("data","disable_action",true);
    
    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("action");
        
    }

    @Override
    public int target() {
        
        return Group;
        
    }
    
    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {
        
        if (NTT.checkGroupAdmin(msg)) return;
        
        if (params.length == 1 && "off".equals(params[0])) {
            
            if (disable.contains(msg.chatId().longValue())) {
                
                msg.send("无需重复关闭 ~").exec();
                
            } else {
                
                disable.add(msg.chatId());
                
                LocalData.setJSONArray("daat","disable_action",disable);
                
                msg.send("关闭成功 ~").exec();
                
            }
            
        } else {
            
            if (!disable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                disable.remove(msg.chatId());

                LocalData.setJSONArray("daat","disable_action",disable);
                
                msg.send("已开启 ~").exec();
                
            }
            
        }
        
        
    }

	public static ChineseAction INSTANCE = new ChineseAction();

	boolean startWithChinese(String msg) {

		if (msg == null) return false;

		if (msg.length() == 0) return false;

		char first = msg.charAt(0);

		return !CharUtil.isAscii(first);

		// return first >= 0x4E00 &&  first <= 0x9FA5;

	}

	@Override
	public boolean onGroup(UserData user,Msg msg) {

        if (disable.contains(msg.chatId().longValue())) return false;
        
		if (startWithChinese(msg.command())) {

			if (msg.replyTo() != null) {

                if (msg.params().length > 0) {

                    String params = ArrayUtil.join(msg.params()," ");
                    
                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + " " + msg.replyTo().from().userName() + " " + params + " ~").html().exec();

                } else {

                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + "了 "+ msg.replyTo().from().userName() + " ~").html().exec();

                }

			} else {
                
                String command = msg.params().length > 0 ? msg.command() + " " + ArrayUtil.join(msg.params()," ") : msg.command();
                
                if (!command.contains("了")) command = command + "了";

				msg.send(user.userName() + " " + HtmlUtil.escape(command) + " ~").html().exec();

				msg.delete();

			}

			return true;

		}

		return false;

	}

}
