package io.kurumi.ntt.fragment.rss;

import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.SyndFeedInfo;
import java.net.URL;
import io.kurumi.ntt.db.AbsData;
import io.kurumi.ntt.fragment.rss.MongoFeedCache.Cache;
import java.io.Serializable;
import cn.hutool.core.util.ObjectUtil;

public class MongoFeedCache implements FeedFetcherCache {

		public static AbsData<String,Cache> data = new AbsData<String,Cache>("FeedCache",Cache.class);

		public static MongoFeedCache INSTANCE = new MongoFeedCache();
		
		public static class Cache {

				public String url;
				public byte[] info;

		}

		@Override
		public SyndFeedInfo getFeedInfo(URL url) {

				Cache cache =  data.getById(url.toString());

				if (cache == null) return null;

				return ObjectUtil.unserialize(cache.info);

		}

		@Override
		public void setFeedInfo(final URL urlToSet,final SyndFeedInfo infoToSet) {

				data.setById(urlToSet.toString(),new Cache() {{
						
						this.url = urlToSet.toString();
						this.info = ObjectUtil.serialize(infoToSet);
						
				}});
				
		}

		@Override
		public void clear() {
				
				data.collection.drop();
				
		}

		@Override
		public SyndFeedInfo remove(URL url) {
	
				Cache cache =  data.getById(url.toString());

				if (cache == null) return null;
				
				data.deleteById(url.toString());
				
				return cache.info;
				
		}

}
