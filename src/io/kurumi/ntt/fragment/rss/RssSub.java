package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.rometools.fetcher.FetcherException;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import io.kurumi.ntt.db.AbsData;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import io.kurumi.ntt.fragment.rss.RssSub.ChannelRss;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.rss.RssSub.RssInfo;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.db.GroupData;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;

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

				public List<String> subscriptions;

		}

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("rss_sub","rss_list","rss_unsub","rss_unsub_all","rss_set_format");

		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {

				if (params.length == 0) { msg.invalidParams("channelId").exec(); return;}

				long channelId;

				if (NumberUtil.isNumber(params[0])) {

						channelId = NumberUtil.parseLong(params[0]);

				} else {

						String username = params[0];

						if (username.startsWith("@")) username = username.substring(1);

						GetChatResponse resp = execute(new GetChat(username));

						if (resp == null) {

								msg.send("Telegram 服务器连接错误").exec();

								return;

						} else if (!resp.isOk()) {

								msg.send("错误 : BOT不在该频道 : @" + username,"( " + resp.description() + " )").exec();

								return;

						}

						channelId = resp.chat().id();

				}

				if (!GroupAdmin.fastAdminCheck(channelId,user.id)) {

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

				if ("rss_sub".equals(function)) {

						if (params.length < 2) {

								msg.invalidParams("channelId","rssUrl").exec();

								return;

						}

						try {

								SyndFeed feed = FeedFetchTask.fetcher.retrieveFeed(new URL(params[1]));

								if (conf.subscriptions.contains(params[1])) {

										msg.send("已经订阅过了 " + Html.a(feed.getTitle(),feed.getLink())).html().exec();

										return;

								}

								conf.subscriptions.add(params[1]);

								channel.setById(channelId,conf);

								RssInfo rss = new RssSub.RssInfo();

								rss.id = params[1];
								rss.title = feed.getTitle();
								rss.last = feed.getEntries().get(0).getLink();

								info.setById(rss.id,rss);

								msg.send("订阅成功 : " + Html.a(feed.getTitle(),feed.getLink())).html().exec();

						} catch (FetcherException e) {

								msg.send("拉取出错 : " + e.getMessage()).exec();

								return;

						} catch (FeedException e) {

								msg.send("拉取出错 : " + e.getMessage()).exec();

						} catch (IOException e) {

								msg.send("拉取出错 : " + e.getMessage()).exec();

						} catch (IllegalArgumentException e) {

								msg.send("无效的RSS链接").exec();

						}

				} else if ("rss_list".equals(function)) {

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

				} else if ("rss_unsub".equals(function)) {

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

				} else if ("rss_unsub_all".equals(function)) {

						if (conf.subscriptions.isEmpty()) {

								msg.send("没有订阅任何源 :)").exec();

						} else {

								conf.subscriptions.clear();

								channel.setById(conf.id,conf);

								msg.send("已经全部取消 :)").exec();

						}

				} else if ("rss_set_format".equals(function)) {

						if (params.length < 2) {

								msg.invalidParams("channelId","[link|full]").exec();

								return;

						}

						if ("full".equals(params[1])) {

								conf.format = 1;

								msg.send("格式已设定为 全文 .").exec();

						} else {

								msg.send("格式已设定为 默认 .").exec();

								conf.format = 0;

						}

						channel.setById(conf.id,conf);



				}

		}

}
