package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import cn.hutool.http.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.fragment.group.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;
import io.kurumi.ntt.*;

public class RssSub extends Fragment {

	public static AbsData<String,RssInfo> info = new AbsData<String,RssInfo>(RssInfo.class);
	public static Data<ChannelRss> channel = new Data<ChannelRss>(ChannelRss.class);

	public static class RssInfo {

		public String id;
		public String title;
		public String last;

	}

	public static class ChannelRss {

		public Long id;

		public int format = 0;

		public boolean preview = false;

		public List<String> subscriptions;

		public String copyright;
		
		public Map<String,FeedError> error;

		public Long fromBot;

		public static class FeedError {

			public Long startAt;

			public String errorMsg;

		}

	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		if (isMainInstance()) {

			registerFunction("rss_set_current","rss_sub","rss_set_copyright","rss_list","rss_unsub","rss_unsub_all","rss_set_format","rss_link_preview","rss_export");

		} else {

			registerFunction("set_current","sub","set_copyright","list","unsub","unsub_all","set_format","link_preview","export");

		}

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (user.blocked()) {

			msg.send("你不能这么做 (为什么？)").async();

			return;

		}

		if (params.length == 0) { msg.invalidParams("channelId").exec(); return;}

		long channelId;

		if (NumberUtil.isNumber(params[0])) {

			channelId = NumberUtil.parseLong(params[0]);

			GetChatResponse resp = execute(new GetChat(channelId));

			if (resp == null) {

				msg.send("Telegram 服务器连接错误").exec();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : BOT不在该频道","( " + resp.description() + " )").exec();

				return;

			} else if (resp.chat().type() != Chat.Type.channel) {
				
				msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").exec();
				
				return;
				
			}

			channelId = resp.chat().id();
		
			
		} else {

			String username = params[0];

			if (!username.startsWith("@")) username = "@" + username;

			GetChatResponse resp = execute(new GetChat(username));

			if (resp == null) {

				msg.send("Telegram 服务器连接错误").exec();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : BOT不在该频道 : @" + username,"( " + resp.description() + " )").exec();

				return;

			}else if (resp.chat().type() != Chat.Type.channel) {

				msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").exec();

				return;

			}
			

			channelId = resp.chat().id();

		}

		if (!user.admin() && !GroupAdmin.fastAdminCheck(this,channelId,user.id,false)) {

			GetChatMemberResponse resp = execute(new GetChatMember(channelId,user.id.intValue()));

			if (resp == null) {

				msg.send("Telegram 服务器连接错误").exec();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : 频道读取失败","( " + resp.description() + " )").exec();

				return;

			} else if (!(resp.chatMember().status() == ChatMember.Status.creator || resp.chatMember().status() == ChatMember.Status.administrator)) {

				msg.send("错误 : 你不是频道管理员").exec();

				return;

			}

			return;

		}

		ChannelRss conf = channel.getById(channelId);

		if (conf == null) {

			conf = new ChannelRss();
			conf.id = channelId;
			conf.subscriptions = new LinkedList<>();

		}

		if (function.endsWith("set_current")) {
			
			if (params.length == 0) {
				
				conf.copyright = null;
				
				msg.send("已还原，感谢对NTT的支持。").exec();
				
			} else {
				
				conf.copyright = ArrayUtil.join(params," ");
				
				msg.send("已还原，已设定。").exec();
				
			}
			
		} else if (function.endsWith("set_current")) {

			if (isMainInstance()) {

				conf.fromBot = null;

				msg.send("重置成功 来源为 NTT本体").exec();

			} else {


				conf.fromBot = origin.me.id();

				msg.send("设置成功 , 来源为 " + UserData.get(origin.me).userName()).html().exec();


			}

		} else if (function.endsWith("export")) {

			if (params.length < 2) {

				msg.invalidParams("channelId","rssUrl").exec();

				return;

			}

			try {

				SyndFeedInput input = new SyndFeedInput();

				String link = params[1];
				
				link = URLUtil.encode(link);
				
				HttpResponse resp;

				try {

					resp = HttpUtil.createGet(link).header(Header.USER_AGENT,"NTT Feed Fetcher ( https://github.com/HiedaNaKan/NTTools)").execute();

				} catch (HttpException ex) {

					msg.send("拉取失败 :",ex.getMessage()).async();

					return;

				}

				if (!resp.isOk()) {

					StringBuilder error = new StringBuilder();

					error.append("HTTP ERROR ").append(resp.getStatus());

					String content = resp.body();

					if (!StrUtil.isBlank(content)) {

						error.append(" : \n\n");

						error.append(content);

					}


					msg.send("拉取失败",error.toString()).async();

					return;

				}

				SyndFeed feed = input.build(new StringReader(resp.body()));

				msg.send("正在输出 " + Html.a(feed.getTitle(),feed.getLink())).html().exec();

				List<SyndEntry> entries = feed.getEntries();

				Collections.reverse(entries);

				for (SyndEntry entry :  entries) {

					Send request = new Send(this,conf.id,FeedHtmlFormater.format(conf,feed,entry));

					if (conf.format == 9 || conf.preview) {

						request.enableLinkPreview();

					}

					request.html().exec();

					// if (conf.format == 9) return;

					//msg.send(request.request().getText()).exec();

				}

			} catch (FeedException e) {

				msg.send("拉取出错 : ",BotLog.parseError(e)).exec();

			} catch (IllegalArgumentException e) {

				msg.send("无效的RSS链接").exec();

			}


		}

		if (function.endsWith("sub")) {

			if (params.length < 2) {

				msg.invalidParams("channelId","rssUrl").exec();

				return;

			}

			try {

				SyndFeedInput input = new SyndFeedInput();

				String link = params[1];

				link = URLUtil.encode(link);
				
				HttpResponse resp;

				try {

					resp = HttpUtil.createGet(link).header(Header.USER_AGENT,"NTT Feed Fetcher ( https://github.com/HiedaNaKan/NTTools)").execute();

				} catch (IORuntimeException ex) {

					msg.send("拉取失败 :",ex.getMessage()).async();

					return;

				}

				if (!resp.isOk()) {

					StringBuilder error = new StringBuilder();

					error.append("HTTP ERROR ").append(resp.getStatus());

					String content = resp.body();

					if (!StrUtil.isBlank(content)) {

						error.append(" : \n\n");

						error.append(content);

					}


					msg.send("拉取失败",error.toString()).async();

					return;

				}

				SyndFeed feed = input.build(new StringReader(resp.body()));

				if (conf.subscriptions.contains(params[1])) {

					msg.send("已经订阅过了 " + Html.a(feed.getTitle(),feed.getLink())).html().exec();

					return;

				}


				if (!isMainInstance() && !origin.me.id().equals(conf.fromBot)) {

					conf.fromBot = origin.me.id();

					if (!conf.subscriptions.isEmpty() || conf.fromBot != null) {

						msg.send("已将输入源设为本机器人。").exec();

					}

				}

				conf.subscriptions.add(params[1]);


				channel.setById(channelId,conf);

				RssInfo rss = new RssSub.RssInfo();

				rss.id = params[1];
				rss.title = feed.getTitle();
				rss.last = FeedFetchTask.generateSign(feed.getEntries().get(0));

				info.setById(rss.id,rss);

				msg.send("订阅成功 : " + Html.a(feed.getTitle(),feed.getLink())).html().exec();

			} catch (FeedException e) {

				msg.send("拉取出错 : " + BotLog.parseError(e)).exec();

			} catch (IllegalArgumentException e) {

				msg.send("无效的RSS链接").exec();

			}

		} else if (function.endsWith("list")) {

			if (conf.subscriptions.isEmpty()) {

				msg.send("没有订阅任何RSS").exec();

				return;

			}

			StringBuilder list = new StringBuilder("订阅列表 :");

			for (String url : conf.subscriptions) {

				RssInfo rss = info.getById(url);

				list.append("\n\n").append(Html.b(rss.title)).append(" : ").append(Html.code(url));

			}

			msg.send(list.toString()).html().exec();

		} else if (function.endsWith("unsub")) {

			if (params.length < 2) {

				msg.invalidParams("channelId","rssUrl");

				return;

			}

			if (conf.subscriptions.remove(params[1])) {

				msg.send("取消订阅成功").exec();

				channel.setById(conf.id,conf);

			} else {

				msg.send("没有订阅这个链接").exec();

			}

		} else if (function.endsWith("unsub_all")) {

			if (conf.subscriptions.isEmpty()) {

				msg.send("没有订阅任何源 :)").exec();

			} else {

				conf.subscriptions.clear();

				channel.setById(conf.id,conf);

				msg.send("已经全部取消 :)").exec();

			}

		} else if (function.endsWith("set_format")) {

			if (params.length < 2) {

				msg.invalidParams("channelId","1 - 9").exec();

				return;

			}

			int target;

			if (!NumberUtil.isNumber(params[1]) || (target = NumberUtil.parseInt(params[1])) < 0 || target > 9) {

				msg.send("请选择有效的格式 : 1 - 9").exec();

				return;

			}

			conf.format = target;

			channel.setById(conf.id,conf);

			msg.send("修改成功 输出格式为 : " + target + " .").exec();

		} else if (function.endsWith("link_preview")) {

			if (params.length < 2) {

				msg.invalidParams("channelId","on/off").exec();

				return;

			}

			if ("on".equals(params[1])) {

				conf.preview = true;

			} else if ("off".equals(params[1])) {

				conf.preview = false;

			}

			channel.setById(conf.id,conf);

			msg.send("修改成功 原文链接预览已设为" + (conf.preview ? "开启" : "关闭") + " .").exec();


		}

	}

}
