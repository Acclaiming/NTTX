package io.kurumi.ntt.td.request;

import io.kurumi.ntt.td.TdApi.*;
import java.util.LinkedList;

public class TextBuilder {

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
