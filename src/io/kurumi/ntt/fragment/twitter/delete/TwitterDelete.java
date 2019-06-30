package io.kurumi.ntt.fragment.twitter.delete;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.BotLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.fragment.BotFragment;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import io.kurumi.ntt.utils.NTT;

public class TwitterDelete extends Fragment {

    public static TwitterDelete INSTANCE = new TwitterDelete();
    final String POINT_DELETE = "td";
    HashMap<Long, DeleteThread> threads = new HashMap<>();

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("delete","delete_cancel");

		registerPoint(POINT_DELETE);
		
	}

	@Override
	public int checkFunction() {

		return FUNCTION_PRIVATE;

	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {
		
		return PROCESS_SYNC;
		
	}

	@Override
	public int checkPoint(UserData user,Msg msg,String point,Object data) {
		
		return PROCESS_SYNC;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg);

	}

    @Override
    public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        if (function.endsWith("cancel")) {

            if (!threads.containsKey(account.id)) {

                msg.send("你没有正在处理中的删除...").exec();

                return;

            }

            threads.remove(account.id).stoped.set(true);

            msg.send("已取消...").exec();


        } else if (threads.containsKey(account.id)) {

            msg.send("你有正在处理中的删除... 这需要大量时间处理... 取消使用 /delete_cancel .").exec();

            return;

        } else {

            setPrivatePoint(user,POINT_DELETE,account);

            msg.send("这个功能需要从 Twitter应用/网页 - 设置 - 账号 - 你的Twitter数据 输入密码下载数据zip，并找到tweet.js/like.js的说").exec();

            msg.send("现在发送 tweet.js / like.js 来删除所有推文/打心... (如果文件超过20m 需要打zip包发送哦 (tweet.js打tweet.zip / like.js打like.zip (❁´▽`❁)","使用 /cancel 取消 ~").exec();

			msg.send("也可以发送 FETCH 执行拉取推文并删除 (无法读取久远的推文、通常是几个月之前)").exec();

        }

    }

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {

		if ("FETCH".equals(msg.text())) {

			DeleteThread thread = new DeleteThread();

			thread.userId = user.id;

			thread.account = (TAuth) data;

			thread.api = thread.account.createApi();

			thread.tweet = true;

			thread.fetch = true;

			threads.put(thread.account.id,thread);

			thread.start();

			clearPrivatePoint(user);

			
			return;
			
		} else if (msg.doc() == null || !msg.doc().fileName().matches("(tweet|like)\\.(js|zip)")) {

            msg.send("你正在删除twetter数据... 发送 tweet.js 删除推文 like.js 删除打心...").withCancel().exec();

			return;

        }

		boolean like = msg.doc().fileName().startsWith("like");

		File file = msg.file();

		if (file == null) {

			if (msg.doc().fileName().endsWith(".js")) {

				msg.send("文件太大... Telegram禁止Bot下载超过20mb的文件... 请打zip包并修改为 tweet.zip / like.zip （●＾o＾●）").exec();

			} else {

				msg.send("可能真的太大...？ zip都超过20m...").exec();

			}

			return;


		}

		File zipOut = new File(Env.CACHE_DIR,"unzip/" + msg.doc().fileId());

		if (!zipOut.isDirectory() || zipOut.list().length == 0) {

			if (msg.doc().fileName().endsWith(".zip")) {

				try {

					ZipUtil.unzip(file,zipOut);

					file = new File(zipOut,like ? "like.js" : "tweet.js");

					if (!file.isFile()) {

						msg.send("这个压缩包里面有文件吗？ 注意 : tweet.js 需要打成 tweet.zip / like.js 需要打成 like.zip 否则无法识别呢 (˚☐˚! )/").exec();

						return;

					}

				} catch (Exception ex) {

					msg.send("解压zip包失败... 你是打了rar包改后缀了吗？(˚☐˚! )/").exec();

					return;

				}

			}

		}


		msg.send("解析" + (like ? "打心" : "推文") + "ing... Σ( ﾟωﾟ").exec();

		clearPrivatePoint(user);

		BufferedReader reader = IoUtil.getReader(IoUtil.toStream(file),CharsetUtil.CHARSET_UTF_8);

		LinkedList<Long> ids = new LinkedList<>();

		try {

			String line = reader.readLine();

			boolean isRC = false;

			while (line != null) {

				if (like) {

					if (line.contains("tweetId")) {

						ids.add(Long.parseLong(StrUtil.subBetween(line,"\" : \"","\"")));

					}

				} else if (isRC) {

					ids.add(Long.parseLong(StrUtil.subBetween(line,"\" : \"","\"")));

					isRC = false;

				} else if (line.contains("retweet_count")) {

					isRC = true;

				}

				line = reader.readLine();

			}

			reader.close();

		} catch (IOException e) {

			msg.send("读取文件错误...",e.toString()).exec();

			return;

		}

		msg.send("已解析 " + ids.size() + " 条" + (like ? "打心" : "推文") + "... 正在启动删除线程...").exec();

		DeleteThread thread = new DeleteThread();

		thread.userId = user.id;

		thread.account = (TAuth) data;

		thread.api = thread.account.createApi();

		thread.ids = ids;

		thread.tweet = !like;

		threads.put(thread.account.id,thread);

		thread.start();



	}


	class DeleteThread extends Thread {

		long userId;
		TAuth account;
		Twitter api;
		LinkedList<Long> ids;
		boolean tweet;
		boolean fetch;

		AtomicBoolean stoped = new AtomicBoolean(false);

		@Override
		public void run() {

			Msg status = new Send(userId,"正在删除... ","取消删除使用 /delete_cancel").send();

			if (!fetch) {

				float count = ids.size();
				float current = 0;

				float progress = 0;

				for (Long id : ids) {

					float last = progress;

					try {

						if (tweet) {

							api.destroyStatus(id);

						} else {

							api.destroyFavorite(id);

						}


					} catch (TwitterException e) {

						BotLog.info("删除，错误",e);

					}

					if (stoped.get()) {

						status.edit("已手动取消...").exec();

						break;

					}

					current ++;

					progress = ((float)(Math.round(current / count)) * 1000) / 100;

					if (progress != last) {

						status.edit("删除中 : " + progress + "%","取消删除使用 /delete_cancel ...").exec();

					}

					if (progress == 100) {

						status.edit("删除完成...").exec();

					}

				}

			} else {

				int current = 0;

				try {

					ResponseList<Status> timeline = api.getUserTimeline(new Paging().count(200));

					while (timeline != null && !timeline.isEmpty() && !stoped.get()) {

						for (Status s : timeline) {

							if (stoped.get()) break;

							try {

								api.destroyStatus(s.getId());

								current ++;

								if (current % 20 == 0) {

									status.edit("拉取中 已删除 " + current + " 条","取消删除使用 /delete_cancel ...").exec();

								}

							} catch (TwitterException e) {

								if (e.getErrorCode() != 144) throw e;

							}

						}

						timeline = api.getUserTimeline(new Paging().count(200));

					}

					status.edit("删除完成 : 已拉取并删除 " + current + " 条","如果有剩余推文，请使用tweet.js以删除程序无法拉取的久远推文").exec();

				} catch (TwitterException e) {

					status.edit("删除出错 已停止 :",NTT.parseTwitterException(e)).exec();

				}

				if (stoped.get()) {

					status.edit("已手动取消...").exec();

				}


			}

			threads.remove(account.id);



		}

	}
}

