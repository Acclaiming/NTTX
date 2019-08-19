package io.kurumi.ntt.model;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.AbstractSend;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Edit;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.NTT;
import java.io.File;
import cn.hutool.log.StaticLog;

public class Msg extends Context {

    public Update update;

    public static String[] NO_PARAMS = new String[0];

	private Msg replyTo;
    private int isCommand = 0;
    private boolean noPayload = false;
    private String payload[];
    private boolean noParams = false;

    private Message message;
    private String name;
	private String function;

    private String[] params;
	private String[] fixedParams;
	private String param;

    public Msg(Message message) {

        this(Launcher.INSTANCE,message);

    }

    public Msg(Fragment fragment,Message message) {

        super(fragment,message.chat());

        this.fragment = fragment;
        this.message = message;

        if (message.replyToMessage() != null) {

            replyTo = new Msg(fragment,message.replyToMessage());

        }

    }

	public void setFunctionAndParam(String function,String... params) {

		this.function = function;
		this.params = params;
		this.fixedParams = params;

	}

    public static Msg from(Fragment fragment,SendResponse resp) {

        if (resp != null && resp.isOk()) return new Msg(fragment,resp.message());

        return null;

    }

	public void invalidQuery() {

		if (this instanceof Callback) {

			((Callback)this).alert("Invalid Query :)");

		} else {

			send("Invalid Query :)").async();

		}

	}

    public Send invalidParams(String... params) {

        return reply("/" + command() + " <" + ArrayUtil.join(params,"> <") + ">");

    }

    public Sticker sticker() {

        return message.sticker();

    }

    public UserData from() {

        return UserData.get(message.from());

    }

    public Message message() {
        return message;
    }

	private UserData newData;

	public UserData newUser() {

		if (message.newChatMembers() == null) return null;

		if (newData == null) {

			newData = UserData.get(message.newChatMembers()[0]);

		}

		return newData;

	}

    public int messageId() {
        return message.messageId();
    }

    public boolean hasText() {

        return message.text() != null;

    }

    public Document doc() {

        return message.document();

    }

    public boolean isStartPayload() {

        return "start".equals(command()) && params().length > 0;

    }

    public String text() {

        return message.text();

    }

    public boolean isGroupAdmin() {

        return NTT.isGroupAdmin(fragment,chatId(),message.from().id());

    }

    public boolean isReply() {

        return message.replyToMessage() != null;

    }


    public AbstractSend sendOrEdit(boolean edit,String msg) {

        if (edit) return edit(msg);
        else return send(msg);

    }

	public Send send(String msg) {

        Send send = new Send(fragment,chatId(),msg);

        send.origin = this;

        return send;

    }

    public Send send(String msg,Object... params) {

        Send send = new Send(fragment,chatId(),msg,params);

        send.origin = this;

        return send;

    }

    public Msg sendSticker(String sticker) {

        return fragment.sendSticker(chatId(),sticker);


    }

    public Msg sendFile(long chatId,String file) {

        return fragment.sendFile(chatId,file);

    }

    public Msg sendFile(File file) {

        return fragment.sendFile(chatId(),file);
    }

    public Msg sendFile(byte[] file) {

        return fragment.sendFile(chatId(),file);

    }

    public void exit() {

        fragment.executeAsync(update,new LeaveChat(chatId()));

    }

    public void sendTyping() {

        if (isPrivate()) {

            fragment.sendTyping(chatId());

        }

    }

    public boolean contactable() {

        return from().contactable(fragment);

    }

    public void sendUpdatingFile() {

        fragment.sendUpdatingFile(chatId());
    }

    public void sendUpdatingPhoto() {

        fragment.sendUpdatingPhoto(chatId());

    }

    public void sendUpdatingAudio() {

        fragment.sendUpdatingAudio(chatId());

    }

    public void sendUpdatingVideo() {

        fragment.sendUpdatingVideo(chatId());
    }

    public void sendUpdatingVideoNote() {

        fragment.sendUpdatingVideoNote(chatId());
    }

    public void sendFindingLocation() {

        fragment.sendFindingLocation(chatId());
    }

    public void sendRecordingAudio(long chatId) {

        fragment.sendRecordingAudio(chatId());

    }

    public void sendRecordingViedo(long chatId) {

        fragment.sendRecordingViedo(chatId());

    }

    public void sendRecordingVideoNote() {

        fragment.sendRecordingVideoNote(chatId());

    }

    public Msg replyTo() {

        return replyTo;

    }

    public Send reply(String msg,Object... params) {

        return send(msg,params).replyTo(this);

    }

    public Edit edit(String... msg) {

        Edit edit = new Edit(fragment,chatId(),messageId(),msg);

        edit.origin = this;

        return edit;

    }

    public void editMarkup(ButtonMarkup markup) {

        fragment.executeAsync(update,new EditMessageReplyMarkup(chatId(),messageId()).replyMarkup(markup.markup()));

    }

    public Boolean deleted;

    public void delete() {

        if (deleted != null) return;

        deleted = true;

        delete(messageId());

    }

    public void delete(int messageId) {

        fragment.executeAsync(update,new DeleteMessage(chatId(),messageId));

    }

    public void kick() {

        kick(false);

    }

    public void kick(boolean ban) {

        kick(from().id,ban);

    }

    public void kick(Long userId) {

        kick(userId,false);

    }

    public void kick(Long userId,boolean ban) {

		// BotLog.errorWithStack("kick");

        if (userId.equals(fragment.origin.me.id())) {

            StaticLog.warn("错误 : 移除自己");

        } else {

            if (ban) {

                fragment.executeAsync(update,new KickChatMember(chatId(),userId.intValue()));

            } else {

                fragment.execute(new KickChatMember(chatId(),userId.intValue()));
                fragment.execute(new UnbanChatMember(chatId(),userId.intValue()));

            }

        }

    }

	public void unrestrict() {

		unrestrict(from().id);

	}


    public void unrestrict(Long userId) {

        fragment.executeAsync(update,new RestrictChatMember(chatId(),userId)
							  .canSendMessages(true)
							  .canSendMediaMessages(true)
							  .canSendOtherMessages(true)
							  .canAddWebPagePreviews(true)
							  );
    }

	public void restrict() {

		restrict(from().id);

	}

    public void restrict(Long userId) {

        fragment.executeAsync(update,new RestrictChatMember(chatId(),userId)
							  .canSendMessages(false)
							  .canSendMediaMessages(false)
							  .canSendOtherMessages(false)
							  .canAddWebPagePreviews(false)
							  );

    }

    public void restrict(Long userId,long until) {

        fragment.executeAsync(update,new RestrictChatMember(chatId(),from().id)
							  .canSendMessages(false)
							  .canSendMediaMessages(false)
							  .canSendOtherMessages(false)
							  .canAddWebPagePreviews(false)
							  .untilDate((int) until));

    }


    public Msg forwardTo(Object chatId) {

        return Msg.from(fragment,fragment.execute(new ForwardMessage(chatId,chatId(),messageId())));

    }

	public PhotoSize maxSize() {

		PhotoSize[] sizes = message.photo();

		return sizes[sizes.length - 1];

	}

    public File photo() {

		PhotoSize[] sizes = message.photo();

		if (sizes == null) return null;

        File local = new File(Env.CACHE_DIR,"files/" + sizes[sizes.length - 1].fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(sizes[sizes.length - 1].fileId())).file());

        HttpUtil.downloadFile(path,local);

        return local;


    }

    public File file() {

        Document doc = message.document();

        if (doc == null) return null;

        return fragment.getFile(doc.fileId());

    }

    public boolean isCommand() {

        if (isCommand == 0) {

            if (text() != null && text().startsWith("/") && text().length() > 1) {

                String body = text().substring(1);

                if (body.contains(" ")) {

                    String cmdAndUser = StrUtil.subBefore(body," ",false);

                    if (cmdAndUser.contains("@" + fragment.origin.me.username())) {

                        name = StrUtil.subBefore(cmdAndUser,"@",false);

                    } else {

                        name = cmdAndUser;

                    }

                } else if (body.contains("@" + fragment.origin.me.username())) {

                    name = StrUtil.subBefore(body,"@",false);

                } else {

                    name = body;

                }

                isCommand = 1;

            } else if (text() != null && text().startsWith("!") && text().length() > 1) {

				isCommand = 1;

			} else {

                isCommand = 2;

            }

        }

        return isCommand == 1;

    }

    public String command() {

        if (!isCommand()) return null;

		if (function != null) return function;

        String body = text().substring(1);

        if (body.contains(" ")) {

            String cmdAndUser = StrUtil.subBefore(body," ",false);

            if (cmdAndUser.contains("@" + fragment.origin.me.username())) {

                name = StrUtil.subBefore(cmdAndUser,"@",false);

            } else {

                name = cmdAndUser;

            }

        } else if (body.contains("@" + fragment.origin.me.username())) {

            name = StrUtil.subBefore(body,"@",false);

        } else {

            name = body;

        }

		function = name;

        return name;

    }

    public String[] payload() {

        if (noPayload) return NO_PARAMS;

        if (payload != null) return payload;

        if (!isStartPayload()) {

            noPayload = true;

            return NO_PARAMS;

        }

        payload = params()[0].split("_");

        return payload;

    }

	public String param() {

		if (param != null) return param;

		if (noParams) {

            return null;

        }

		if (!isCommand()) {

            noParams = true;

            return null;

        }

		String body = StrUtil.subAfter(text(),"/",false);

        if (body.contains(" ")) {

            param = StrUtil.subAfter(body," ",false);
			params = param.split(" ");
			fixedParams = param.replace("  "," ").split(" ");

        } else {

            noParams = true;

			param = "";
			params = NO_PARAMS;
            params = NO_PARAMS;

        }

		return param;

	}

	public String[] fixedParams() {

		if (fixedParams != null) return fixedParams;

		if (noParams) {

            return NO_PARAMS;

        }

		if (!isCommand()) {

            noParams = true;

            return NO_PARAMS;

        }

		String body = StrUtil.subAfter(text(),"/",false);

        if (body.contains(" ")) {

            param = StrUtil.subAfter(body," ",false);
			params = param.split(" ");
			fixedParams = param.replace("  "," ").split(" ");

        } else {

            noParams = true;

			param = "";
			params = NO_PARAMS;
            params = NO_PARAMS;

        }

		return fixedParams;

	}


    public String[] params() {

        if (params != null) return params;

        if (noParams) {

            return NO_PARAMS;

        }

        if (!isCommand()) {

            noParams = true;

            return NO_PARAMS;

        }

        String body = StrUtil.subAfter(text(),"/",false);

        if (body.contains(" ")) {

            param = StrUtil.subAfter(body," ",false);
			params = param.split(" ");
			fixedParams = param.replace("  "," ").split(" ");

        } else {

            noParams = true;

			param = "";
			params = NO_PARAMS;
            params = NO_PARAMS;

        }

        return params;

    }


}
