package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.date.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import java.util.TimeZone;

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

		long reply = -1;

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

					fromC = NTT.parseScreenName(fromC);
				
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

					to = NumberUtil.parseLong(toC);

				} else {
					
					toC = NTT.parseScreenName(toC);

					UserArchive archive = UserArchive.get(toC);

					if (archive == null) {

						msg.send("没有这个人的记录 : " + toC).exec();

						return;

					}

					to = archive.id;

				}

			} else if (param.startsWith("reply=")) {

				String replyC = StrUtil.subAfter(param,"=",false);

				reply = NumberUtil.parseLong(replyC);

			} else if (param.startsWith("media=")) {

				media = ("media=true".equals(param)) ? 1 : 2;

			} else if (param.equals("regex")) {

				regex = true;

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

			} else if (param.startsWith("end=")) {

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

		if (params.length == 0) {

			msg.send("推文查询 /search [参数...] 内容",

					 "from=<发送用户ID|用户名>","",
					 "to=<回复用户ID|用户名>","",
					 "reply=<回复推文ID>","",
					 "start=<时间上限>","",
					 "end=<时间下限>","",
					 "(格式 yyyy-MM-dd HH:mm)","",
					 "media=<true|false> (筛选是否有媒体)","",
					 "regex (使用正则表达式匹配)").publicFailed();

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

		search.reply = reply;

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

		if (!search.user.equals(user.id)) {

			callback.alert("只有发起搜索的用户可以翻页哦 (");
			return;
			
		} else {

			callback.text("正在加载...");

		}

		long count = search.count();

		callback.edit(exportContent(search,cursor)).buttons(makeButtons(searchId,count,cursor)).html().exec();

	}

	String exportContent(SavedSearch search,long cursor) {

		StringBuilder format = new StringBuilder("------------------ 查询结果 -------------------");

		for (StatusArchive archive : search.query((int)(cursor - 1) * 10,10)) {

			String text = archive.text;

			if (text.length() > 100) {

				text = StrUtil.subPre(text,27) + "...";

			}

			format.append("\n\n").append(Html.a(archive.user().name + " : " + text,"https://t.me/" + origin.me.username() + "?start=" + PAYLOAD_SHOW_STATUS + PAYLOAD_SPLIT + archive.id));

		}

		return format.toString();

	}

	String PAYLOAD_SHOW_STATUS = "status";

	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (super.onMsg(user,msg)) return true;

		if (!msg.isStartPayload() || !PAYLOAD_SHOW_STATUS.equals(msg.payload()[0])) return false;

		TAuth auth = TAuth.getById(user.id);

		Long statusId = NumberUtil.parseLong(msg.payload()[1]);

		if (auth == null) {

			StatusArchive archive = StatusArchive.get(statusId);

			if (archive == null) {

				msg.send("找不到存档...").exec();

			} else {

				msg.send(archive.toHtml()).html().exec();

			}

		} else {

			Twitter api = auth.createApi();

			msg.sendTyping();

			try {

				StatusArchive newStatus = StatusArchive.save(api.showStatus(statusId));

				newStatus.loop(api);

				msg.send(newStatus.toHtml()).html().point(1,newStatus.id);

			} catch (TwitterException e) {

				if (StatusArchive.contains(statusId)) {

					msg.send(StatusArchive.get(statusId).toHtml()).html().point(1,statusId);

				} else {

					msg.send(NTT.parseTwitterException(e)).publicFailed();

				}


			}


		}

		return true;

	}

	ButtonMarkup makeButtons(final long searchId,final long count,final long current) {

		return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				if (current > 1) {

					line.newButton(" □ ",POINT_SHOW_PAGE,searchId,1);

					line.newButton(" << ",POINT_SHOW_PAGE,searchId,current - 1);

				}

				int max = (int)count / 10;

				if (count % 10 != 0) {

					max ++;

				}

				line.newButton(current + " / " + max,"null");

				if (current < max) {

					line.newButton(" >> ",POINT_SHOW_PAGE,searchId,current + 1);

					line.newButton(" ■ ",POINT_SHOW_PAGE,searchId,max);

				}

			}};

	} 

}
