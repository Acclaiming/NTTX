package io.kurumi.ntt.fragment.twitter.track;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserTrackTask extends Thread {

		public HashSet<Long> waitFor = new HashSet<>();

		@Override
		public void run() {

				while (!isInterrupted()) {

						for (TrackTask.IdsList ids : TrackTask.friends.collection.find()) {

								waitFor.addAll(ids.ids);

						}

						for (TrackTask.IdsList ids : TrackTask.followers.collection.find()) {

								waitFor.addAll(ids.ids);

						}

						List<TAuth> allAuth = TAuth.data.getAll();

						Iterator<TAuth> iter = allAuth.iterator();

						while (allAuth != null && !waitFor.isEmpty()) {

								if (!iter.hasNext()) iter = allAuth.iterator();

								List<Long> target;

								if (waitFor.size() > 100) {

										target = CollectionUtil.sub(waitFor,0,100);
										waitFor.removeAll(target);

								} else {

										target = new LinkedList<>();
										target.addAll(waitFor);

										waitFor.clear();

								}

								try {

										ResponseList<User> result = iter.next().createApi().lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

										for (User tuser : result) {

												target.remove(tuser.getId());

												UserArchive.save(tuser);

										}

										for (Long da : target) {

												UserArchive.saveDisappeared(da);
												
										}

								} catch (TwitterException e) {

										if (e.getErrorCode() == 17) {

												for (Long da : target) {

														UserArchive.saveDisappeared(da);

												}

										} else {

												waitFor.addAll(target);

										}

								}

						}


				}

		}

}
