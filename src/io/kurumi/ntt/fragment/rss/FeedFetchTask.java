package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import cn.hutool.crypto.digest.*;
import cn.hutool.http.*;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;

public class FeedFetchTask extends TimerTask {

	public static Timer rssTimer = new Timer();

	public static FeedFetchTask INSTANCE = new FeedFetchTask();

	public static void start() {

		rssTimer.scheduleAtFixedRate(INSTANCE,new Date(),15 * 60 * 1000);

	}

	public static String USER_AGENT = "NTT RSS Fetcher By Kazama Wataru (https://t.me/NTT_X)";

	int step = 0;

	@Override
	public void run() {

		Set<String> sites = new HashSet<>();
		Set<String> errors = new HashSet<>();

		for (RssSub.ChannelRss info : RssSub.channel.getAll()) {

			sites.addAll(info.subscriptions);

			if (info.error != null) {

				for (Map.Entry<String,RssSub.ChannelRss.FeedError> error : info.error.entrySet()) {

					if (System.currentTimeMillis() -  error.getValue().startAt > 6 * 60 * 60 * 1000) {

						sites.add(error.getKey());

					}

				}

			}

		}

		if (step < 3) {

			step ++;

			sites.removeAll(errors);

		} else {

			step = 0;

		}

		next:for (String url : sites) {

			try {

				SyndFeedInput input = new SyndFeedInput();

				HttpResponse resp;

				try {

					resp = HttpUtil.createGet(url).header(Header.USER_AGENT,"NTT Feed Fetcher ( https://github.com/HiedaNaKan/NTTools)").execute();

				} catch (IORuntimeException ex) {

					fetchError(url,ex);

					continue;

				}

				if (!resp.isOk()) {

					StringBuilder error = new StringBuilder();

					error.append("HTTP ERROR ").append(resp.getStatus());

					String content = resp.body();

					if (!StrUtil.isBlank(content)) {

						error.append(" : \n\n");

						error.append(content);

					}

					fetchError(url,new Exception(error.toString()));

					continue;

				}

				SyndFeed feed = input.build(new StringReader(resp.body()));

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

						request.html().exec();

					}

				}

			} catch (FeedException e) {

				fetchError(url,e);

			} catch (IllegalArgumentException e) {}



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

	static String generateSign(SyndEntry entry) {

		long time = entry.getPublishedDate().getTime();

		return DigestUtil.md5Hex(time + (StrUtil.isBlank(entry.getLink()) ? entry.getTitle() : entry.getTitle() + entry.getLink()));

	}

}
