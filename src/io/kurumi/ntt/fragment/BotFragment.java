package io.kurumi.ntt.fragment;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetMeResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Callback;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.Query;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.admin.DelMsg;
import io.kurumi.ntt.fragment.admin.Firewall;
import io.kurumi.ntt.fragment.base.Final;
import io.kurumi.ntt.fragment.base.GetID;
import io.kurumi.ntt.fragment.base.PingFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;
import java.util.Timer;

public abstract class BotFragment extends Fragment implements UpdatesListener {

    Final finalFragment = new Final() {{ init(BotFragment.this); }};;

	public static Timer mainTimer = new Timer();
	public static ExecutorService asyncPool = Executors.newCachedThreadPool();
	public static LinkedBlockingQueue<UserAndUpdate> queue = new LinkedBlockingQueue<>();
	public static LinkedList<ProcessThread> threads = new LinkedList<>();

    public User me;
    private TelegramBot bot;
    private LinkedList<Fragment> fragments = new LinkedList<>();
    private String token;
    private PointStore point;

	public static void startThreads(int count) {

		for (int index = 0;index < count;index ++) {

			threads.add(new ProcessThread() {{ start(); }});

		}

	}

	public static void stopThreads() {

		for (ProcessThread thread : threads) {

			thread.stopped.set(true);

			thread.interrupt();

		}

	}

	class UserAndUpdate {

		long chatId;

		UserData user;

		Update update;

		BotFragment.Processed process() {

			for (final Fragment fragmnet : fragments) {

				Fragment.Processed processed =  fragmnet.onAsyncUpdate(user,update);

				if (processed != null) return processed;

			}

			finalFragment.onAsyncUpdate(user,update);

			return null;

		}

	}

    @Override
    public TelegramBot bot() {

        return bot;
    }

    public void reload() {

		fragments.clear();

		addFragment(this);

		addFragment(new Firewall());
		addFragment(new PingFunction());
		addFragment(new GetID());

		addFragment(new DelMsg());


    }

	public HashMap<String,Fragment> functions = new HashMap<>();

	public HashMap<String,Fragment> adminFunctions = new HashMap<>();

	public HashMap<String,Fragment> payloads = new HashMap<>();

	public HashMap<String,Fragment> adminPayloads = new HashMap<>();

	public HashMap<String,Fragment> points = new HashMap<>();
	public HashMap<String,Fragment> callbacks = new HashMap<>();


	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("cancel");

		registerPoint(POINT_REQUEST_TWITTER);

	}

	@Override
	public int checkPoint(UserData user,Msg msg,String point,Object data) {
		
		return PROCESS_SYNC;
		
	}

	@Override
	public void onPoint(final UserData user,Msg msg,String point,Object data) {

		if (POINT_REQUEST_TWITTER.equals(point)) {

            final TwitterRequest request = (TwitterRequest) data;

			request.msgs.add(msg);

            if (!msg.hasText() || !msg.text().startsWith("@")) {

                request.msgs.add(msg.send("请选择 Twitter 账号 (˚☐˚! )/").send());

                return;

            }

            String screenName = msg.text().substring(1);

            final TAuth account = TAuth.getById(UserArchive.get(screenName).id);

            if (account == null) {

                request.msgs.add(msg.send("找不到这个账号 (？) 请重新选择 ((*゜Д゜)ゞ").send());

                return;

            }

            for (Msg toDelete : request.msgs) toDelete.delete();

            msg.send("选择了 : " + account.archive().urlHtml() + " (❁´▽`❁)").removeKeyboard().html().failed();

            clearPrivatePoint(user);

			if (request.payload) {

				String payload = request.originMsg.payload()[0];

				String[] params = request.originMsg.payload().length > 1 ? ArrayUtil.sub(request.originMsg.payload(),1,request.originMsg.payload().length) : new String[0];

				int checked = request.fragment.checkTwitterPayload(user,request.originMsg,payload,params,account);

				request.fragment.onTwitterPayload(user,request.originMsg,payload,params,account);

				if (checked == PROCESS_THREAD) {

					asyncPool.execute(new Runnable() {

							@Override
							public void run() {

								request.fragment.onTwitterFunction(user,request.originMsg,request.originMsg.command(),request.originMsg.params(),account);

							}

						});

				} else {

					request.fragment.onTwitterFunction(user,request.originMsg,request.originMsg.command(),request.originMsg.params(),account);

				}


			} else {

				int checked = request.fragment.checkTwitterFunction(user,request.originMsg,request.originMsg.command(),request.originMsg.params(),account);

				if (checked == PROCESS_THREAD) {

					asyncPool.execute(new Runnable() {

							@Override
							public void run() {

								request.fragment.onTwitterFunction(user,request.originMsg,request.originMsg.command(),request.originMsg.params(),account);

							}

						});

				} else {

					request.fragment.onTwitterFunction(user,request.originMsg,request.originMsg.command(),request.originMsg.params(),account);

				}

			}

        }

	}

    public void addFragment(Fragment fragment) {

		fragment.init(this);

		fragments.add(fragment);

    }

    public abstract String botName();

    @Override
    public int process(List<Update> updates) {

        for (final Update update : updates) {

            try {

                processAsync(update);

            } catch (Exception e) {

                BotLog.error("更新出错",e);

                Launcher.INSTANCE.uncaughtException(Thread.currentThread(),e);

            }


        }

        return CONFIRMED_UPDATES_ALL;

    }

    @Override
    public PointStore point() {

        if (point != null) return point;

        synchronized (this) {

            if (point != null) return point;

            point = PointStore.getInstance(this);

            return point;

        }

    }

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if ("cancel".equals(function)) {

			msg.send("没有什么需要取消的 :)").publicFailed();

			return;

		}

	}

	@Override
	public void onPointedFunction(UserData user,Msg msg,String function,String[] params,String point,Object data) {

		if ("cancel".equals(function)) {

			clearPrivatePoint(user);

			msg.send("已经取消当前操作 :) ","帮助文档 : @NTT_X").publicFailed();

			return;

		}

	}

    public void processAsync(final Update update) {

        final UserData user;

		long targetId = -1;

        if (update.message() != null) {

            user = UserData.get(update.message().from());

			if (update.message().chat().type() != Chat.Type.Private) {

				targetId = update.message().chat().id();

			}

        } else if (update.editedMessage() != null) {

			user = UserData.get(update.editedMessage().from());

			if (update.editedMessage().chat().type() != Chat.Type.Private) {

				targetId = update.editedMessage().chat().id();

			}

		} else if (update.channelPost() != null) {

            user = update.channelPost().from() != null ? UserData.get(update.channelPost().from()) : null;

			targetId = update.channelPost().chat().id();

		} else if (update.editedChannelPost() != null) {

			user = update.editedChannelPost().from() != null ? UserData.get(update.editedChannelPost().from()) : null;

			targetId = update.editedChannelPost().chat().id();

        } else if (update.callbackQuery() != null) {

            user = UserData.get(update.callbackQuery().from());

        } else if (update.inlineQuery() != null) {

            user = UserData.get(update.inlineQuery().from());

        } else user = null;

		UserAndUpdate uau = new UserAndUpdate();

		uau.chatId = targetId;
		uau.user = user;
		uau.update = update;

		queue.add(uau);


    }

	@Override
	public Processed onAsyncUpdate(UserData user,Update update) {

		if (onUpdate(user,update)) return EMPTY;

		if (update.message() != null) {

			final Msg msg = new Msg(this,update.message());

			final PointStore.Point privatePoint = point().getPrivate(user);
			final PointStore.Point groupPoint = point().getGroup(user);

			if (msg.isGroup() && groupPoint != null) {

				final Fragment function = points.containsKey(groupPoint.point) ? points.get(groupPoint.point) : this;

				if (msg.isCommand()) {

					int checked = function.checkPointedFunction(user,msg,msg.command(),msg.params(),groupPoint.point,groupPoint.data);

					if (checked == PROCESS_REJECT) return EMPTY;

					return new Processed(user,update,checked) {

						@Override
						public void process() {

							msg.sendTyping();

							function.onPointedFunction(user,msg,msg.command(),msg.params(),groupPoint.point,groupPoint.data);

						}

					};

				} else {

					int checked = function.checkPoint(user,msg,groupPoint.point,groupPoint.data);

					if (checked == PROCESS_REJECT) return EMPTY;

					return new Processed(user,update,checked) {

						@Override
						public void process() {

							msg.sendTyping();

							function.onPoint(user,msg,groupPoint.point,groupPoint.data);

						}

					};

				}


			} else if (msg.isPrivate() && privatePoint != null) {

				final Fragment function = !points.containsKey(privatePoint.point) || "cancel".equals(msg.command()) ? this : points.get(privatePoint.point);

				if (msg.isCommand()) {

					int checked = function.checkPointedFunction(user,msg,msg.command(),msg.params(),privatePoint.point,privatePoint.data);

					if (checked == PROCESS_REJECT) return EMPTY;

					return new Processed(user,update,checked) {

						@Override
						public void process() {

							msg.sendTyping();

							function.onPointedFunction(user,msg,msg.command(),msg.params(),privatePoint.point,privatePoint.data);

						}

					};

				} else {

					int checked = function.checkPoint(user,msg,privatePoint.point,privatePoint.data);

					if (checked == PROCESS_REJECT) return EMPTY;

					return new Processed(user,update,checked) {

						@Override
						public void process() {

							msg.sendTyping();

							function.onPoint(user,msg,privatePoint.point,privatePoint.data);

						}

					};

				}

			} else {

				if (msg.isCommand()) {

					if (msg.isStartPayload()) {

						final String payload = msg.payload()[0];
						final String[] params = msg.payload().length > 1 ? ArrayUtil.sub(msg.payload(),1,msg.payload().length) : new String[0];

						if (payloads.containsKey(payload)) {

							final Fragment function = payloads.get(payload);

							int checked = function.checkPayload(user,msg,payload,params);

							if (checked == PROCESS_REJECT) return EMPTY;

							return new Processed(user,update,checked) {

								@Override
								public void process() {

									msg.sendTyping();

									function.onPayload(user,msg,payload,params);

								}

							};

						} else if (adminPayloads.containsKey(payload)) {

							final Fragment function = adminPayloads.get(payload);

							int checked = function.checkPayload(user,msg,payload,params);

							if (checked == PROCESS_REJECT) return EMPTY;

							return new Processed(user,update,checked) {

								@Override
								public void process() {

									msg.sendTyping();

									function.onPayload(user,msg,payload,params);

								}

							};

						} else {

							int checked = checkPayload(user,msg,payload,params);

							if (checked == PROCESS_REJECT) return EMPTY;

							return new Processed(user,update,checked) {

								@Override
								public void process() {

									msg.sendTyping();

									onPayload(user,msg,payload,params);

								}

							};

						}

					} else if (functions.containsKey(msg.command())) {

						final Fragment function = functions.get(msg.command());

						int checked = function.checkFunction(user,msg,msg.command(),msg.params());

						if (checked == PROCESS_REJECT) return EMPTY;

						if (function.checkFunction() == FUNCTION_GROUP && !msg.isGroup()) {

							return new Processed(user,update,checked) {

								@Override
								public void process() {

									msg.send("请在群组使用 :)").exec();

								}

							};


						} else if (function.checkFunction() == FUNCTION_PRIVATE && !msg.isPrivate()) {

							return new Processed(user,update,checked) {

								@Override
								public void process() {

									msg.send("请在私聊使用 :)").exec();

								}

							};

						}

						return new Processed(user,update,checked) {

							@Override
							public void process() {

								msg.sendTyping();

								function.onFunction(user,msg,msg.command(),msg.params());

							}

						};

					} else if (adminFunctions.containsKey(msg.command())) {

						final Fragment function = adminFunctions.get(msg.command());

						int checked = function.checkFunction(user,msg,msg.command(),msg.params());

						if (checked == PROCESS_REJECT) return EMPTY;

						return new Processed(user,update,checked) {

							@Override
							public void process() {

								msg.sendTyping();

								function.onFunction(user,msg,msg.command(),msg.params());

							}

						};

					} else {

						return new Processed(user,update,PROCESS_ASYNC) {

							@Override
							public void process() {

								if (msg.isPrivate()) {

									msg.sendTyping();

									onFinalMsg(user,msg);

								}

							}

						};

					}

				} else {

					int checked = checkMsg(user,msg); 

					if (checked == PROCESS_THREAD) {

						asyncPool.execute(new Processed(user,update,PROCESS_ASYNC) {

								@Override
								public void process() {

									onMsg(user,msg);

								}

							});

					}

					if (checked == PROCESS_REJECT) return EMPTY;

					onMsg(user,msg);

				}

			}

		} else if (update.channelPost() != null) {

			final Msg msg = new Msg(this,update.channelPost());

			int checked = checkChanPost(user,msg); 

			if (checked == PROCESS_THREAD) {

				asyncPool.execute(new Processed(user,update,PROCESS_ASYNC) {

						@Override
						public void process() {

							onChanPost(user,msg);

						}

					});

			}

			if (checked == PROCESS_REJECT) return EMPTY;

			onChanPost(user,msg);

		} else if (update.callbackQuery() != null) {

			final Callback callback = new Callback(this,update.callbackQuery());

			final String point = callback.params.length == 0 ? "" : callback.params[0];
			final String[] params = callback.params.length > 1 ? ArrayUtil.sub(callback.params,1,callback.params.length) : new String[0];

			final Fragment function = callbacks.containsKey(point) ?  callbacks.get(point): this;

			int checked = function.checkCallback(user,callback,point,params);

			if (checked == PROCESS_REJECT) return EMPTY;

			return new Processed(user,update,checked) {

				@Override
				public void process() {

					function.onCallback(user,callback,point,params);

				}

			};

		} else if (update.inlineQuery() != null) {

			onQuery(user,new Query(this,update.inlineQuery()));

		} else if (update.poll() != null) {

			onPollUpdate(update.poll());

		}

		return null;
	}

	static class ProcessThread extends Thread {

		static LinkedList<Long> processing = new LinkedList<>();

		AtomicBoolean stopped = new AtomicBoolean(false);

		@Override
		public void run() {

			while (!stopped.get()) {

				UserAndUpdate uau;

				try {

					uau = queue.take();

				} catch (InterruptedException e) {

					continue;

				}

				synchronized (processing) {

					if ((uau.chatId != -1 && processing.contains(uau.chatId)) || (uau.user != null && processing.contains(uau.user.id))) {

						queue.add(uau);

						continue;

					} else {

						if (uau.chatId != -1) {

							processing.add(uau.chatId);

						}

						if (uau.user != null) {

							processing.add(uau.user.id);

						}

					}

				}

				Processed processed;

				try {

					processed = uau.process();

				} catch (Exception e) {

					new Send(Env.GROUP,"处理中出错 " + uau.update.toString(),BotLog.parseError(e)).exec();

					if (uau.user != null && !uau.user.admin()) {

						new Send(uau.user.id,"处理出错，已提交报告，可以到官方群组 @NTTDiscuss  继续了解").exec();

					}

					continue;

				}


				if (processed == null) {

					synchronized (processing) {

						if (uau.chatId != -1) {

							processing.remove(uau.chatId);

						}

						if (uau.user != null) {

							processing.remove(uau.user.id);

						}

					}

					continue;

				}

				if (processed.type == 1 && !(uau.user == null && uau.chatId == -1)) {

					processed.run();

				}

				synchronized (processing) {

					if (uau.chatId != -1) {

						processing.remove(uau.chatId);

					}

					if (uau.user != null) {

						processing.remove(uau.user.id);

					}

				}

				if (processed.type == 2) {

					processed.run();

				} else if (processed.type == 3) {

					asyncPool.execute(processed);

				}

			}

		}

	}

	@Override
	public boolean onUpdate(UserData user,Update update) {

		BotLog.process(origin,user,update);
		
		return super.onUpdate(user,update);
	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if ("null".equals(point)) callback.confirm();
		else callback.alert("无效的回调指针 : " + point + "\n请联系开发者");

	}

	final String split = "------------------------\n";

    public void onFinalMsg(UserData user,Msg msg) {

        StringBuilder str = new StringBuilder();

        Message message = msg.message();

        str.append("消息ID : " + message.messageId()).append("\n");

        if (message.forwardFrom() != null) {

            str.append("来自用户 : ").append(UserData.get(message.forwardFrom()).userName()).append("\n");
            str.append("用户ID : ").append(message.forwardFrom().id()).append("\n");

        }

        if (message.forwardFromChat() != null) {

            if (message.forwardFromChat().type() == Chat.Type.channel) {

                str.append("来自频道 : ").append(message.forwardFromChat().username() == null ? message.forwardFromChat().title() : Html.a(message.forwardFromChat().username(),"https://t.me/" + message.forwardFromChat().username())).append("\n");

                str.append("频道ID : ").append(message.forwardFromChat().id());

                if (message.forwardSenderName() != null) {

                    str.append("签名用户 : ").append(message.forwardSenderName());

                }

            } else if (message.forwardFromChat().type() == Chat.Type.group || message.forwardFromChat().type() == Chat.Type.supergroup) {

                str.append("来自群组 : ").append(message.forwardFromChat().username() == null ? message.forwardFromChat().title() : Html.a(message.forwardFromChat().username(),"https://t.me/" + message.forwardFromChat().username())).append("\n");

            } else {

                if (message.forwardFrom() == null) {

                    str.append("来自 : ").append(message.forwardSenderName()).append(" (隐藏来源)\n");

                }

            }

            str.append("消息链接 : https://t.me/c/").append(message.forwardFromChat().id()).append("/").append(message.forwardFromMessageId()).append("\n");

        }

        if (message.sticker() != null) {

            str.append(split);

            str.append("贴纸ID : ").append(message.sticker().fileId()).append("\n");

            str.append("贴纸表情 : ").append(message.sticker().emoji()).append("\n");

            if (message.sticker().setName() != null) {

                str.append("贴纸包 : ").append("https://t.me/addstickers/" + message.sticker().setName()).append("\n");

            }

            msg.sendUpdatingPhoto();

            bot().execute(new SendPhoto(msg.chatId(),getFile(msg.message().sticker().fileId())).caption(str.toString()).parseMode(ParseMode.HTML).replyToMessageId(msg.messageId()));

        } else {

            msg.send("这一条消息未被处理 将忽略","帮助文档 / 公告频道 : @NTT_X","交流建议群组 : @NTTDiscuss :)",str.toString()).replyTo(msg).html().exec();

		}

    }

	public boolean isLongPulling() {

		return false;

	}

	public String getToken() {

		return Env.get("token." + botName());

	}

	public void setToken(String botToken) {

		Env.set("token." + botName(),token);

	}

	public boolean silentStart() {

		reload();

		token = getToken();

		bot = new TelegramBot.Builder(token).build();

		GetMeResponse resp = bot.execute(new GetMe());

		if (resp == null || !resp.isOk()) return false;

		me = resp.user();

		realStart();

		return true;

	}

	public void start() {

		reload();

		token = getToken();

		if (token == null || !Env.verifyToken(token)) {

			token = Env.inputToken(botName());

		}

		setToken(token);

		OkHttpClient.Builder okhttpClient = new OkHttpClient.Builder();

		okhttpClient.networkInterceptors().clear();

		bot = new TelegramBot.Builder(token)
			.okHttpClient(okhttpClient.build()).build();

		me = bot.execute(new GetMe()).user();

		realStart();

	}

	public void realStart() {

		bot.execute(new DeleteWebhook());

		if (isLongPulling()) {

			bot.setUpdatesListener(this,new GetUpdates());

		} else {

			/*

			 GetUpdatesResponse update = bot.execute(new GetUpdates());

			 if (update.isOk()) {

			 process(update.updates());

			 }

			 */

			String url = "https://" + BotServer.INSTANCE.domain + "/" + token;

			BotServer.fragments.put(token,this);

			BaseResponse resp = bot.execute(new SetWebhook().url(url));
			
			if (!resp.isOk()) {

				BotLog.debug("SET WebHook for " + botName() + " Failed : " + resp.description());

				BotServer.fragments.remove(token);

			}


		}

	}

	public void stop() {

		if (!isLongPulling()) {

			bot.execute(new DeleteWebhook());

		} else {

			bot.removeGetUpdatesListener();

		}
		
	}


}
