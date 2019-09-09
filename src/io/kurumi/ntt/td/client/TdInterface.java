package io.kurumi.ntt.td.client;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.i18n.Locale;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;

import java.util.LinkedList;

public abstract class TdInterface {

    public TdClient client;

    public long send(Function function) {

        return client.send(function);

    }

    public String userName(User user) {

        String name = user.firstName;

        if (!StrUtil.isBlank(user.lastName)) {

            name += " " + user.lastName;

        }

        return name;

    }

    public <T extends TdApi.Object> T E(TdApi.Function function) {

        try {

            return client.execute(function);

        } catch (TdException e) {

            StaticLog.debug("{} : {}", function.getClass().getSimpleName(), e.getMessage());

            return null;

        }

    }


    public <T extends TdApi.Object> T execute(TdApi.Function function) {

        return client.execute(function);

    }

    public void execute(Function function, TdCallback<?> callback) {

        client.execute(function, callback);

    }

    public TdPoint getPointStore() {

        return client.point;

    }

    public void send(SMBuilder function) {

        send(function.build());

    }

    public TMsg execute(SMBuilder function) {

        return new TMsg(client, (Message)execute(function.build()));

    }

    public Locale getLocale(User user) {

        return Locale.get(user);

    }

    public boolean isAdmin(int userId) {

        return ArrayUtil.contains(Env.ADMINS, userId);

    }

    public int superGroupId(Long chatId) {

        return NumberUtil.parseInt(chatId.toString().substring(4));

    }

    public static SMBuilder chatId(long chatId) {

        return new SMBuilder(chatId);

    }

    public static class SMBuilder {

        long chatId;

        public SMBuilder(long chatId) {

            this.chatId = chatId;

        }

        long replyToMessageId = 0;

        public SMBuilder replyToMessageId(long replyToMessageId) {
            this.replyToMessageId = replyToMessageId;
            return this;
        }

        boolean disableNotification = false;

        public SMBuilder disableNotification() {
            this.disableNotification = true;
            return this;
        }

        boolean fromBackground = false;

        public SMBuilder fromBackground() {
            this.fromBackground = true;
            return this;
        }

        ReplyMarkup replyMarkup;

        public SMBuilder replyMarlup(ReplyMarkup markup) {
            this.replyMarkup = markup;
            return this;
        }

        public SMBuilder removeKeyBoard(boolean isPersional) {
            this.replyMarkup = new ReplyMarkupRemoveKeyboard(isPersional);
            return this;
        }

        public SMBuilder forceReply(boolean isPersional) {
            this.replyMarkup = new ReplyMarkupForceReply(isPersional);
            return this;
        }

        InputMessageContent content;

        public SMBuilder input(InputMessageContent input) {
            this.content = input;
            return this;
        }

        public SMBuilder inputText(FormattedText text) {

            this.content = new InputMessageText(text, true, false);

            return this;

        }

        public SMBuilder inputText(FormattedText text, boolean enablePreview) {

            this.content = new InputMessageText(text, !enablePreview, false);

            return this;

        }

        public SMBuilder inputText(FormattedText text, boolean enablePreview, boolean clearDraft) {

            this.content = new InputMessageText(text, !enablePreview, clearDraft);

            return this;

        }

        public SMBuilder inputText(TextBuilder text) {

            this.content = new InputMessageText(text.build(), true, false);

            return this;

        }

        public SMBuilder inputText(TextBuilder text, boolean enablePreview) {

            this.content = new InputMessageText(text.build(), !enablePreview, false);

            return this;

        }

        public SMBuilder inputText(TextBuilder text, boolean enablePreview, boolean clearDraft) {

            this.content = new InputMessageText(text.build(), !enablePreview, clearDraft);

            return this;

        }

        public SendMessage build() {

            return new SendMessage(chatId, replyToMessageId, disableNotification, fromBackground, replyMarkup, content);

        }

    }

    public InputMessageText inputHtml(String html) {

        return new InputMessageText(html(html), true, false);

    }

    public InputMessageText inputMarkdown(String html) {

        return new InputMessageText(html(html), true, false);

    }

    public InputMessageText inputText(FormattedText text) {

        return new InputMessageText(text, true, false);

    }

    public InputMessageText inputText(FormattedText text, boolean enablePreview) {

        return new InputMessageText(text, !enablePreview, false);

    }

    public InputMessageText inputText(FormattedText text, boolean enablePreview, boolean clearDraft) {

        return new InputMessageText(text, !enablePreview, clearDraft);

    }

    public InputMessageText inputText(TextBuilder text) {

        return new InputMessageText(text.build(), true, false);

    }

    public InputMessageText inputText(TextBuilder text, boolean enablePreview) {

        return new InputMessageText(text.build(), !enablePreview, false);

    }

    public InputMessageText inputText(TextBuilder text, boolean enablePreview, boolean clearDraft) {

        return new InputMessageText(text.build(), !enablePreview, clearDraft);

    }

    public TextBuilder text(String text) {

        return new TextBuilder().text(text);

    }

    public TextBuilder mention(String text) {

        return new TextBuilder().mention(text);

    }

    public TextBuilder hashTag(String text) {

        return new TextBuilder().hashTag(text);

    }

    public TextBuilder cashTag(String text) {

        return new TextBuilder().cashTag(text);

    }

    public TextBuilder command(String text) {

        return new TextBuilder().command(text);

    }

    public TextBuilder url(String text) {

        return new TextBuilder().url(text);

    }

    public TextBuilder email(String text) {

        return new TextBuilder().email(text);

    }

    public TextBuilder bold(String text) {

        return new TextBuilder().bold(text);


    }

    public TextBuilder italic(String text) {

        return new TextBuilder().italic(text);

    }

    public TextBuilder code(String text) {

        return new TextBuilder().code(text);

    }

    public TextBuilder pre(String text) {

        return new TextBuilder().pre(text);

    }

    public TextBuilder preCode(String text, String lang) {

        return new TextBuilder().preCode(text, lang);

    }

    public TextBuilder url(String text, String url) {

        return new TextBuilder().url(text, url);

    }

    public TextBuilder mention(String text, int userId) {

        return new TextBuilder().mention(text, userId);

    }

    public TextBuilder phoneNumber(String text) {

        return new TextBuilder().phoneNumber(text);

    }

    public static class TextBuilder {

        private StringBuilder builder = new StringBuilder();
        private LinkedList<TextEntity> entities = new LinkedList<>();

        public TextBuilder text(String text) {

            builder.append(text);

            return this;

        }

        void entity(String text, TextEntityType type) {

            TextEntity entity = new TextEntity(builder.length(), text.length(), type);

            entities.add(entity);

            builder.append(text);

        }

        public TextBuilder mention(String text) {

            entity(text, new TextEntityTypeMention());

            return this;

        }

        public TextBuilder hashTag(String text) {

            entity(text, new TextEntityTypeHashtag());

            return this;

        }

        public TextBuilder cashTag(String text) {

            entity(text, new TextEntityTypeCashtag());

            return this;

        }

        public TextBuilder command(String text) {

            entity(text, new TextEntityTypeBotCommand());

            return this;

        }

        public TextBuilder url(String text) {

            entity(text, new TextEntityTypeUrl());

            return this;

        }

        public TextBuilder email(String text) {

            entity(text, new TextEntityTypeEmailAddress());

            return this;

        }

        public TextBuilder bold(String text) {

            entity(text, new TextEntityTypeBold());

            return this;

        }

        public TextBuilder italic(String text) {

            entity(text, new TextEntityTypeItalic());

            return this;

        }

        public TextBuilder code(String text) {

            entity(text, new TextEntityTypeCode());

            return this;

        }

        public TextBuilder pre(String text) {

            entity(text, new TextEntityTypePre());

            return this;

        }

        public TextBuilder preCode(String text, String lang) {

            entity(text, new TextEntityTypePreCode(lang));

            return this;

        }

        public TextBuilder url(String text, String url) {

            entity(text, new TextEntityTypeTextUrl(url));

            return this;

        }

        public TextBuilder mention(String text, int userId) {

            entity(text, new TextEntityTypeMentionName(userId));

            return this;

        }

        public TextBuilder phoneNumber(String text) {

            entity(text, new TextEntityTypePhoneNumber());

            return this;

        }

        public FormattedText build() {

            return new FormattedText(builder.toString(), entities.toArray(new TextEntity[entities.size()]));

        }

    }

    public FormattedText html(String text) {

        return execute(new ParseTextEntities(text, new TextParseModeHTML()));

    }

    public FormattedText markdown(String text) {

        return execute(new ParseTextEntities(text, new TextParseModeMarkdown()));

    }

    public void sendText(long chatId, String text, java.lang.Object... params) {

        send(chatId(chatId).inputText(text(StrUtil.format(text, params))));

    }

    public void sendText(TMsg msg, String text, java.lang.Object... params) {

        send(msg.sendText(text(StrUtil.format(text, params))));

    }

    public void replyText(TMsg msg, String text, java.lang.Object... params) {

        send(msg.replyTo().inputText(text(StrUtil.format(text, params))));

    }

    public void sendHTML(long chatId, String text, java.lang.Object... params) {

        send(chatId(chatId).inputText(html(StrUtil.format(text, params))));

    }

    public void sendHTML(TMsg msg, String text, java.lang.Object... params) {

        send(msg.sendText(html(StrUtil.format(text, params))));

    }

    public void replyHTML(TMsg msg, String text, java.lang.Object... params) {

        send(msg.replyTo().inputText(html(StrUtil.format(text, params))));

    }

    public void sendMD(long chatId, String text, java.lang.Object... params) {

        send(chatId(chatId).inputText(markdown(StrUtil.format(text, params))));

    }

    public void sendMD(TMsg msg, String text, java.lang.Object... params) {

        send(msg.sendText(markdown(StrUtil.format(text, params))));

    }

    public void replyMD(TMsg msg, String text, java.lang.Object... params) {

        send(msg.replyTo().inputText(markdown(StrUtil.format(text, params))));

    }

    public void sendText(long chatId, FormattedText text) {

        send(chatId(chatId).inputText(text));

    }

    public void sendText(TMsg msg, FormattedText text) {

        send(msg.sendText(text));

    }

    public void replyText(TMsg msg, FormattedText text) {

        send(msg.replyTo().inputText(text));

    }

    public void sendText(long chatId, TextBuilder text) {

        send(chatId(chatId).inputText(text));

    }

    public void sendText(TMsg msg, TextBuilder text) {

        send(msg.sendText(text));

    }

    public void replyText(TMsg msg, TextBuilder text) {

        send(msg.replyTo().inputText(text));

    }

    public TdPointData getPrivatePoint(int userId) {

        synchronized (getPointStore().privatePoints) {

            return getPointStore().privatePoints.get(userId);

        }

    }

    public void setPrivatePoint(int userId, String point, String actionName) {

        setPrivatePoint(userId, point, actionName, new TdPointData());

    }

    public void setPrivatePoint(int userId, String point, String actionName, TdPointData data) {

        data.chatType = 0;
        data.point = point;
        data.actionName = actionName;

        synchronized (getPointStore().privatePoints) {

            getPointStore().privatePoints.put(userId, data);

        }

    }

    public TdPointData getGroupPoint(long chatId, int userId) {

        synchronized (getPointStore().groupPoints) {

            if (!getPointStore().groupPoints.containsKey(chatId)) return null;

            return getPointStore().groupPoints.get(chatId).points.get(userId);

        }

    }

    public void setGroupPoint(long chatId, int userId, String point, String actionName) {

        setGroupPoint(chatId, userId, point, actionName, new TdPointData());

    }

    public void setGroupPoint(long chatId, final int userId, String point, String actionName, final TdPointData data) {

        data.chatType = 1;
        data.point = point;
        data.actionName = actionName;

        synchronized (getPointStore().groupPoints) {

            if (getPointStore().groupPoints.containsKey(chatId)) {

                getPointStore().groupPoints.get(chatId).points.put(userId, data);

            } else {

                getPointStore().groupPoints.put(chatId, new TdPoint.Group() {{
                    points.put(userId, data);
                }});

            }

        }

    }


}
