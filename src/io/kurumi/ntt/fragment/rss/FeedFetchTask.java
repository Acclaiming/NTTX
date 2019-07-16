package io.kurumi.ntt.fragment.rss;

import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.FetcherEvent;
import com.rometools.fetcher.FetcherException;
import com.rometools.fetcher.FetcherListener;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import io.kurumi.ntt.utils.BotLog;
import com.rometools.fetcher.impl.HttpClientFeedFetcher;
import java.util.Collections;
import cn.hutool.http.HtmlUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.crypto.digest.DigestUtil;
import javax.annotation.Generated;

public class FeedFetchTask extends TimerTask {

		public static Timer rssTimer = new Timer();

		public static FeedFetchTask INSTANCE = new FeedFetchTask();

		public static void start() {

				rssTimer.scheduleAtFixedRate(INSTANCE,new Date(),5 * 60 * 1000);

		}

		public static String USER_AGENT = "NTT RSS Fetcher By Kazama Wataru (https://t.me/NTT_X)";

		public static FeedFetcher fetcher = new HttpClientFeedFetcher(MongoFeedCache.INSTANCE);

		static { fetcher.setUserAgent(USER_AGENT); }

		@Override
		public void run() {

				Set<String> sites = new HashSet<>();

				for (RssSub.ChannelRss info : RssSub.channel.getAll()) {

						sites.addAll(info.subscriptions);

				}


				next:for (String url : sites) {

						try {

								//BotLog.debug("开始拉取 : " + url);

								SyndFeed feed = fetcher.retrieveFeed(new URL(url));

								RssSub.RssInfo info = RssSub.info.getById(url);

								BotLog.debug("拉取 " + feed.getTitle());

								if (info == null) {

										info = new RssSub.RssInfo();

										info.id = url;
										info.title = feed.getTitle();
										info.last = generateSign(feed.getEntries().get(0));

										RssSub.info.setById(info.id,info);

										BotLog.debug("已保存");

										continue next;

								}

								LinkedList<SyndEntry> posts = new LinkedList<>();

								for (SyndEntry entry : feed.getEntries()) {
										
										if (generateSign(entry).equals(info.last)) {

												break;

										}

										posts.add(entry);

								}

								if (posts.isEmpty()) {

										BotLog.debug("无新文章");

										continue next;

								}

								Collections.reverse(posts);

								info.last = generateSign(feed.getEntries().get(0));

								RssSub.info.setById(info.id,info);

								for (RssSub.ChannelRss channel : RssSub.channel.findByField("subscriptions",info.id)) {

										for (SyndEntry entry : posts) {

												new Send(channel.id,FeedHtmlFormater.format(channel.format,feed,entry)).html().exec();
												
										}

								}


						} catch (Exception e) {

								BotLog.error("拉取错误",e);


						}

				}


		}
		
		String generateSign(SyndEntry entry) {
				
				long time = entry.getPublishedDate().getTime();
				
				String content;
				
				if (entry.getContents() != null && !entry.getContents().isEmpty()) content= entry.getContents().get(0).getValue();
				else content = entry.getDescription().getValue();
				
				return DigestUtil.md5Hex(time + entry.getTitle() + content);
				
		}

}
