package io.kurumi.ntt.utils;

import cn.hutool.core.util.*;
import java.util.*;
import twitter4j.*;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.mongodb.client.FindIterable;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.fragment.twitter.tasks.TrackTask;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import java.io.File;

public class NTT {

	/*

	 public static long[] getChatMembers(Long chat) {

	 TLRequestMessagesGetFullChat getFullChat = new TLRequestMessagesGetFullChat();

	 getFullChat.setChatId(chat.intValue());

	 try {

	 TLMessagesChatFull resp = Launcher.INSTANCE.mtp.getKernelComm().doRpcCallSync(getFullChat);

	 if (resp != null) {

	 long[] users = new long[resp.getUsers().size()];

	 for (int index = 0;index < resp.getUsers().size();index ++) {

	 users[index] = resp.getUsers().get(index).getId();

	 }

	 return users;

	 }

	 } catch (ExecutionException e) {

	 BotLog.info("getfulchat error",e);

	 } catch (RpcException e) {

	 BotLog.info("getfulchat error",e);

	 }

	 return null;

	 }

	 */
	 
	public static LinkedList<User> lookupUsers(Twitter api,LinkedList<Long> users) throws TwitterException {

		LinkedList<User> results = new LinkedList<>();

		while (!users.isEmpty()) {

			List<Long> target;

			if (users.size() > 100) {

				target = new LinkedList<Long>(users.subList(0,100));
				users.removeAll(target);

			} else {

				target = new LinkedList<>();
				target.addAll(users);

				users.clear();

			}

			try {

				ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

				results.addAll(result);

			} catch (TwitterException e) {

				if (e.getErrorCode() == 17) {

					for (Long da : target) {

						UserArchive.saveDisappeared(da);

					}

				} else throw e;

			}

		}

		return results;

	}
	 
	public static UserArchive findUser(Twitter api,String idOrName) throws TwitterException {
		
		if (NumberUtil.isNumber(idOrName)) {
			
			return UserArchive.save(api.showUser(NumberUtil.parseLong(idOrName)));
			
		} else {
			
			return UserArchive.save(api.showUser(NTT.parseScreenName(idOrName)));
			
		}
		
	}
	 
	public static Date nextHour(int offset) {
		
		Date next = new Date();
		
		next.setMinutes(0);
		next.setSeconds(0);
		
		int nextHour = next.getHours();
		
		nextHour += offset;
		
		if (nextHour > 23) {
			
			next.setHours(nextHour - 24);
			next.setDate(next.getDate() + 1);
			
		} else {
			
			next.setHours(nextHour);
			
		}
		
		return next;
		
	}

    public static long telegramToTwitter(Twitter api, String fileId, String fileName, int type) throws TwitterException {

        File file = Launcher.INSTANCE.getFile(fileId);

        if (type == 2) {

            File converted = new File(Env.CACHE_DIR, "tg_gif/" + fileId + ".gif");

            if (!converted.isFile()) {

                File globalPalettePic = FFMpeg.getGifPalettePic(file);

                FFMpeg.toGif(globalPalettePic, file, converted);

                FileUtil.del(globalPalettePic);

            }

            if (converted.length() < 15 * 1024 * 1024) {

                file = converted;

                fileName = file.getName();

            }


        }

        if (type == 0 || type == 2) {

            return api.uploadMedia(fileName, IoUtil.toStream(file)).getMediaId();


        } else {

            return api.uploadMediaChunked(fileName, IoUtil.toStream(file)).getMediaId();

        }

    }

    public static boolean testSearchBan(Twitter api, UserArchive archive) throws TwitterException {

        QueryResult result = api.search(new twitter4j.Query("from:" + archive.screenName));

        return result.getCount() == 0;

    }

    public static boolean testThreadBan(Twitter api, UserArchive archive) throws TwitterException {

        ResponseList<Status> tl = api.getUserTimeline(archive.id, new Paging().count(200));

        for (Status status : tl) {

            if (status.getQuotedStatus() != null) {

                QueryResult result = api.search(new twitter4j.Query("from:" + archive.screenName + " to:" + status.getQuotedStatus().getUser().getScreenName()).sinceId(status.getId()).maxId(status.getId()));

                return result.getCount() == 0;

            }

        }

        return false;

    }

    public static boolean testSearchSuggestionBan(Twitter api, UserArchive archive) throws TwitterException {

        ResponseList<twitter4j.User> result = api.getUserSuggestions(archive.screenName);

        for (twitter4j.User user : result) {

            if (archive.id.equals(user.getId())) {

                return false;

            }

        }

        return true;

    }
	
	public static class Accessable {
		
		public TAuth auth;
		public ResponseList<Status> timeline;

		public Accessable(TAuth auth,ResponseList<Status> timeline) {
			this.auth = auth;
			this.timeline = timeline;
		}
		
	}

    public static Accessable loopFindAccessable(Object idOrScreenName) {

        long targetL = NumberUtil.isNumber(idOrScreenName.toString()) ? NumberUtil.parseLong(idOrScreenName.toString()) : -1;
        String targetS = idOrScreenName.toString();

        for (TAuth auth : TAuth.data.collection.find()) {

            Twitter api = auth.createApi();

            try {

                UserArchive user = UserArchive.save(targetL == -1 ? api.showUser(targetS) : api.showUser(targetL));

                if (user.isProtected) {

                    FindIterable<TrackTask.IdsList> accs = TrackTask.friends.findByField("ids", user.id);

                    for (TrackTask.IdsList acc : accs) {

                        TAuth newAuth = TAuth.getById(acc.id);

                        if (newAuth != null) return new Accessable(newAuth,null);

                    }

                    return null;

                } else {

                    return new Accessable(auth,api.getUserTimeline(user.id, new Paging().count(200)));

                }

            } catch (TwitterException e) {

                if (ArrayUtil.contains(new int[]{17, 34, 50}, e.getErrorCode())) {

                    return null;

                }

            }

        }

        return null;

    }

    public static String parseTwitterException(TwitterException exc) {

        switch (exc.getStatusCode()) {

            case 410:
                return "这个操作已经不存在了";

            case 413:
                return "这是个官方文档都没写的错误";

           case TwitterException.TOO_MANY_REQUESTS:
                return "服务器繁忙";

            case TwitterException.ENHANCE_YOUR_CLAIM:
                return "NTT被限制 : 请联系开发者";

        }

        switch (exc.getErrorCode()) {

            case 17:
            case 50:
                return "账号不存在";

			case 32:
				
				return "无法认证";
				
            case 34:
                return "找不到内容";

            case 63:
                return "账号被冻结";

            case 64:
                return "账号被限制";

            case 87:
                return "使用的API无权限进行此操作 : 请联系开发者";

            case 88:
                return "超过接口调用上限 : 通常是十五分钟内的限制，请稍后再试";

            case 89:
            case 99:
            case 215:
                return "使用的API被取消了授权 或 你的账号被停用 / 冻结";

            case 93:
                return "使用的API无权操作私信 如果重新认证账号仍无法操作，请联系开发者";

            case 130:
                return "Twitter服务器超载 请稍后再试";

            case 131:
                return "Twitter服务器内部问题 请稍后再试";

            case 135:
                return "服务器时间戳错误，请联系开发者";

            case 136:
                return "被对方屏蔽";

            case 139:
                return "已经喜欢过了这条推文";

            case 144:
                return "推文不存在";

            case 150:
                return "没有关注对方，无法发送私信";

            case 151:
                return "发送私信错误 : " + exc.getMessage();

            case 160:
                return "已经发送过关注请求了";

            case 161:
                return "超过用户单日关注上限 : " + exc.getMessage();

            case 179:
                return "对方锁推且未被关注";

            case 185:
                return "发推数量超过上限";

            case 186:
                return "文本太长 限制为 180 字";

            case 187:
                return "无法发送推文, 与上一条重复.";

            case 205:
                return "操作失败，请稍后再试 : 你被jvbao了";

            case 226:
                return "操作失败 : Twitter认为这是程序自动进行的恶意操作";

            case 261:
                return "使用的API无权进行写操作 : 请联系开发者";

            case 271:
                return "不能对你自己静音";

            case 272:
                return "没有对这个用户静音";

            case 323:
                return "同时发送多张图片时 不允许其他媒体 (指视频或Gif)";

            case 324:
                return "视频太短或媒体文件过期 : " + exc.getErrorMessage();

            case 326:
                return "账号被Twitter限制 : 必须登录Twitter网站/客户端来解除这个限制 : 这通常需要验证手机 \n\n如果Twitter确认违反了规定，可能需要等待至少十二个小时的时间来恢复除了给关注者私信以外的功能。";

            case 327:
                return "已经转推过了这条推文";

            case 349:
                return "不被允许发送消息给对方";

            case 354:
                return "发送失败 : 私聊消息字数超过限制";

            case 385:
                return "不能回复一条你不可查看或已被删除的推文";

            case 416:
                return "使用的API无效/被停用 : 通常是因为开发者账号被停用/冻结 或开发者人工删除";

            default:
                return "其他错误 请联系开发者 : " + exc.getErrorCode() + " " + exc.getMessage();

        }

    }

    public static boolean isUserContactable(Fragment f, long id) {

        SendResponse resp = new Send(f, id, "test_user_ontactable").disableNotification().exec();

        if (resp == null || !resp.isOk()) return false;

        new Msg(f, resp.message()).delete();

        return true;

    }

    /*

	 public static boolean checkUserNonAuth(UserData user,Msg msg) {

	 String nonAuthMsg = msg.isPrivate() ? "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 使用 /login ~" : "乃还没有认证Twitter账号 (ﾟ〇ﾟ ; 私聊BOT使用 /login ~";

	 if (!TAuth.exists(user.id)) {

	 if (msg instanceof Callback) {

	 ((Callback)msg).alert(nonAuthMsg);

	 } else {

	 msg.send(nonAuthMsg).publicFailed();

	 }



	 return true;

	 }

	 if (!TAuth.avilable(user.id)) {

	 synchronized (TAuth.auth) {

	 TAuth.auth.remove(user.id.toString());

	 }

	 TAuth.saveAll();

	 msg.send("乃的认证可能已经被取消... 请使用 /login 重新认证 :(").exec();

	 return true;

	 }

	 return false;

	 }

	 */
	 
	public static boolean checkDropped(UserData user,Msg msg) {
		
		if (!user.blocked()) return false;
		
		// do domething
		
		return true;
		
	}

    public static boolean checkNonContactable(UserData user, Msg msg) {

        String notContactableMsg = "咱无法给乃发送信息呢，请私聊点击 'start' 启用咱 ~";

        if (!msg.isPrivate() && !isUserContactable(msg.fragment, user.id)) {

            if (msg instanceof Callback) {

                ((Callback) msg).alert(notContactableMsg);

            } else {

                msg.send("{}\n{}",user.userName(), notContactableMsg).publicFailed();

            }

            return true;

        }

        return false;

    }

    public static String parseScreenName(String input) {

        if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input, "twitter.com/", true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input, "?", false);

            }

            if (input.contains("/")) {

                input = StrUtil.subBefore(input, "/", false);

            }

        }

        if (input.contains("@")) {

            input = StrUtil.subAfter(input, "@", false);

        }

        return input;

    }

    public static long parseStatusId(String input) {

        Long statusId = -1L;

		if (input == null) return statusId;
		
        if (NumberUtil.isLong(input)) {

            statusId = NumberUtil.parseLong(input);

        } else if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input, "status/", true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input, "?", false);

            }

            if (NumberUtil.isNumber(input)) {

                statusId = NumberUtil.parseLong(input);

            }

        }

        return statusId;

    }

    public static void tryDelete(final long delay, final Msg... messages) {

        BotFragment.mainTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                for (Msg message : messages) {

                    message.fragment.execute(new DeleteMessage(message.chatId(), message.messageId()));

                }

            }

        }, new Date(System.currentTimeMillis() + delay));

    }

    public static boolean isGroupAdmin(Long chatId, Long userId) {

        return isGroupAdmin(Launcher.INSTANCE, chatId, userId);

    }

    public static boolean isGroupAdmin(Fragment fragment, Long chatId, long userId) {

        if (ArrayUtil.contains(Env.ADMINS, (int)userId)) return true;

        GetChatMemberResponse resp = fragment.bot().execute(new GetChatMember(chatId, (int)userId));

        if (resp.isOk() && ((resp.chatMember().status() == ChatMember.Status.administrator) || resp.chatMember().status() == ChatMember.Status.creator)) {

            return true;

        }

        return false;

    }

    public static boolean checkPrivate(Msg msg) {

        if (!msg.isPrivate()) {

            msg.send("请使用私聊 ( ˶‾᷄࿀‾᷅˵ )").publicFailed();

            return true;

        }

        return false;

    }

    public static boolean checkGroup(Msg msg) {

        if (!msg.isGroup()) {

            msg.send("请在群组使用 ( ˶‾᷄࿀‾᷅˵ )").exec();

            return true;

        }

        return false;

    }

    public static boolean checkGroupAdmin(Msg msg) {

        if (msg.from().admin() ||isGroupAdmin(msg.fragment, msg.chatId(), msg.from().id)) {

            return false;

        }

        if (msg instanceof Callback) {

            ((Callback) msg).alert("乃不是绒布球 Σ( ﾟωﾟ");

        } else {

            msg.send("乃不是绒布球 Σ( ﾟω。").publicFailed();

        }

        return true;

    }

    public static String checkCommand(Msg msg) {

        if (msg.isCommand()) {

            return msg.command();

        }

        return "-1";

    }

}
