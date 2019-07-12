package io.kurumi.ntt.fragment.inline;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.ButtonMarkup;
import java.util.Collections;

public class MakeButtons extends Fragment {

	@Override
	public void onQuery(UserData user,Query inlineQuery) {

		if (StrUtil.isBlank(inlineQuery.text) || inlineQuery.text.startsWith("BUTTONS")) {
		
			return;
			
		}

		String text = inlineQuery.text;

		boolean html = false;
		boolean buttons = false;

		while (true) {

		 if (text.startsWith("\n") || text.startsWith(" ")) {
				
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

				if (!(line.startsWith("[") && line.endsWith(")"))) break;
				
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

		AnswerInlineQuery request = inlineQuery.reply();

		BaseResponse resp = execute(request);
		
		if (resp == null) {
        } else if (!resp.isOk()) {
			
			inlineQuery.article("解析失败","解析失败 : \n\n" + resp.description(),null,null);
			
			execute(inlineQuery.reply());
			
		}

	}

}
