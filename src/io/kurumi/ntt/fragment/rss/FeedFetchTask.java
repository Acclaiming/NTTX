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
import com.pengrad.telegrambot.response.*;
import java.util.*;

public class FeedFetchTask extends TimerTask {

	public static Timer rssTimer = new Timer();

	public static FeedFetchTask INSTANCE = new FeedFetchTask();

	public static void start() {

		rssTimer.scheduleAtFixedRate(INSTANCE,new Date(),15 * 60 * 1000);

	}

	public static String USER_AGENT = "NTT RSS Fetcher By Kazama Wataru (https://t.me/NTT_X)";

	public static FeedFetcher fetcher = new HttpClientFeedFetcher(MongoFeedCache.INSTANCE);

	static { fetcher.setUserAgent(USER_AGENT); }

	int step = 0;

	@Override
	public void run() {

		Set<String> sites = new HashSet<>();
		Set<String> errors = new HashSet<>();
		
		for (RssSub.ChannelRss info : RssSub.channel.getAll()) {

			sites.addAll(info.subscriptions);
			errors.addAll(info.error.keySet());
			
		}
		
		if (step < 4) {

			step ++;

			sites.removeAll(errors);

		} else {

			step = 0;

		}

		next:for (String url : sites) {

			try {

				SyndFeed feed = fetcher.retrieveFeed(new URL(url));

				RssSub.RssInfo info = RssSub.info.getById(url);

				//BotLog.debug("拉取 " + feed.getTitle());

				if (info == null) {

					info = new RssSub.RssInfo();

					info.id = url;
					info.title = feed.getTitle();
					info.last = generateSign(feed.getEntries().get(0));

					RssSub.info.setById(info.id,info);

					continue next;

				}


				LinkedList<SyndEntry> posts = new LinkedList<>();

				for (SyndEntry entry : feed.getEntries()) {

					if (generateSign(entry).equals(info.last)) {

						break;

					}

					posts.add(entry);

				}

				info.last = generateSign(feed.getEntries().get(0));

				RssSub.info.setById(info.id,info);

				if (posts.isEmpty()) {

					continue next;

				}

				Collections.reverse(posts);

				for (RssSub.ChannelRss channel : RssSub.channel.findByField("subscriptions",info.id)) {

					for (SyndEntry entry : posts) {

						Send request = new Send(channel.id,FeedHtmlFormater.format(channel.format,feed,entry));

						if (channel.format == 0 || channel.preview) {

							request.enableLinkPreview();

						}

						request.html().async();

					}

				}

			} catch (FetcherException e) {

				fetchError(url,e);

			} catch (FeedException e) {

				fetchError(url,e);

			} catch (IOException e) {} catch (IllegalArgumentException e) {}



		}


	}

	void fetchError(String url,Exception e) {

		RssSub.RssInfo info = RssSub.info.getById(url);

		for (RssSub.ChannelRss channel : RssSub.channel.findByField("subscriptions",info.id)) {

			if (channel.error == null) {

				channel.error = new HashMap<>();

			}

			RssSub.ChannelRss.FeedError error;

			if (channel.error.containsKey(url)) {

				error = channel.error.get(url);

			} else {

				error = new RssSub.ChannelRss.FeedError();

				error.startAt = System.currentTimeMillis();

				error.errorMsg = e.getMessage();

			}

			if (System.currentTimeMillis() - error.startAt > 3 * 24 * 60 * 60 * 1000) {

				channel.subscriptions.remove(url);
				channel.error.remove(url);

				if (channel.error.isEmpty()) channel.error = null;

				new Send(channel.id,Html.b(info.title) + " 连续三天拉取错误，已取消订阅 (" + error.errorMsg).async();

			}

		}



	}

	String generateSign(SyndEntry entry) {

		long time = entry.getPublishedDate().getTime();

		return DigestUtil.md5Hex(time + (StrUtil.isBlank(entry.getLink()) ? entry.getTitle() : entry.getTitle() + entry.getLink()));

	}

}
