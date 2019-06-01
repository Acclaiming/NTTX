package io.kurumi.ntt.fragment.twitter.status;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.twitter.archive.*;
import cn.hutool.core.date.*;
import java.text.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.model.request.*;

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

		boolean regex = false;

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

			} else if (param.startsWith("to=")) {

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

			} else if (param.startsWith("regex=")) {

				regex = ("regex=true".equals(param));

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

			} else if (param.startsWith("end")) {

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

		search.from = from;

		search.to = to;

		search.regex = regex;

		search.user = user.id;

		search.media = media;

		search.content = content;

		if (start != -1) {

			search.start = start + (utc * 60 * 60 * 1000);

		}

		if (end != -1) {

			search.end = end + (utc * 60 * 60 * 1000);

		}

		Msg status = msg.send("正在创建查询...").send();

		msg.sendTyping();
		
		search.id = MongoIDs.getNextId(SavedSearch.class.getSimpleName());

		SavedSearch.data.setById(search.id,search);

		status.edit("创建查询√\n正在查询...").exec();

		msg.sendTyping();
		
		long count = search.count();
		
		if (count == 0) {
			
			status.edit("暂无结果...").exec();
			
			return;
			
		}
		
		status.edit(exportContent(search,1)).buttons(makeButtons(search.id,count,1)).html().exec();
		
	}

	final String POINT_SHOW_PAGE = "ss|show";

	@Override
	public void points(LinkedList<String> points) {

		points.add(POINT_SHOW_PAGE);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		callback.sendTyping();

		long searchId = Long.parseLong(params[0]);
		long cursor = Long.parseLong(params[1]);

		SavedSearch search = SavedSearch.data.getById(searchId);

		if (search == null) {

			callback.alert("此查询已过期 :(");

			return;

		}

		callback.confirm();

		long count = search.count();

		callback.edit(exportContent(search,cursor)).buttons(makeButtons(searchId,count,cursor)).html().exec();

	}

	String exportContent(SavedSearch search,long cursor) {
		
		StringBuilder format = new StringBuilder("-------- 查询结果 ---------");

		for (StatusArchive archive : search.query((int)(cursor - 1) * 10,(int)cursor * 10)) {

			String text = archive.text;

			if (text.length() > 30) {

				text = StrUtil.subPre(text,27) + "...";

			}

			format.append("\n").append(Html.a(archive.user().name + " : " + text,"https://t.me/" + origin.me.username() + "?start=" + PAYLOAD_SHOW_STATUS + "|" + archive.id));

		}
		
		return format.toString();
		
	}

	String PAYLOAD_SHOW_STATUS = "status";

	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (super.onMsg(user,msg)) return true;
		
		if (!msg.isStartPayload() || !PAYLOAD_SHOW_STATUS.equals(msg.payload()[0])) return false;

		Long statusId = NumberUtil.parseLong(msg.payload()[1]);

		StatusArchive archive = StatusArchive.get(statusId);

		if (archive == null) {

			msg.send("找不到存档...").exec();

		} else {

			msg.send(archive.toHtml()).html().exec();

		}

		return true;

	}

	ButtonMarkup makeButtons(final long searchId,final long count,final long current) {

		return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				if (current > 1) {

					line.newButton(" << ",POINT_SHOW_PAGE,searchId,current - 1);

				}

				int max = (int)count / 10;

				if (count % 10 != 0) {

					max ++;

				}

				line.newButton(current + " / " + max,"null");

				if (current < max) {

					line.newButton(" >> ",POINT_SHOW_PAGE,searchId,current + 1);

				}

			}};

	} 

}
