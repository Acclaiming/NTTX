package io.kurumi.ntt.fragment;

import cn.hutool.http.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.abs.*;
import io.kurumi.ntt.fragment.abs.request.*;
import io.kurumi.ntt.utils.*;
import java.io.*;

import io.kurumi.ntt.fragment.abs.Callback;
import java.io.File;

public class Fragment {

    public String PAYLOAD_SPLIT = "_";

    public BotFragment origin;

    public TelegramBot bot() {

        return origin.bot();

    }

    public PointStore point() {

        return origin.point();

    }

    public <T> void setPoint(UserData user, String pointTo, PointStore.Type context) {

        point().set(user, context, pointTo, null);

    }

    public <T> void setPoint(UserData user, String pointTo, PointStore.Type context, T content) {

        point().set(user, context, pointTo, content);

    }

    public <T> void setPoint(UserData user, String pointTo, T content) {

        point().set(user, pointTo, content);

    }

    public void setPoint(UserData user, String pointTo) {

        point().set(user, pointTo, null);

    }

    public <T> PointStore.Point<T> clearPoint(UserData user) {

        return point().clear(user);

    }

    public <T> PointStore.Point<T> getPoint(UserData user) {

        return point().get(user);

    }

    public boolean onUpdate(UserData user, Update update) {

		return false;

    }

	public static abstract class Processed implements Runnable {

		public UserData user;
		public  Update update;

		public Processed(UserData user, Update update) {
			this.user = user;
			this.update = update;
		}

		public abstract void process();

		public void run() {

			try {

				process();

			} catch (Exception e) {

				new Send(Env.GROUP, "处理中出错 " + update.toString(), BotLog.parseError(e)).exec();

				if (user != null && !user.developer()) {

					new Send(user.id, "处理出错，已提交报告，可以到官方群组 @NTTDiscuss  继续了解").exec();

				}

			}

		}

	}

	public static Processed EMPTY = new Processed(null, null) {

		@Override
		public void process() {
		}

	};

	public Processed onAsyncUpdate(final UserData user, Update update) {

		if (onUpdate(user, update)) return EMPTY;

		int checked;

		if (update.message() != null) {

			final Msg msg = new Msg(this, update.message());

			int point;

			point = user == null ? 0 :
				!point().contains(user) ? 0 :
				getPoint(user).type == PointStore.Type.Private ? 1 :
				getPoint(user).type == PointStore.Type.Global ? 3 : 2;

			if (point == 0) {

				if ((checked = checkMsg(user, msg)) > 0) {

					final int cd = checked; return new Processed(user, update) {

						@Override
						public void process() {

							onAsyncMsg(user, msg, cd);

						}

					};

				} else if (checked == -1) {

					if (onMsg(user, msg)) return EMPTY;

				}

			} else {

				if ((checked = checkPointedMsg(user, msg)) > 0) {

					final int cd = checked; return new Processed(user, update) {

						@Override
						public void process() {

							onAsyncPointedMsg(user, msg, cd);

						}

					};

				} else if (checked == -1) {

					if (onPointedMsg(user, msg)) return EMPTY;

				}

			}

			switch (update.message().chat().type()) {

				case Private: {

						if (point == 1 || point == 3) {

							if ((checked = checkPointedPrivate(user, msg)) > 0) {

								final int cd = checked; return new Processed(user, update) {

									@Override
									public void process() {

										onAsyncPointedPrivate(user, msg, cd);

									}

								};

							} else if (checked == -1) {

								if (onPointedPrivate(user, msg)) return EMPTY;

							}

						} else {

							if ((checked = checkPrivate(user, msg)) > 0) {

								final int cd = checked; return new Processed(user, update) {

									@Override
									public void process() {

										onAsyncPrivate(user, msg, cd);

									}

								};

							} else if (checked == -1) {

								if (onPrivate(user, msg)) return EMPTY;

							}
						}

						break;

					}

				case group:
				case supergroup: {


						if (point > 1) {

							if ((checked = checkPointedGroup(user, msg)) > 0) {

								final int cd = checked; return new Processed(user, update) {

									@Override
									public void process() {

										onAsyncPointedGroup(user, msg, cd);

									}

								};

							} else if (checked == -1) {

								if (onPointedGroup(user, msg)) return EMPTY;

							}

						} else {

							if ((checked = checkGroup(user, msg)) > 0) {

								final int cd = checked; return new Processed(user, update) {

									@Override
									public void process() {

										onAsyncGroup(user, msg, cd);

									}

								};

							} else if (checked == -1) {

								if (onGroup(user, msg)) return EMPTY;

							}

						}

						break;

					}

			}

		} else if (update.channelPost() != null) {

			if (onChanPost(user, new Msg(this, update.channelPost()))) {

				return EMPTY;

			}

		} else if (update.callbackQuery() != null) {

			if (onCallback(user, new Callback(this, update.callbackQuery()))) {

				return EMPTY;

			}

		} else if (update.inlineQuery() != null) {

			if (onQuery(user, new Query(this, update.inlineQuery()))) {

				return EMPTY;

			}

		} else if (update.poll() != null) {

			if (onPollUpdate(update.poll())) {

				return EMPTY;

			}

		}

		return null;

    }

    public boolean onMsg(UserData user, Msg msg) {

        return false;

    }

    public boolean onPointedMsg(UserData user, Msg msg) {

        return false;

    }

    public boolean onPrivate(UserData user, Msg msg) {

        return false;

    }

    public boolean onPointedPrivate(UserData user, Msg msg) {

        return false;

    }

    public boolean onGroup(UserData user, Msg msg) {

        return false;

    }

    public boolean onPointedGroup(UserData user, Msg msg) {

        return false;

    }

    public boolean onChanPost(UserData user, Msg msg) {

        return false;

    }

	public int checkMsg(UserData user, Msg msg) {

        return -1;

    }

    public int checkPointedMsg(UserData user, Msg msg) {

        return -1;

    }

    public int checkPrivate(UserData user, Msg msg) {

        return -1;

    }

    public int checkPointedPrivate(UserData user, Msg msg) {

        return -1;

    }

    public int checkGroup(UserData user, Msg msg) {

        return -1;

    }

    public int checkPointedGroup(UserData user, Msg msg) {

        return -1;

    }

    public int checkChanPost(UserData user, Msg msg) {

        return -1;

    }

	public void onAsyncMsg(UserData user, Msg msg, int checked) {
    }

    public void onAsyncPointedMsg(UserData user, Msg msg, int checked) {
    }

    public void onAsyncPrivate(UserData user, Msg msg, int checked) {
    }

    public void onAsyncPointedPrivate(UserData user, Msg msg, int checked) {
    }

    public void onAsyncGroup(UserData user, Msg msg, int checked) {
    }

    public void onAsyncPointedGroup(UserData user, Msg msg, int checked) {
    }

    public void onAsyncChanPost(UserData user, Msg msg, int checked) {
    }

	public boolean onPollUpdate(Poll poll) {

		return false;

	}

    public boolean onCallback(UserData user, Callback callback) {

        return false;

    }

    public boolean onQuery(UserData user, Query inlineQuery) {
        return false;
    }

    public File getFile(String fileId) {

        File local = new File(Env.CACHE_DIR, "files/" + fileId);

        if (local.isFile()) return local;

        GetFileResponse file = bot().execute(new GetFile(fileId));

        if (!file.isOk()) {

            BotLog.debug("取文件失败 : " + file.errorCode() + " " + file.description());

            return null;

        }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path, local);

        return local;

    }

    public Msg sendSticker(long chatId, StickerPoint sticker) {

        return Msg.from(this, bot().execute(new SendSticker(chatId, sticker.fileId)));

    }

    public Msg sendSticker(long chatId, String sticker) {

        return Msg.from(this, bot().execute(new SendSticker(chatId, sticker)));

    }


    public Msg sendFile(long chatId, String file) {

        return Msg.from(this, this.bot().execute(new SendDocument(chatId, file)));

    }

    public Msg sendFile(long chatId, File file) {

        return Msg.from(this, bot().execute(new SendDocument(chatId, file)));

    }

    public Msg sendFile(long chatId, byte[] file) {

        return Msg.from(this, bot().execute(new SendDocument(chatId, file)));

    }

    public void sendTyping(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.typing));

    }

    public void sendUpdatingFile(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.upload_document));

    }

    public void sendUpdatingPhoto(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.upload_photo));

    }

    public void sendUpdatingAudio(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.upload_audio));

    }

    public void sendUpdatingVideo(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.upload_video));

    }

    public void sendUpdatingVideoNote(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.upload_video_note));

    }

    public void sendFindingLocation(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.find_location));

    }

    public void sendRecordingAudio(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.record_audio));

    }


    public void sendRecordingViedo(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.record_video));

    }

    public void sendRecordingVideoNote(long chatId) {

        bot().execute(new SendChatAction(chatId, ChatAction.record_video_note));

    }


}
