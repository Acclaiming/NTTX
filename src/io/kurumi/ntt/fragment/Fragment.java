package io.kurumi.ntt.fragment;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpUtil;
import com.mongodb.client.FindIterable;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Poll;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetFileResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.status.StatusAction;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import java.io.File;
import java.io.IOException;
import cn.hutool.core.thread.ThreadUtil;
import java.util.LinkedList;

public class Fragment {

    public String PAYLOAD_SPLIT = "_";

    public BotFragment origin;

    public TelegramBot bot() {

        return origin.bot();

    }

		public <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {

				return bot().execute(request);

    }


    public PointStore point() {

        return origin.point();

    }

		public static void execute(Runnable runnable) {

				BotFragment.asyncPool.execute(runnable);

		}

		public  void setPrivatePoint(UserData user,String pointTo,PointData content) {

        point().setPrivate(user,pointTo,content);

    }

    public PointData setPrivatePointData(UserData user,String pointTo,Object content) {

        return point().setPrivateData(user,pointTo,content);

    }

    public PointData setPrivatePoint(UserData user,String pointTo) {

        return point().setPrivateData(user,pointTo,null);

    }

		public  void setGroupPoint(UserData user,String pointTo,PointData content) {

        point().setGroup(user,pointTo,content);

    }


		public  PointData setGroupPointData(UserData user,String pointTo,Object content) {

        return point().setGroupData(user,pointTo,content);

    }

    public PointData setGroupPoint(UserData user,String pointTo) {

				return point().setGroupData(user,pointTo,null);

    }

    public PointData clearPrivatePoint(UserData user) {

        return point().clearPrivate(user);

    }

		public PointData clearGroupPoint(UserData user) {

        return point().clearGroup(user);

    }

    public  PointData getPrivatePoint(UserData user) {

        return point().getPrivate(user);

    }

		public PointData getGroupPoint(UserData user) {

        return point().getPrivate(user);

    }

    public boolean onUpdate(UserData user,Update update) {

				return false;

    }

		public static abstract class Processed implements Runnable {

				public int type;
				public UserData user;
				public  Update update;

				public Processed(UserData user,Update update,int type) {

						this.type = type;

						this.user = user;
						this.update = update;
				}

				public abstract void process();

				public void run() {

						try {

								process();

						} catch (Exception e) {

								new Send(Env.GROUP,"处理中出错 " + update.toString(),BotLog.parseError(e)).exec();

								if (user != null && !user.admin()) {

										new Send(user.id,"处理出错，已提交报告，可以到官方群组 @NTTDiscuss  继续了解").exec();

								}

						}

				}

		}

		public void init(BotFragment origin) {

				this.origin = origin;

		}

		static Processed EMPTY = new Processed(null,null,-1) {

				@Override
				public void process() {
				}

		};

		public Processed onAsyncUpdate(final UserData user,Update update) {

				if (onUpdate(user,update)) return null;

				if (update.message() != null) {

						final Msg msg = new Msg(this,update.message());

						msg.update = update;

						if (msg.replyTo() != null) msg.replyTo().update = update;

						int checked = checkMsg(user,msg); 

						if (checked == PROCESS_ASYNC) {

								BotFragment.asyncPool.execute(new Processed(user,update,PROCESS_ASYNC) {

												@Override
												public void process() {

														onMsg(user,msg);

												}

										});

						} else if (checked == PROCESS_REJECT) {

								return EMPTY;

						} else {

								onMsg(user,msg);

						}

				} else if (update.channelPost() != null) {

						final Msg msg = new Msg(this,update.channelPost());

						msg.update = update;

						if (msg.replyTo() != null) msg.replyTo().update = update;

						int checked = checkChanPost(user,msg); 

						if (checked == PROCESS_ASYNC) {

								BotFragment.asyncPool.execute(new Processed(user,update,PROCESS_ASYNC) {

												@Override
												public void process() {

														onChanPost(user,msg);

												}

										});

						} else if (checked == PROCESS_REJECT) {

								return EMPTY;

						} else {

								onChanPost(user,msg);

						}

				} else if (update.inlineQuery() != null) {

						onQuery(user,new Query(this,update.inlineQuery()));

				} else if (update.poll() != null) {

						onPollUpdate(update.poll());

				}

				return null;

    }

		// 注册函数

		public void registerFunction(String... functions) {

				for (String function : functions) {

						origin.functions.put(function,this);

				}

		}

		public void registerAdminFunction(String... functions) {

				for (String function : functions) {

						origin.adminFunctions.put(function,this);

				}

		}

		public void registerPayload(String... payloads) {

				for (String payload : payloads) {

						origin.payloads.put(payload,this);

				}

		}

		public void registerAdminPayload(String... payloads) {

				for (String payload : payloads) {

						origin.adminPayloads.put(payload,this);

				}

		}

		public void registerPoint(String... points) {

				for (String point : points) {

						origin.points.put(point,this);

				}

		}

		public void registerPoints(String... points) {

				for (String point : points) {

						origin.callbacks.put(point,this);
						origin.points.put(point,this);

				}

		}


		public void registerCallback(String... points) {

				for (String point : points) {

						origin.callbacks.put(point,this);

				}


		}

		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_PRIVATE;

		}

		public int checkFunction(UserData user,Msg msg,String function,String[] params) {

				return PROCESS_SYNC;

		}

		public void onFunction(UserData user,Msg msg,String function,String[] params) {
		}

		protected final String POINT_REQUEST_TWITTER = "request_twitter";

		static class TwitterRequest extends PointData {

				UserData fromUser;
				Msg originMsg;
				Fragment fragment;

				boolean payload;

		}

		public void requestTwitter(UserData user,Msg msg) {

				requestTwitter(user,msg,false,false);

		}

		public void requestTwitter(UserData user,Msg msg,boolean noCurrent) {

				requestTwitter(user,msg,noCurrent,false);

		}

		public void requestTwitterPayload(UserData user,Msg msg) {

				requestTwitter(user,msg,false,true);

		}


		public void requestTwitterPayload(UserData user,Msg msg,boolean noCurrent) {

				requestTwitter(user,msg,noCurrent,true);

		}

		public void requestTwitter(final UserData user,final Msg msg,boolean noCurrent,final boolean isPayload) {

				if (!TAuth.contains(user.id)) {

            msg.send("这个功能需要授权 Twitter账号 才能使用 (❁´▽`❁)","使用 /login 认证账号 ~").exec();

            return;

        }

				if (TAuth.data.countByField("user",user.id) == 1) {

						TAuth auth = TAuth.getByUser(user.id).first();

						if (isPayload) {

								onTwitterPayload(user,msg,msg.payload()[0],msg.payload().length > 1 ? ArrayUtil.sub(msg.payload(),1,msg.payload().length) : new String[0],auth);

						} else {

								onTwitterFunction(user,msg,msg.command(),msg.params(),auth);

						}

						return;

				}

				if (!noCurrent && StatusAction.current.containsId(user.id)) {

						TAuth current = TAuth.getById(StatusAction.current.getById(user.id).accountId);

						if (current != null && current.user.equals(user.id)) {

								if (isPayload) {

										onTwitterPayload(user,msg,msg.payload()[0],msg.payload().length > 1 ? ArrayUtil.sub(msg.payload(),1,msg.payload().length) : new String[0],current);

								} else {

										onTwitterFunction(user,msg,msg.command(),msg.params(),current);

								}

								return;

						}

				}

				if (msg.isGroup()) {

						msg.send("咱已经在私聊回复了你。","如果BOT有删除信息权限,命令和此回复将被自动删除。:)").failedWith();

						msg.targetChatId = user.id;

						msg.sendTyping();

				}

				final FindIterable<TAuth> accounts = TAuth.getByUser(user.id);

				TwitterRequest request = new TwitterRequest() {{

								this.fromUser = user;
								this.originMsg = msg;
								this.fragment = Fragment.this;
								this.payload = isPayload;

						}};

				msg.send("请选择目标账号 Σ( ﾟωﾟ ").keyboard(new Keyboard() {{

										for (TAuth account : accounts) {

												newButtonLine("@" + account.archive().screenName);

										}

								}}).withCancel().exec(request);


				setPrivatePoint(msg.from(),POINT_REQUEST_TWITTER,request);



		}

		public int onBlockedMsg(UserData user,Msg msg) { return 0; }

		public int checkTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

				return PROCESS_ASYNC;

		}

		public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		}

		public int checkPayload(UserData user,Msg msg,String payload,String[] params) {

				return PROCESS_ASYNC;

		}

		public void onPayload(UserData user,Msg msg,String payload,String[] params) {
		}

		public int checkTwitterPayload(UserData user,Msg msg,String payload,String[] params,TAuth account) {

				return PROCESS_ASYNC;

		}

		public void onTwitterPayload(UserData user,Msg msg,String payload,String[] params,TAuth account) {
		}

		public int checkPoint(UserData user,Msg msg,String point,PointData data) {

				return PROCESS_SYNC;

		}

		public void onPoint(UserData user,Msg msg,String point,PointData data) {
		}

		public int checkPointedFunction(UserData user,Msg msg,String function,String[] params,String point,PointData data) {

				return PROCESS_SYNC;

		}

		public void onPointedFunction(UserData user,Msg msg,String function,String[] params,String point,PointData data) {

				onPoint(user,msg,point,data);

		}

		public int checkCallback(UserData user,Callback callback,String point,String[] params) {

				return PROCESS_ASYNC;

    }

		public void onCallback(UserData user,Callback callback,String point,String[] params) {
    }

		// 基本函数

		public static final int PROCESS_REJECT = 0;
		public static final int PROCESS_SYNC = 1;
		public static final int PROCESS_ASYNC = 2;

		public static final int FUNCTION_PRIVATE = 1;
		public static final int FUNCTION_GROUP = 2;
		public static final int FUNCTION_PUBLIC = 3;

		public int checkMsg(UserData user,Msg msg) {

				return PROCESS_ASYNC;

		}

		public void onMsg(UserData user,Msg msg) {

				if (msg.isGroup()) onGroup(user,msg);
				if (msg.isPrivate()) onPrivate(user,msg);

		}

		public void onGroup(UserData user,Msg msg) {}
		public void onPrivate(UserData user,Msg msg) {}

		public int checkChanPost(UserData user,Msg msg) {

				return PROCESS_ASYNC;

		}

    public void onChanPost(UserData user,Msg msg) {
		}

		public void onPollUpdate(Poll poll) {
		}

    public void onQuery(UserData user,Query inlineQuery) {
    }

		public byte[] readStiker(Long userId,Sticker sticker) {

				File file = getFile(sticker.fileId());

				return FileUtil.readBytes(file);
		}

    public File getFile(String fileId) {

        File local = new File(Env.CACHE_DIR,"files/" + fileId);

        if (local.isFile()) return local;

        GetFileResponse file = execute(new GetFile(fileId));

        if (!file.isOk()) {

            BotLog.debug("取文件失败 : " + file.errorCode() + " " + file.description());

            return null;

        }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path,local);

        return local;

    }

		public File getFile(GetFileResponse file) {

        File local = new File(Env.CACHE_DIR,"files/" + file.file().fileId());

        if (local.isFile()) return local;

				if (!file.isOk()) {

            BotLog.debug("取文件失败 : " + file.errorCode() + " " + file.description());

            return null;

        }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path,local);

        return local;

    }

    public Msg sendSticker(long chatId,String sticker) {

        return Msg.from(this,execute(new SendSticker(chatId,sticker)));

    }

    public Msg sendFile(long chatId,String file) {

        return Msg.from(this,this.execute(new SendDocument(chatId,file)));

    }

    public Msg sendFile(long chatId,File file) {

        return Msg.from(this,execute(new SendDocument(chatId,file)));

    }

    public Msg sendFile(long chatId,byte[] file) {

        return Msg.from(this,execute(new SendDocument(chatId,file)));

    }

    public void sendTyping(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.typing));

    }

    public void sendUpdatingFile(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.upload_document));

    }

    public void sendUpdatingPhoto(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.upload_photo));

    }

    public void sendUpdatingAudio(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.upload_audio));

    }

    public void sendUpdatingVideo(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.upload_video));

    }

    public void sendUpdatingVideoNote(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.upload_video_note));

    }

    public void sendFindingLocation(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.find_location));

    }

    public void sendRecordingAudio(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.record_audio));

    }


    public void sendRecordingViedo(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.record_video));

    }

    public void sendRecordingVideoNote(long chatId) {

        execute(new SendChatAction(chatId,ChatAction.record_video_note));

    }


}
