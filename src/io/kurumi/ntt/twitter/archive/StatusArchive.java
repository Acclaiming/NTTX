package io.kurumi.ntt.twitter.archive;



import cn.hutool.core.util.*;
import cn.hutool.http.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import twitter4j.Status;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.abs.*;

public class StatusArchive {

    public static Data<StatusArchive> data = new Data<StatusArchive>(StatusArchive.class);

    public static StatusArchive get(Long id) { return data.getById(id); }
    public static boolean contains(Long id) { return data.containsId(id); }

    public static StatusArchive save(Status status) {

		if (status == null) return null;

        StatusArchive archive = data.getById(status.getId());

        if (archive == null) {

            archive = new StatusArchive();

            archive.id = status.getId();

            archive.read(status);

            data.setById(archive.id,archive);

        }

        return archive;

    }

	public static StatusArchive save(Status status,Twitter api) {

        StatusArchive archive = data.getById(status.getId());

        if (archive == null) {

            archive = new StatusArchive();

            archive.id = status.getId();

            archive.read(status);

			archive.loop(api);

            data.setById(archive.id,archive);

        }

        return archive;

    }

    public Long id;

	public Long createdAt;

	public String text;

	public Long from;

	public Long inReplyToStatusId;

	public String inReplyToScreenName;

    public Long inReplyToUserId;

	public Long quotedStatusId;

	public LinkedList<String> mediaUrls;

    public Boolean isRetweet;

    public Long retweetedStatus;

	public LinkedList<Long> userMentions;

    public void read(Status status) {

        createdAt = status.getCreatedAt().getTime();

        text = status.getText();

		userMentions = new LinkedList<>();

		for (UserMentionEntity mention : status.getUserMentionEntities()) {

			if (text.startsWith("@" + mention.getScreenName() + " ")) {

				userMentions.add(mention.getId());

				text = StrUtil.subAfter(text,"@" + mention.getScreenName() + " ",false);

			}

		}

        from = status.getUser().getId();

        UserArchive.save(status.getUser());

        inReplyToStatusId = status.getInReplyToStatusId();

        inReplyToScreenName = status.getInReplyToScreenName();

        inReplyToUserId = status.getInReplyToUserId();

        quotedStatusId = status.getQuotedStatusId();

        if (quotedStatusId != -1 && !StatusArchive.contains(quotedStatusId)) {

            StatusArchive.save(status.getQuotedStatus());

        }

		for (URLEntity url : status.getURLEntities()) {

			if (text.endsWith(url.getURL()) && quotedStatusId != -1) {

				text = StrUtil.subBefore(text,url.getURL(),true);

			} else {

				text.replace(url.getURL(),url.getExpandedURL());

			}

		}

        mediaUrls = new LinkedList<>();

        for (MediaEntity media : status.getMediaEntities()) {

            mediaUrls.add(media.getMediaURL());

        }

        isRetweet = status.isRetweet();

        if (isRetweet) {

            retweetedStatus = status.getRetweetedStatus().getId();

            if (!StatusArchive.contains(retweetedStatus)) {

                StatusArchive.save(status.getRetweetedStatus());

            }

        } else {

            retweetedStatus = -1L;

        }

	}

    public UserArchive user() {

        UserArchive user = UserArchive.get(from);

		return user;

    }

    public String url() {

        return "https://twitter.com/" + user().screenName + "/status/" + id;

    }

    public String htmlURL() {

        return Html.a(StrUtil.padAfter(text,5,"..."),url());

    }

	public transient String split_tiny = "---------------";
    public transient String split = "\n\n" + split_tiny + split_tiny + "\n\n";

    public String toHtml() {

        return toHtml(-1);

    }

	public int depth() {

		int depth = 0;

		if (inReplyToStatusId != -1) {

			depth ++;

			StatusArchive inReplyTo = StatusArchive.get(inReplyToStatusId);

			if (inReplyTo != null) {

				depth += inReplyTo.depth();

			}

		}

		return depth;

	}

	public String toHtml(int depth) {

		return toHtml(depth,false,true);

	}

    public String toHtml(int depth,boolean quoted,boolean current) {

        StringBuilder archive = new StringBuilder();

		if (!quoted && inReplyToStatusId != -1) {

            if (depth != 0) {

                StatusArchive inReplyTo = StatusArchive.get(inReplyToStatusId);

                if (inReplyTo != null) {

                    archive.append(inReplyTo.toHtml(depth > 0 ? depth - 1 : depth,false,false));

                } else {

                    archive.append(notAvilableStatus(inReplyToUserId,inReplyToScreenName));

                }

                archive.append(split);

            }

            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("回复",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id));


        } else if (!quoted && isRetweet) {

            StatusArchive retweeted = StatusArchive.get(retweetedStatus);

            archive.append(user().urlHtml()).append(" 转推从 " + retweeted.user().urlHtml()).append(" 的 ").append(Html.a("推文",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id)).append(" : ");

			archive.append(split);

            archive.append(retweeted.toHtml(depth > 0 ? depth - 1 : depth,false,false));

            return archive.toString();

        } else {

            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("推文",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id));

		}

        String content = text;

        if (!mediaUrls.isEmpty()) {

            content = StrUtil.subBefore(content,"https://t.co",true);

        }

		if (!userMentions.isEmpty() && !quoted) {

			archive.append(" 给 ");

			archive.append(UserArchive.get(userMentions.get(0)).urlHtml());

			if (userMentions.size() > 1) {

				archive.append(" 和另外" + (userMentions.size() - 1) + "人");

			}

		}

		content = HtmlUtil.escape(content);

		content = (content + " ").replaceAll("(@.+) ","<a href=\"https://twitter.com/$1\">$1</a> ");

		content = content.substring(0,content.length() - 1);

		if (!content.trim().isEmpty()) {

			archive.append("\n\n").append(content).append("\n\n");

		}

        if (!mediaUrls.isEmpty()) {

            archive.append("\n媒体文件 :");

            for (String url : mediaUrls) {

                archive.append(Html.a(" 媒体文件",url));

            }

			archive.append("\n\n");

        }

		if (quotedStatusId != -1 && !quoted) {

			StatusArchive quotedStatus = StatusArchive.get(quotedStatusId);

			archive.append(split_tiny).append("\n");

			if (quotedStatus != null) {

				archive.append("引用 " + quotedStatus.toHtml(1,true,false));

			} else {

				archive.append("引用的推文已不可用");

			}

			archive.append(split_tiny).append("\n");

        }

		if (!quoted) {

			archive.append("在 ");

			Calendar date = Calendar.getInstance();

			date.setTimeInMillis(createdAt);

			archive.append(date.get(Calendar.YEAR) - 2000).append("年").append(date.get(Calendar.MONTH) + 1).append("月").append(date.get(Calendar.DAY_OF_MONTH)).append("日");

			archive.append(", ").append(date.get(Calendar.AM_PM) == 0 ? "上午" : "下午").append(" ").append(date.get(Calendar.HOUR)).append(":").append(date.get(Calendar.MINUTE));

		}

        return archive.toString();

    }

    String notAvilableStatus(Long id,String screenName) {

        if (UserArchive.contains(id)) {

            return UserArchive.get(id).urlHtml() + " 的 不可用的推文";

        } else {

            return Html.a("@" + screenName,"https://twitter.com/" + screenName) + " 的 不可用的推文";

        }

    }

	public StatusArchive loop(Twitter api) {

		return loop(api,false);

	}

    public StatusArchive loop(Twitter api,boolean avoid) {

        String content = text;

		if (!UserArchive.contains(from)) {

			try {

				UserArchive.save(api.showUser(from));

			} catch (TwitterException e) {

				if (!avoid) {

					TAuth accessable = NTT.loopFindAccessable(inReplyToUserId);

					if (accessable != null) {

						loop(accessable.createApi(),true);

					}

				}


			}

		}

        if (content.startsWith("@")) {

            while (content.startsWith("@")) {

                String screenName = StrUtil.subBefore(content.substring(1)," ",false);

                content = StrUtil.subAfter(content," ",false);

                if (!UserArchive.contains(screenName)) {

                    try {

                        UserArchive.save(api.showUser(screenName));

                    } catch (TwitterException ex) {

						if (!avoid) {

							TAuth accessable = NTT.loopFindAccessable(inReplyToUserId);

							if (accessable != null) {

								loop(accessable.createApi(),true);

							}

						}

					} 

                }

            }

        }


        try {

            if (inReplyToStatusId != -1) {

                if (StatusArchive.contains(inReplyToStatusId)) {

                    StatusArchive.get(inReplyToStatusId).loop(api);

                } else {

                    Status status = api.showStatus(inReplyToStatusId);

                    StatusArchive inReplyTo = StatusArchive.save(status);

                    inReplyTo.loop(api);

                }

            }

		} catch (TwitterException ex) {

			if (inReplyToUserId != -1 && !avoid) {

				TAuth accessable = NTT.loopFindAccessable(inReplyToUserId);

				if (accessable != null) {

					loop(accessable.createApi(),true);

				}

			}

		}

		try {

            if (quotedStatusId != -1) {

                if (StatusArchive.contains(quotedStatusId)) {

                    StatusArchive.get(quotedStatusId).loop(api);

                } else {

                    Status status = api.showStatus(quotedStatusId);

                    StatusArchive quoted = StatusArchive.save(status);

                    quoted.loop(api);

                }

            }

        } catch (TwitterException ex) {

			if (inReplyToUserId != -1 && !avoid) {

				TAuth accessable = NTT.loopFindAccessable(inReplyToUserId);

				if (accessable != null) {

					loop(accessable.createApi(),true);

				}

			}

		}

        return this;

    }


}
