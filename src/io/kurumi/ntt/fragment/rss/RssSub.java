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

				public List<String> subscriptions;

		}
		
		@Override
		public void init(BotFragment origin) {
				
				super.init(origin);
				
				registerFunction("rss_sub","rss_list","rss_unsub","rss_unsub_all");
				
		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {
				
				if (params.length == 0) { msg.invalidParams("channelId"); }
				
				long channelId = NumberUtil.parseLong(params[0]);
				
				GetChatMemberResponse resp = execute(new GetChatMember(channelId,user.id.intValue()));

				if (!resp.isOk()) {
						
						msg.send("错误 : " + resp.description(),"\n把机器人添加到该频道了吗？").exec();
						
						return;
						
				} else if (!(resp.chatMember().status() == ChatMember.Status.creator || resp.chatMember().status() == ChatMember.Status.administrator)) {
						
						msg.send("错误 : 你不是该频道的创建者或管理员").exec();
						
						return;
						
				}
				
				if ("rss_sub".equals(function)) {
						
						if (params.length < 2) {
								
								msg.invalidParams("channelId","rssUrl");
								
								return;
								
						}
						
						ChannelRss conf = channel.getById(channelId);

						if (conf == null) {
								
								conf = new ChannelRss();
								conf.id = channelId;
								conf.subscriptions = new LinkedList<>();
								
						}
						
						try {
								
								SyndFeed feed = FeedFetchTask.fetcher.retrieveFeed(new URL(params[1]));
								
								if (conf.subscriptions.contains(feed.getLink())) {
										
										msg.send("已经订阅过了 " + Html.a(feed.getTitle(),feed.getLink())).html().exec();
										
								}
								
								conf.subscriptions.add(feed.getLink());
								
								channel.setById(channelId,conf);
								
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

				}
				
		}
		
}
