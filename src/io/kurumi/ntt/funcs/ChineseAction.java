package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import cn.hutool.core.text.*;
import cn.hutool.http.*;

public class ChineseAction extends Fragment {

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

		if (startWithChinese(msg.command())) {

			if (msg.replyTo() != null) {

                if (msg.params().length > 0) {

                    String params = ArrayUtil.join(msg.params()," ");
                    
                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + " " + msg.replyTo().from().userName() + " " + params + " ~").html().exec();

                } else {

                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + " 了 "+ msg.replyTo().from().userName() + " ~").html().exec();

                }

			} else {
                
                
                
                String command = msg.params().length > 0 ? msg.command() + " " + ArrayUtil.join(msg.params()," ") : msg.command() + " 了";

				msg.send(user.userName() + " " + HtmlUtil.escape(command) + " ~").html().exec();

				msg.delete();

			}

			return true;

		}

		return false;

	}

}
