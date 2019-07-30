package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import io.kurumi.ntt.db.AbsData;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.rometools.rome.io.WireFeedOutput;
import cn.hutool.core.io.IoUtil;
import java.io.ByteArrayOutputStream;
import cn.hutool.core.util.CharsetUtil;
import java.io.IOException;
import com.pengrad.telegrambot.request.SendDocument;

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

		public Long delay;

		public Long last;

		public static class FeedError {

			public Long startAt;

			public String errorMsg;

		}

	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		if (isMainInstance()) {

			registerFunction("rss_import","rss_export","rss_set_delay","rss_set_current","rss_sub","rss_set_copyright","rss_list","rss_unsub","rss_unsub_all","rss_set_format","rss_link_preview","rss_export");

		} else {

			registerFunction("import","export","set_delay","set_current","sub","set_copyright","list","unsub","unsub_all","set_format","link_preview","export");

		}

	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {

		return PROCESS_ASYNC;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (user.blocked()) {

			msg.send("你不能这么做 (为什么？)").async();

			return;

		}

		if (params.length == 0) { msg.invalidParams("频道","...").exec(); return;}

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

			} else if (resp.chat().type() != Chat.Type.channel) {

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
		
		if (function.endsWith("export")) {
			
			if (conf.subscriptions == null || conf.subscriptions.isEmpty()) {
				
				msg.send("这个频道还没有订阅过RSS，无法导出。").async();
				
				return;
				
			}
		
			Opml opml = new Opml();
			
			List<Outline> outlines = new LinkedList<>();
			
			for (String rss : conf.subscriptions) {
				
				String title = rss;
				
				RssInfo rssInfo = info.getById(rss);

				if (rssInfo != null) title = rssInfo.title;
				
				URL url = URLUtil.url(rss);
				
				Outline outline = new Outline(title,url,url);

				outlines.add(outline);
				
			}
			
			opml.setOutlines(outlines);
			
			WireFeedOutput output = new WireFeedOutput();
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			
			try {
				
				output.output(opml,IoUtil.getWriter(bytes,CharsetUtil.CHARSET_UTF_8));
				
				executeAsync(new SendDocument(msg.chatId(),bytes.toByteArray()).caption("rss.opml"));
				
			} catch (Exception e) {
				
				msg.send("导出失败",BotLog.parseError(e)).async();
				
				return;
				
			}
			
		} else if (function.endsWith("set_copyright")) {
			
			if (params.length == 1) {
				
				conf.copyright = null;
				
				msg.send("已重置，感谢对NTT的支持。").async();
				
			} else {
				
				conf.copyright = ArrayUtil.join(ArrayUtil.remove(params,0)," ");
				
				msg.send("设置成功 ~").async();
				
			}

		} else if (function.endsWith("set_delay")) {

			if (params.length == 1) {

				conf.delay = null;

				msg.send("已经重置为 15分钟").async();

			} else {


				long delay = 0;

				int m;

				if (!NumberUtil.isNumber(params[1]) || (m = NumberUtil.parseInt(params[1])) > 60 || m < 0) {

					msg.send("无效的分钟数量 : " + params[1]).async();

					return;

				}

				delay += m;

				if (params.length > 2) {

					int h;

					if (!NumberUtil.isNumber(params[2]) || (h = NumberUtil.parseInt(params[2])) > 24 || h < 0) {

						msg.send("无效的小时数量 : " + params[2]).async();

						return;

					}

					delay += h * 60;

				}

				if (params.length > 3) {

					int d;

					if (!NumberUtil.isNumber(params[3]) || (d = NumberUtil.parseInt(params[2])) > 7 || d < 1) {

						msg.send("无效的天数 : " + params[3]).async();

						return;

					}

					delay += d * 24 * 60;

				}

				if (delay < 15) {

					msg.send("无效的延时设置 ( < 15分钟 )").async();

					return;

				} else if (delay > 7 * 24 * 60) {

					msg.send("无效的延时设置 ( < 7天 )").async();

					return;

				}

				conf.delay = delay;

				msg.send("拉取间隔设置成功。").async();

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

				msg.invalidParams("频道","链接").exec();

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

				int limit = params.length < 3 ? -1: NumberUtil.parseInt(params[2]);

				if (limit > 0 && limit < entries.size()) {

					entries = entries.subList(entries.size() - limit,entries.size());

				}

				for (SyndEntry entry :  entries) {


					limit --;

					Send request = new Send(this,conf.id,FeedHtmlFormater.format(conf,feed,entry));

					if (conf.format > 8 || conf.preview) {

						request.enableLinkPreview();

					}

					SendResponse result = request.html().exec();

					// if (conf.format == 9) return;

					if (result != null) {

						if (result.isOk()) continue;

						msg.send(result.errorCode() + " - " + result.description()).exec();

					}

					msg.send(request.request().getText()).exec();


				}

			} catch (FeedException e) {

				msg.send("拉取出错 : ",BotLog.parseError(e)).exec();

			} catch (IllegalArgumentException e) {

				msg.send("无效的RSS链接").exec();

			}


		}

		if (function.matches("(rss_sub|sub)")) {

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

				msg.invalidParams("频道","链接");

				return;

			}

			if (conf.subscriptions.remove(params[1])) {

				msg.send("取消订阅成功").exec();

			} else {

				msg.send("没有订阅这个链接").exec();

			}

		} else if (function.endsWith("unsub_all")) {

			if (conf.subscriptions.isEmpty()) {

				msg.send("没有订阅任何源 :)").exec();

			} else {

				conf.subscriptions.clear();

				msg.send("已经全部取消 :)").exec();

			}

		} else if (function.endsWith("set_format")) {

			if (params.length < 2) {

				msg.invalidParams("频道","1 - 9").exec();

				return;

			}

			int target;

			if (!NumberUtil.isNumber(params[1]) || (target = NumberUtil.parseInt(params[1])) < 0 || target > 10) {

				msg.send("请选择有效的格式 : 1 - 10").exec();

				return;

			}

			conf.format = target;

			msg.send("修改成功 输出格式为 : " + target + " .").exec();

		} else if (function.endsWith("link_preview")) {

			if (params.length < 2) {

				msg.invalidParams("频道","on/off").exec();

				return;

			}

			if ("on".equals(params[1])) {

				conf.preview = true;

			} else if ("off".equals(params[1])) {

				conf.preview = false;

			}

			msg.send("修改成功 原文链接预览已设为" + (conf.preview ? "开启" : "关闭") + " .").exec();


		}

		channel.setById(conf.id,conf);

	}

}
