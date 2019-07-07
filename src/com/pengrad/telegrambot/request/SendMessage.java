package com.pengrad.telegrambot.request;

import com.pengrad.telegrambot.model.request.ParseMode;

/**
 * stas
 * 5/1/16.
 */
public class SendMessage extends AbstractSendRequest<SendMessage> {

	public transient String text;
	public transient ParseMode mode;
	public transient boolean disableWebPagePreview = true;
	
    public SendMessage(Object chatId, String text) {
        super(chatId);
        setText(text);
    }

	public void setText(String text) {
		this.text = text;
		add("text", text);
	}

	public String getText() {
		return text;
	}

    public SendMessage parseMode(ParseMode parseMode) {
		this.mode = parseMode;
        return add("parse_mode", parseMode.name());
    }

    public SendMessage disableWebPagePreview(boolean disableWebPagePreview) {
		this.disableWebPagePreview = disableWebPagePreview;
        return add("disable_web_page_preview", disableWebPagePreview);
    }
}
