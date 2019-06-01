package io.kurumi.ntt.fragment.twitter.status;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.twitter.archive.*;
import cn.hutool.core.date.*;
import java.text.*;

public class StatusSearch extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("search");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		int index = 0;

		long from = -1;

		long to = -1;

		int media = 0;

		int utc = 8;

		long start = -1;

		long end = -1;

		for (;index < params.length;index ++) {

			String param = params[index];

			if (param.startsWith("from=")) {

				String fromC = StrUtil.subAfter(param,"=",false);

				if (NumberUtil.isNumber(fromC)) {

					from = NumberUtil.parseLong(fromC);

				} else {

					if (fromC.startsWith("@")) fromC = fromC.substring(1);

					UserArchive archive = UserArchive.get(fromC);

					if (archive == null) {

						msg.send("没有这个人的记录 : " + fromC).exec();

						return;

					}

					from = archive.id;

				}

			} else if (param.startsWith("--to=")) {

				String toC = StrUtil.subAfter(param,"=",false);

				if (NumberUtil.isNumber(toC)) {

					from = NumberUtil.parseLong(toC);

				} else {

					if (toC.startsWith("@")) toC = toC.substring(1);

					UserArchive archive = UserArchive.get(toC);

					if (archive == null) {

						msg.send("没有这个人的记录 : " + toC).exec();

						return;

					}

					from = archive.id;

				}

			} else if (param.startsWith("media=")) {

				media = ("media=true".equals(param)) ? 1 : 2;

			} else if (param.startsWith("utc=")) {

				String utcS = StrUtil.subAfter(param,"=",false);

				try {
					
					utc = Integer.parseInt(utcS);

				} catch (NumberFormatException ex) {

					msg.send("utc : " + utcS + " 不是一个有效的数字").exec();

					return;

				}
				
			} else if (param.startsWith("start=")) {
			
				String startS = StrUtil.subAfter(param,"=",false);
				
				try {
				
				start = DateUtil.parse(startS,"yyyy-MM-dd/HH:mm").toCalendar(TimeZone.getTimeZone("UTC")).getTimeInMillis();
				
				} catch (Exception ex) {
					
				msg.send("时间格式 : yyyy-MM-dd/HH:mm").exec();
				
				return;
					
				}
				
			} else if (param.startsWith("--end")) {
				
				String endS = StrUtil.subAfter(param,"=",false);

				try {

					end = DateUtil.parse(endS,"yyyy-MM-dd/HH:mm").toCalendar(TimeZone.getTimeZone("UTC")).getTimeInMillis();

				} catch (Exception ex) {

					msg.send("时间格式 : yyyy-MM-dd/HH:mm").exec();

					return;

				}
				
				
			} else {

				break;

			}

		}
		
		if (index > params.length) {
			
			msg.send("请输入查询内容 ？").exec();
			
			return;
			
		}
		
		String content = ArrayUtil.join(ArrayUtil.sub(params,index,params.length)," ");

		if (content.toCharArray().length > 100) {}
		
		SavedSearch search = new SavedSearch();

		search.user = user.id;
		
		search.content = content;
		
		if (start != -1) {
			
			search.start = start + (utc * 60 * 60 * 1000);
			
		}
		
		if (end != -1) {
			
			search.end = end + (utc * 60 * 60 * 1000);
			
		}
		
	}

}
