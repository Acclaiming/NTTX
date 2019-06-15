package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.abs.*;
import io.kurumi.ntt.fragment.admin.*;
import io.kurumi.ntt.fragment.base.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.*;

import io.kurumi.ntt.fragment.abs.Callback;
import java.util.concurrent.atomic.*;
import io.kurumi.ntt.fragment.BotFragment.*;
import cn.hutool.core.thread.*;

public abstract class BotFragment extends Fragment implements UpdatesListener {

	static LinkedBlockingQueue<UserAndUpdate> queue = new LinkedBlockingQueue<>();
	static LinkedList<ProcessThread> threads = new LinkedList<>();
	
    public User me;
    private TelegramBot bot;
    private LinkedList<Fragment> fragments = new LinkedList<>();
    private String token;
    private PointStore point;

	public static void startThreads(int count) {
		
		for (int index = 0;index < count;index ++) {
			
			threads.add(new ProcessThread());
			
		}
		
	}
	
	public static void stopThreads() {
		
		ProcessThread thread = threads.remove();
		
		thread.stopped.set(true);
		
		thread.interrupt();

	}
	
	class UserAndUpdate {

		long targetId;

		UserData user;

		Update update;

		final boolean point = user != null && point().contains(user);

		void process() {

			if (update.message() != null) {

				Msg msg = new Msg(BotFragment.this, update.message());

				for (Fragment fragmnet : fragments) {

					if (fragmnet.onUpdate(user, update)) {

						return;

					}

				}

				for (Fragment fragmnet : fragments) {

					if (!point) {

						if (fragmnet.onMsg(user, msg)) {

							return;

						}

					} else {

						if (fragmnet.onPointedMsg(user, msg)) {

							return;

						}

					}

				}

				switch (update.message().chat().type()) {

					case Private: {

							for (Fragment fragmnet : fragments) {

								if (!point) {

									if (fragmnet.onPrivate(user, msg)) {

										return;

									}

								} else {


									if (fragmnet.onPointedPrivate(user, msg)) {

										return;

									}

								}

							}

							break;

						}

					case group:
					case supergroup: {

							for (Fragment fragmnet : fragments) {

								if (!point) {

									if (fragmnet.onGroup(user, msg)) {

										return;

									}

								} else {

									if (fragmnet.onPointedGroup(user, msg)) {

										return;

									}

								}

							}

							break;

						}

				}

			} else if (update.channelPost() != null) {


				for (Fragment fragmnet : fragments) {

					if (fragmnet.onUpdate(user, update)) {

						return;

					}

				}


				for (Fragment fragmnet : fragments) {

					if (fragmnet.onChanPost(user, new Msg(fragmnet, update.channelPost()))) {

						return;

					}

				}

			} else if (update.callbackQuery() != null) {

				for (Fragment fragmnet : fragments) {

					if (fragmnet.onUpdate(user, update)) {

						return;

					}

				}


				for (Fragment fragmnet : fragments) {

					if (fragmnet.onCallback(user, new Callback(fragmnet, update.callbackQuery()))) {

						return;

					}

				}

			} else if (update.inlineQuery() != null) {


				for (Fragment fragmnet : fragments) {

					if (fragmnet.onUpdate(user, update)) {

						return;

					}

				}


				for (Fragment fragmnet : fragments) {

					if (fragmnet.onQuery(user, new Query(fragmnet, update.inlineQuery()))) {

						return;

					}

				}

			}


		}

	}

	public BotFragment() {

        origin = this;

	}


    @Override
    public TelegramBot bot() {

        return bot;
    }

    public void reload() {

		fragments.clear();

		addFragment(this);

    }

    public void addFragment(Fragment fragment) {

        fragment.origin = this;
        fragments.add(fragment);


    }

    /*

     public boolean isLongPulling() {

     return false;

     }

     */

    public void remFragment(Fragment fragment) {

        fragments.remove(fragment);


    }

    public abstract String botName();

    @Override
    public int process(List<Update> updates) {

        for (final Update update : updates) {

            try {

                processAsync(update);

            } catch (Exception e) {

                BotLog.error("更新出错", e);

                Launcher.INSTANCE.uncaughtException(Thread.currentThread(), e);

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
    public boolean onMsg(UserData user, Msg msg) {

        if ("cancel".equals(msg.command())) {

            msg.send("你要取消什么？ >_<").exec();

            return true;

        }

        return false;

    }

    @Override
    public boolean onPointedMsg(UserData user, Msg msg) {

        if ("cancel".equals(msg.command())) {

            clearPoint(user);

            msg.send("取消成功 ~").removeKeyboard().exec();

            return true;

        }

        return false;

    }

    public void processAsync(final Update update) {

        final UserData user;

        if (update.message() != null) {

            user = UserData.get(update.message().from());

        } else if (update.channelPost() != null) {

            user = update.channelPost().from() != null ? UserData.get(update.channelPost().from()) : null;

        } else if (update.callbackQuery() != null) {

            user = UserData.get(update.callbackQuery().from());

        } else if (update.inlineQuery() != null) {

            user = UserData.get(update.inlineQuery().from());

        } else user = null;


		UserAndUpdate uau = new UserAndUpdate();

		uau.targetId = -1;

		if (user != null) uau.targetId = user.id;
		else if (update.channelPost() != null) uau.targetId = update.channelPost().chat().id();
		else if (update.editedChannelPost() != null) uau.targetId = update.editedChannelPost().chat().id();

		uau.user = user;
		uau.update = update;

		queue.add(uau);


    }

	static class ProcessThread extends Thread {

		static LinkedList<Long> processing = new LinkedList<>();

		AtomicBoolean stopped = new AtomicBoolean(false);

		@Override
		public void run() {

			while (!stopped.get()) {

				UserAndUpdate uau;

				try {

					uau =   queue.take();

				} catch (InterruptedException e) {

					return;

				}

				synchronized (processing) {

					if (processing.contains(uau.targetId)) {

						queue.add(uau);

						continue;

					} else {

						processing.add(uau.targetId);

					}

				}

				uau.process();

				synchronized (processing) {

					processing.remove(uau.targetId);

				}


			}

		}

	}

	public boolean isLongPulling() {

		return false;

	}

	public String getToken() {

		return Env.get("token." + botName());

	}

	public void setToken(String botToken) {

		Env.set("token." + botName(), token);

	}

	public boolean silentStart() {

		token = getToken();

		bot = new TelegramBot.Builder(token).build();

		GetMeResponse resp = bot.execute(new GetMe());

		if (resp == null || !resp.isOk()) return false;

		me = resp.user();

		realStart();

		return true;

	}

	public void start() {

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

		reload();

		bot.execute(new DeleteWebhook());

		if (isLongPulling()) {

			bot.setUpdatesListener(this, new GetUpdates());

		} else {

			/*

			 GetUpdatesResponse update = bot.execute(new GetUpdates());

			 if (update.isOk()) {

			 process(update.updates());

			 }

			 */

			String url = "https://" + BotServer.INSTANCE.domain + "/" + token;

			BotServer.fragments.put(token, this);

			BaseResponse resp = bot.execute(new SetWebhook().url(url));

			BotLog.debug("SET WebHook for " + botName() + " : " + url);

			if (!resp.isOk()) {

				BotLog.debug("Failed... : " + resp.description());

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

		BotLog.info(botName() + " 已停止 :)");

	}


}
