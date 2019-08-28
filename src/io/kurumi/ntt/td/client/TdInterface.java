package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi.*;

import cn.hutool.core.util.NumberUtil;
import java.util.LinkedList;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.Env;
import cn.hutool.core.util.ArrayUtil;

public abstract class TdInterface {
	
	public TdClient client;
	
	public long send(Function function) {

		return client.send(function);

	}
	
	public <T extends TdApi.Object> T execute(TdApi.Function function) throws TdException {

		return client.execute(function);

	}
	
	public void execute(Function function,TdCallback<?> callback) {

		client.execute(function,callback);

	}
	
	public TdPoint getPointStore() {

		return client.point;

	}
	
	public void send(SMBuilder function) {
		
		send(function.build());
		
	}
	
	public  Message execute(SMBuilder function) throws TdException {
		
		return execute(function.build());
		
	}
	
	public boolean isAdmin(int userId) {
		
		return ArrayUtil.contains(Env.ADMINS,userId);
		
	}
	
	public int superGroupId(Long chatId) {
		
		return NumberUtil.parseInt(chatId.toString().substring(4));

	}

	public SMBuilder chatId(long chatId) {

		return new SMBuilder(chatId);

	}

	public static class SMBuilder {

		long chatId;

		public SMBuilder(long chatId) {

			this.chatId = chatId;

		}

		long replyToMessageId = 0;

		public SMBuilder replyToMessageId(long replyToMessageId) { this.replyToMessageId = replyToMessageId; return this; }

		boolean disableNotification = false;

		public SMBuilder disableNotification() { this.disableNotification = true;return this; }

		boolean fromBackground = false;

		public SMBuilder fromBackground() { this.fromBackground = true;return this; }

		ReplyMarkup replyMarkup;

		public SMBuilder replyMarlup(ReplyMarkup markup) { this.replyMarkup = markup;return this; }
		public SMBuilder removeKeyBoard(boolean isPersional) { this.replyMarkup = new ReplyMarkupRemoveKeyboard(isPersional);return this; }
		public SMBuilder forceReply(boolean isPersional) { this.replyMarkup = new ReplyMarkupForceReply(isPersional);return this; }

		InputMessageContent content;

		public SMBuilder input(InputMessageContent input) { this.content = input;return this; }

		public SMBuilder input(FormattedText text) { 

			this.content = new InputMessageText(text,true,false);

			return this;

		}

		public SMBuilder input(FormattedText text,boolean enablePreview) { 

			this.content = new InputMessageText(text,!enablePreview,false);

			return this;

		}

		public SMBuilder input(FormattedText text,boolean enablePreview,boolean clearDraft) { 

			this.content = new InputMessageText(text,!enablePreview,clearDraft);

			return this;

		}

		public SMBuilder input(TextBuilder text) { 

			this.content = new InputMessageText(text.build(),true,false);

			return this;

		}

		public SMBuilder input(TextBuilder text,boolean enablePreview) { 

			this.content = new InputMessageText(text.build(),!enablePreview,false);

			return this;

		}

		public SMBuilder input(TextBuilder text,boolean enablePreview,boolean clearDraft) { 

			this.content = new InputMessageText(text.build(),!enablePreview,clearDraft);

			return this;

		}



		public SendMessage build() {

			return new SendMessage(chatId,replyToMessageId,disableNotification,fromBackground,replyMarkup,content);

		}
		
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

	public TextBuilder preCode(String text,String lang) {

		return new TextBuilder().preCode(text,lang);

	}

	public TextBuilder url(String text,String url) {

		return new TextBuilder().url(text,url);

	}

	public TextBuilder mention(String text,int userId) {

		return new TextBuilder().mention(text,userId);

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

		void entity(String text,TextEntityType type) {

			TextEntity entity = new TextEntity(builder.length(),text.length(),type);

			entities.add(entity);

			builder.append(text);

		}

		public TextBuilder mention(String text) {

			entity(text,new TextEntityTypeMention());

			return this;

		}

		public TextBuilder hashTag(String text) {

			entity(text,new TextEntityTypeHashtag());

			return this;

		}

		public TextBuilder cashTag(String text) {

			entity(text,new TextEntityTypeCashtag());

			return this;

		}

		public TextBuilder command(String text) {

			entity(text,new TextEntityTypeBotCommand());

			return this;

		}

		public TextBuilder url(String text) {

			entity(text,new TextEntityTypeUrl());

			return this;

		}

		public TextBuilder email(String text) {

			entity(text,new TextEntityTypeEmailAddress());

			return this;

		}

		public TextBuilder bold(String text) {

			entity(text,new TextEntityTypeBold());

			return this;

		}

		public TextBuilder italic(String text) {

			entity(text,new TextEntityTypeItalic());

			return this;

		}

		public TextBuilder code(String text) {

			entity(text,new TextEntityTypeCode());

			return this;

		}

		public TextBuilder pre(String text) {

			entity(text,new TextEntityTypePre());

			return this;

		}

		public TextBuilder preCode(String text,String lang) {

			entity(text,new TextEntityTypePreCode(lang));

			return this;

		}

		public TextBuilder url(String text,String url) {

			entity(text,new TextEntityTypeTextUrl(url));

			return this;

		}

		public TextBuilder mention(String text,int userId) {

			entity(text,new TextEntityTypeMentionName(userId));

			return this;

		}

		public TextBuilder phoneNumber(String text) {

			entity(text,new TextEntityTypePhoneNumber());

			return this;

		}

		public FormattedText build() {

			return new FormattedText(builder.toString(),entities.toArray(new TextEntity[entities.size()]));

		}

	}

	public FormattedText html(String text) throws TdException {

		return execute(new ParseTextEntities(text,new TextParseModeHTML()));

	}

	public FormattedText markdown(String text) throws TdException {

		return execute(new ParseTextEntities(text,new TextParseModeMarkdown()));

	}
	
	public TdPointData getPrivatePoint(int userId) {

		synchronized (getPointStore().privatePoints) {

			return getPointStore().privatePoints.get(userId);

		}

	}

	public void setPrivatePoint(int userId,String point,String actionName) {

		setPrivatePoint(userId,point,actionName,new TdPointData());

	}

	public void setPrivatePoint(int userId,String point,String actionName,TdPointData data) {

		data.chatType = 0;
		data.point = point;
		data.actionName = actionName;

		synchronized (getPointStore().privatePoints) {

			getPointStore().privatePoints.put(userId,data);

		}

	}

	public TdPointData getGroupPoint(long chatId,int userId) {

		synchronized (getPointStore().groupPoints) {

			if (!getPointStore().groupPoints.containsKey(chatId)) return null;

			return getPointStore().groupPoints.get(chatId).points.get(userId);

		}

	}

	public void setGroupPoint(long chatId,int userId,String point,String actionName) {

		setGroupPoint(chatId,userId,point,actionName,new TdPointData());

	}

	public void setGroupPoint(long chatId,final int userId,String point,String actionName,final TdPointData data) {

		data.chatType = 1;
		data.point = point;
		data.actionName = actionName;

		synchronized (getPointStore().groupPoints) {

			if (getPointStore().groupPoints.containsKey(chatId)) {

				getPointStore().groupPoints.get(chatId).points.put(userId,data);

			} else {

				getPointStore().groupPoints.put(chatId,new TdPoint.Group() {{ points.put(userId,data); }});

			}

		}

	}

	
	
}
