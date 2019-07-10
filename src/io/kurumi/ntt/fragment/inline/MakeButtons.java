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
import java.util.Collection;
import java.util.Collections;

public class MakeButtons extends Fragment {

	@Override
	public void onQuery(UserData user,Query inlineQuery) {

		if (StrUtil.isBlank(inlineQuery.text)) {
		
			return;
			
		}

		String text = inlineQuery.text;

		boolean html = false;
		boolean buttons = false;

		while (true) {

		 if (text.startsWith(" ") || text.startsWith("\n")) {
				
				text = text.substring(1);
				
			} else if (text.startsWith("HTML")) {

				text = text.substring(5);

				html = true;

			} else if (text.startsWith("BUTTONS")) {

				text = text.substring(8);

				buttons = true;

			} else {

				break;

			}

		}

		if (StrUtil.isBlank(text) || (!html && !buttons)) {
			
			return;
			
		}

		ButtonMarkup markup = null;

		if (buttons) {

			for (String line : ArrayUtil.reverse(text.split("\n"))) {

				if (!line.startsWith("[") && line.endsWith(")")) break;
				
				ButtonLine bL = new ButtonLine();
				
				while (line.contains("[")) {
				
					String after = StrUtil.subBefore(line,"[",true);
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
				
				Collections.reverse(bL);
				
				markup.add(bL);
				
				text = StrUtil.subBefore(text,"\n",true);
				
			}

		}
		
		if (markup != null) {
			
			Collections.reverse(markup);
			
		}
		
		ParseMode parseMode = html ? ParseMode.HTML : null;
		
		inlineQuery.article("完成 *٩(๑´∀`๑)ง*",text,parseMode,markup);

		execute(inlineQuery.reply());
		
	}

}
