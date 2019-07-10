package io.kurumi.ntt.fragment.inline;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Query;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.model.request.ButtonMarkup;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.model.request.ButtonLine;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.response.BaseResponse;

public class MakeButtons extends Fragment {

	@Override
	public void onQuery(UserData user,Query inlineQuery) {

		if (StrUtil.isBlank(inlineQuery.text)) {
			
			execute(inlineQuery.article("错误 : 空的内容","错误 : 空的内容",null,null).reply());
			
			return;
			
		}

		String text = inlineQuery.text;

		boolean html = false;
		boolean buttons = false;

		while (true) {

			if (text.startsWith("@" + origin.me.username())) {
				
				text = text.substring(origin.me.username().length() + 2);
				
			} else if (text.startsWith(" ") || text.startsWith("\n")) {
				
				text = text.substring(1);
				
			} else if (text.startsWith("HTML ")) {

				text = text.substring(5);

				html = true;

			} else if (text.startsWith("BUTTONS ")) {

				text = text.substring(8);

				buttons = true;

			} else {

				break;

			}

		}

		if (StrUtil.isBlank(text) || (!html && !buttons)) {
			
			execute(inlineQuery.article("错误 : 不需要解析","错误 : 不需要解析",null,null).reply());

			return;
			
		}

		ButtonMarkup markup = null;

		if (buttons) {

			for (String line : ArrayUtil.reverse(text.split("\n"))) {

				if (!line.startsWith("[") && line.endsWith(")")) break;
				
				ButtonLine bL = new ButtonLine();
				
				while (line.contains("[")) {
				
					String after = StrUtil.subAfter(line,"[",true);
					line = StrUtil.subBefore(line,"[",true);
					
					String bText = StrUtil.subBefore(after,"]",false);
					String bUrl = StrUtil.subBetween(after,"(",")");
					
					if (bText == null || bUrl == null) {
						
						// invalid format
						
						break;
						
					}
					
					bL.newUrlButton(bText,bUrl);
					
				}

				if (markup == null) markup = new ButtonMarkup();
				
				markup.add(bL);
				
				text = StrUtil.subBefore(text,"\n",true);
				
			}

		}
		
		ParseMode parseMode = html ? ParseMode.HTML : null;
		
		inlineQuery.article("解析完成 点击显示",text,parseMode,markup);

		execute(inlineQuery.reply());
		
	}

}
