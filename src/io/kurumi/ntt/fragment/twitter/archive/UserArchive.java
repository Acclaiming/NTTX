package io.kurumi.ntt.fragment.twitter.archive;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.tasks.TrackTask;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class UserArchive {

    public static Data<UserArchive> data = new Data<UserArchive>(UserArchive.class);
    public Long id;
    public Long createdAt;
	public Long lastUpdate;
    public String name;
    public String screenName;
    public String bio;
    public String photoUrl;
    public String bannerUrl;
    public String url;
    public Boolean isProtected;
    public Boolean isDisappeared;
	public Long disappearedAt;
	
	public String location;
	public Integer following;
	public Integer followers;
	
	public Integer statuses;
	public Integer likes;
	
	public String lang;
	
    public transient String oldPhotoUrl;
    public transient String oldBannerUrl;
    private transient String oldScreename;

	public static LinkedList<UserArchive> lookupAll(Twitter api,LinkedList<Long> ids) {

		LinkedList<UserArchive> all = new LinkedList<>();

		while (ids != null && !ids.isEmpty()) {

			List<Long> target;

			if (ids.size() > 100) {

				target = CollectionUtil.sub(ids,0,100);
				ids.removeAll(target);

			} else {

				target = new LinkedList<>();
				target.addAll(ids);

				ids.clear();

			}

			try {

				ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

				for (User tuser : result) {

					target.remove(tuser.getId());

					all.add(UserArchive.save(tuser));

				}

				for (Long da : target) {

					if (contains(da)) {

						all.add(get(da));

					}

				}

			} catch (TwitterException e) {

			}

		}
		
		return all;
		
	}

    public static UserArchive show(Twitter api,Long id) {

        try {

            User user = api.showUser(id);

            return save(user);

        } catch (TwitterException e) {

            return get(id);

        }

    }
	
	public static UserArchive get(Twitter api,Long id) {

        try {
			
			if (contains(id)) return get(id);

            User user = api.showUser(id);

            return save(user);

        } catch (TwitterException e) {

            return null;

        }

    }

    public static UserArchive show(Twitter api,String screenName) {

        try {

            User user = api.showUser(screenName);

            return save(user);

        } catch (TwitterException e) {

            return get(screenName);

        }

    }

    public static UserArchive get(Long id) {

        return data.getById(id);

    }

    public static UserArchive get(String screenName) {

        return data.collection.find(regex("screenName",ReUtil.escape(screenName),"i")).first();

    }

    public static boolean contains(Long id) {

        return data.containsId(id);

    }

    public static boolean contains(String screenName) {

        return data.collection.count(regex("screenName",ReUtil.escape(screenName),"i")) > 0;

    }

    public static UserArchive save(User user) {

        if (user == null) return null;

        UserArchive archive;

        if (data.containsId(user.getId())) {

            archive = data.getById(user.getId());

            if (archive.read(user)) data.setById(archive.id,archive);

        } else {

            archive = new UserArchive();

            archive.isDisappeared = false;

            archive.id = user.getId();

            archive.read(user);

            data.setById(user.getId(),archive);

        }


        return archive;

    }

    public static void saveDisappeared(Long da) {

        UserArchive user = data.getById(da);

        if (user != null) {

            user.read(null);

            data.setById(da,user);

        }

    }

    public String oldScreenName() {

        return oldScreename == null ? screenName : oldScreename;

    }

    public boolean read(User user) {

		boolean change = false;
        StringBuilder str = new StringBuilder();
        String split = "\n--------------------------------\n";

        if (user == null && !isDisappeared) {

            isDisappeared = true;
			
			disappearedAt = System.currentTimeMillis();

            TrackTask.onUserChange(this,split + "用户被冻结或已停用 :)");

            if (Env.TEP_CHANNEL != -1 && TEPH.needPuhlish(this)) {

				/*

				 File photo = new File(Env.CACHE_DIR,"twitter_profile_images/" + FileUtil.getName(photoUrl));

				 if (!photo.isFile()) {

				 HttpUtil.downloadFile(photoUrl,photo);

				 }

				 */

                String notice = "#推友消失 \n\n" + formatToChannel();



				/*

				 if (photo.isFile()) {

				 Launcher.INSTANCE.execute(new SendPhoto(Env.TEP_CHANNEL,photo).caption(notice).parseMode(ParseMode.HTML));

				 } else {

				 */

                new Send(Env.TEP_CHANNEL,notice).html().exec();

                //}

            }

            return true;

        }

        if (user == null && isDisappeared) {

            return false;

        }


        String nameL = name;

        if (!(name = user.getName()).equals(nameL)) {

            str.append(split).append("名称更改 : ").append(nameL).append(" ------> ").append(name);

            change = true;

        }

        String screenNameL = screenName;

        if (!(screenName = user.getScreenName()).equals(screenNameL)) {

            str.append(split).append("用户名更改 : @").append(screenNameL).append(" ------> @").append(screenName);

            oldScreename = screenNameL;

            change = true;

        }

        String bioL = bio;

		String newBio = user.getDescription();

		for (URLEntity entry : user.getDescriptionURLEntities()) {

			newBio = newBio.replace(entry.getURL(),entry.getExpandedURL());

		}

        if ((bio == null || !bio.contains("://t.co/")) && !ObjectUtil.equal(bio = newBio,bioL)) {

            str.append(split).append("简介更改 : \n\n").append(bioL).append(" \n\n ------> \n\n").append(bio);

            change = true;

        }

        oldPhotoUrl = photoUrl;

        if ((!ObjectUtil.equal(photoUrl = user.getOriginalProfileImageURLHttps(),oldPhotoUrl))) {

            str.append(split).append("头像更改 : " + Html.a("新头像",photoUrl));

            change = true;

        } else oldPhotoUrl = null;

        Boolean protectL = isProtected;

        if (protectL != (isProtected = user.isProtected())) {

            str.append(split).append("保护状态更改 : ").append(isProtected ? "开启了锁推" : "关闭了锁推");

            change = true;

        }

        oldBannerUrl = bannerUrl;

        if (!ObjectUtil.equal(bannerUrl = user.getProfileBannerURL(),oldBannerUrl)) {

            str.append(split).append("横幅更改 : ");

			if (photoUrl != null) {

				str.append(Html.a("新横幅",photoUrl));

			} else {

				str.append("移除横幅");

			}

            change = true;

        } else oldBannerUrl = null;


        String urlL = url;

		String newUrl = user.getURL();

		if (newUrl != null && user.getURLEntity() != null) {

			URLEntity entry = user.getURLEntity();

			newUrl = newUrl.replace(entry.getURL(),entry.getExpandedURL());

		}

        if ((urlL == null || !urlL.contains("://t.co/")) && !ObjectUtil.equal(url = newUrl,urlL)) {

            str.append(split).append("链接更改 : \n\n").append(urlL).append(" \n\n ------> \n\n").append(url);

            change = true;

        }

        if (createdAt == null) {

            createdAt = user.getCreatedAt().getTime();

            change = false;

        }

		if (isDisappeared) {

            isDisappeared = false;

            str.append(split).append("用户被取消了冻结/重新启用 :)");

            if (Env.TEP_CHANNEL != null && TEPH.needPuhlish(this)) {

				new Send(Env.TEP_CHANNEL,"#推友回档\n\n{}",formatToChannel()).html().async();

			}

			change = true;

        }

		following = user.getFriendsCount();
		followers = user.getFollowersCount();
		location = user.getLocation();
		statuses = user.getStatusesCount();
		likes = user.getFavouritesCount();
		lang = user.getLang();
		
		lastUpdate = System.currentTimeMillis();

		change = change && !(TAuth.data.containsId(id) && ArrayUtil.contains(Env.ADMINS,TAuth.getById(id).user.intValue()));
		
        if (change) {

            TrackTask.onUserChange(this,str.toString());

        }

        return change;

    }

	public String bName() {

		return Html.b(name) + " " + Html.a("@" + screenName,url());

	}

	public String formatSimple() {

		String message = "ID : " + Html.code(id);

		message += "\nName : " + name;

		message += "\nSN : " + Html.a("@" + screenName,url());

		return message;

	}

	public String formatToChannel() {

		String message = urlHtml() + " (#" + screenName + ")";

		if (isProtected) message += " [ 锁推 ]";

		if (!StrUtil.isBlank(bio)) {

			message += "\n\nBIO : " + bio + "\n";

		}

		message += "\nUID : " + Html.code(id);

		if (!(StrUtil.isBlank(url))) {

			message += "\n个人链接 : " + url;

		}

		if (followers != null) {

			message += "\n" + following + " 正在关注   " + followers + " 关注者";

		}

		message += "\n加入时间 : " + DateUtil.formatChineseDate(new Date(createdAt),false);

		return message;

	}

    public String urlHtml() {

        return Html.a(name.replaceAll(" ?\n ?",""),url());

    }

    public String url() {

        return "https://twitter.com/" + screenName;

    }


}
