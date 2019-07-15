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

public class FeedFetchTask extends TimerTask {

		public static Timer rssTimer = new Timer();
		
		public static FeedFetchTask INSTANCE = new FeedFetchTask();
		
		public static void start() {
				
				rssTimer.scheduleAtFixedRate(INSTANCE,new Date(),2 * 60 * 1000);
				
		}

		public static String USER_AGENT = "NTT RSS Fetcher By Kazama Wataru (https://t.me/NTT_X)";

		public static FeedFetcher fetcher = new HttpURLFeedFetcher(MongoFeedCache.INSTANCE);

		static { fetcher.setUserAgent(USER_AGENT); }

		@Override
		public void run() {

				Set<String> sites = new HashSet<>();

				for (RssSub.ChannelRss info : RssSub.channel.getAll()) {

						sites.addAll(info.subscriptions);

				}

				for (String url : sites) {

						try {

								SyndFeed feed = fetcher.retrieveFeed(new URL(url));

								RssSub.RssInfo info = RssSub.info.getById(feed.getLink());

								if (info == null) {

										info = new RssSub.RssInfo();

										info.id = url;
										info.title = feed.getTitle();
										info.last = feed.getEntries().get(0).getLink();

										RssSub.info.setById(info.id,info);

										return;

								}

								LinkedList<String> posts = new LinkedList<>();

								for (SyndEntry entry : feed.getEntries()) {

										if (entry.getLink().equals(info.last)) {

												break;

										}

										StringBuilder post = new StringBuilder();

										post.append(Html.b(feed.getTitle()));

										post.append("\n\n");

										post.append(Html.a(entry.getTitle(),entry.getLink()));

										posts.add(post.toString());

								}

								if (posts.isEmpty()) return;

								for (RssSub.ChannelRss channel : RssSub.channel.findByField("subscriptions",info.id)) {

										for (String post : posts) {

												new Send(channel.id,post).html().exec();

										}

								}
								
								
						} catch (FetcherException e) {

						} catch (FeedException e) {

						} catch (IOException e) {
								
						} catch (IllegalArgumentException e) {}

				}


		}

}
