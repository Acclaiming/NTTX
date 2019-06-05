package io.kurumi.ntt.utils;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import com.mongodb.client.*;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;
import io.kurumi.ntt.twitter.archive.*;
import java.io.*;

public class NTT {

 static AbsData<String,TgMedia> media = new AbsData<String,TgMedia>(TgMedia.class);
	
 static class TgMedia {
	 
	 public String id;
	 public long mediaId;
	 
 }
	
	public static long telegramToTwitter(Twitter api,String fileId) throws TwitterException {
		
		if (media.containsId(fileId)) {
			
			return media.getById(fileId).mediaId;
			
		}
		
		TgMedia file = new TgMedia();
		
		file.id = fileId;
		
		file.mediaId = api.uploadMedia(Launcher.INSTANCE.getFile(fileId)).getMediaId();
		
		media.setById(file.id,file);
		
		return file.mediaId;
		
	}
	
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

	public static boolean testSearchBan(Twitter api,UserArchive archive) throws TwitterException {

		QueryResult result = api.search(new twitter4j.Query("from:" + archive.screenName));

		return result.getCount() == 0;

	}
	
	public static boolean testThreadBan(Twitter api,UserArchive archive) throws TwitterException {
		
		ResponseList<Status> tl = api.getUserTimeline(archive.id,new Paging().count(200));

		for (Status status : tl) {
			
			if (status.getQuotedStatus() != null) {
				
				QueryResult result = api.search(new twitter4j.Query("from:" + archive.screenName + " to:" +  status.getQuotedStatus().getUser().getScreenName()).sinceId(status.getId()).maxId(status.getId()));

				return result.getCount() == 0;
				
			}
			
		}
		
		return false;
		
	}
	
	public static boolean testSearchSuggestionBan(Twitter api,UserArchive archive) throws TwitterException {

		ResponseList<twitter4j.User> result = api.getUserSuggestions(archive.screenName);

		for (twitter4j.User user : result) {
			
			if (archive.id.equals(user.getId())) {
				
				return false;
				
			}
			
		}
		
		return true;

	}

	public static TAuth loopFindAccessable(Object idOrScreenName) {

		long targetL = NumberUtil.isNumber(idOrScreenName.toString()) ? NumberUtil.parseLong(idOrScreenName.toString()) : -1;
		String targetS = idOrScreenName.toString();

		for (TAuth auth : TAuth.data.collection.find()) {

			Twitter api = auth.createApi();

			try {

				UserArchive user = UserArchive.save(targetL == -1 ? api.showUser(targetS) : api.showUser(targetL));

				if (user.isProtected) {

					FindIterable<TrackTask.IdsList> accs = TrackTask.friends.findByField("ids",user.id);

					for (TrackTask.IdsList acc : accs) {

						TAuth newAuth = TAuth.getById(acc.id);

						if (newAuth != null) return newAuth;

					}

					return null;

				} else {

					api.getUserTimeline(user.id,new Paging().count(1));

				}

				return auth;

			} catch (TwitterException e) {

				if (ArrayUtil.contains(new int[] { 17,34,50 },e.getErrorCode())) {

					return null;

				}

			}

		}

		return null;

	}

	public static String parseTwitterException(TwitterException exc) {

		switch (exc.getStatusCode()) {

			case 410 : return "这个操作已经不存在了";

			case TwitterException.TOO_MANY_REQUESTS : return "请求过多被限制 : 请稍后操作";

			case TwitterException.ENHANCE_YOUR_CLAIM : return "NTT被限制 : 请联系开发者";

		}

		switch (exc.getErrorCode()) {

			case 17 : case 50 : return "找不到用户";

			case 34 : return "请求的内容找不到";

			case 63 : return "用户被冻结";

			case 64 : return "你的账号被限制 : 无法进行此操作";

			case 87 : return "NTT无权限进行此操作 : 请联系开发者";

			case 88 : return "超过接口调用上限 : 通常是十五分钟内的限制，请稍后再试";

			case 89 : case 99 : case 215: return "NTT被取消了授权 或 你的账号被停用 / 冻结";

			case 93 : return "NTT无权操作私信 如果重新认证账号仍无法操作，请联系开发者";

			case 130 : return "Twitter服务器超载 请稍后再试";

			case 131 : return "Twitter服务器内部问题 请稍后再试";

			case 135 : return "服务器时间戳错误，请联系开发者";

			case 136 : return "操作失败 : 你被对方屏蔽";

			case 139 : return "你已经喜欢过了这条推文";

			case 144 : return "推文找不到 / 被删除";

			case 150 : return "你没有关注对方，无法发送私信";

			case 151 : return "发送私信错误 : " + exc.getMessage();

			case 160 : return "已经发送过关注请求了";

			case 161 : return "超过用户单日关注上限 : 这通常是400人";

			case 179 : return "推文无法取得 : 对方锁推且未被关注";

			case 185 : return "无法发送推文 : 发推数量超过上限";

			case 186 : return "无法发送推文 : 文本太长 限制为 180 字";

			case 187 : return "无法发送推文 : 与上一条重复 你是复读机吗？";

			case 205 : return "操作失败，请稍后再试 : 你被jvbao了";

			case 226 : return "操作失败 : Twitter认为这是程序自动进行的恶意操作";

			case 261 : return "NTT无权进行写操作 : 请联系开发者";

			case 271 : return "你不能对你自己静音";

			case 272 : return "你没有对这个用户静音";

			case 323 : return "同时发送多张图片时 不允许其他媒体 (指视频或Gif)";

			case 326 : return "账号被Twitter限制 : 你必须登录Twitter网站/客户端来解除这个限制 : 这通常需要验证手机";

			case 327 : return "你已经转推过了这条推文";

			case 349 : return "你不被允许发送消息给对方";

			case 354 : return "发送失败 : 私聊消息字数超过限制";

			case 385 : return "你不能回复一条你不可查看或已被删除的推文";

			case 416 : return "NTT接口无效/被停用 : 通常是因为开发者账号被停用/冻结";

			default : return "其他错误 请联系开发者 : " + exc.getErrorCode() + " " + exc.getMessage();

		}

	}

    public static boolean isUserContactable(long id) {

        SendResponse resp = new Send(id,"test_user_ontactable").disableNotification().exec();

        if (!resp.isOk()) return false;

        new Msg(resp.message()).delete();

        return true;

    }

    public static boolean checkNonContactable(UserData user,Msg msg) {

        String notContactableMsg = "咱无法给乃发送信息呢，请私聊点击 'start' 启用咱 ~";

        if (!msg.isPrivate() && !isUserContactable(user.id)) {

            if (msg instanceof Callback) {

                ((Callback)msg).alert(notContactableMsg);

            } else {

                msg.send(user.userName(),notContactableMsg).publicFailed();

            }

            return true;

        }

        return false;

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

    public static String parseScreenName(String input) {

        if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input,"twitter.com/",true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input,"?",false);

            }

			if (input.contains("/")) {

				input = StrUtil.subBefore(input,"/",false);

			}

        }

		if (input.contains("@")) {

			input = StrUtil.subAfter(input,"@",false);

		}

        return input;

    }

    public static long parseStatusId(String input) {

        Long statusId = -1L;

        if (NumberUtil.isLong(input)) {

            statusId = NumberUtil.parseLong(input);

        } else if (input.contains("twitter.com/")) {

            input = StrUtil.subAfter(input,"status/",true);

            if (input.contains("?")) {

                input = StrUtil.subBefore(input,"?",false);

            }

            if (NumberUtil.isNumber(input))  {

                statusId = NumberUtil.parseLong(input);

            }

        }

        return statusId;

    }

    static Timer deleteTimer = new Timer();

	public static void tryDelete(final long delay,final Msg... messages) {

        deleteTimer.schedule(new TimerTask() {

				@Override
				public void run() {

					for (Msg message : messages) {

						if (message == null) continue;

						if (!message.delete()) return;

					}

				}

			},delay);

	}

    public static boolean isGroupAdmin(Long chatId,Long userId) {

        GetChatMemberResponse resp = Launcher.INSTANCE.bot().execute(new GetChatMember(chatId,userId.intValue()));

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

		if (msg.from().developer()) return false;

        if (!isGroupAdmin(msg.chatId(),msg.from().id)) {

            if (msg instanceof Callback) {

                ((Callback)msg).alert("你不是绒布球 Σ( ﾟωﾟ");

            } else {

                msg.send("你不是绒布球 Σ( ﾟω。").publicFailed();

            }

            return true;

        }

        return false;

    }

    public static String checkCommand(Msg msg) {

        if (msg.isCommand()) {

            return msg.command();

        }

        return "-1";

    }

}
