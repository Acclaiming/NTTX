package io.kurumi.ntt.td.request;

public class TdSendText extends TdAbsSend {

    public TdSendText(TdMain main, long chatId) {

        super(main, chatId);

    }

    private long replyToMessageId = 0;

    public TdSendText replyToMessageId(long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
        return this;
    }

    private boolean disableNotification = false;

    public TdSendText disableNotification() {
        this.disableNotification = true;
        return this;
    }

    boolean fromBackground = false;

    public TdSendText fromBackground() {
        this.fromBackground = true;
        return this;
    }

    TdSendText replyMarkup;
	
	/*

	public TdSendText replyMarlup(ReplyMarkup markup) { this.replyMarkup = markup;return this; }
	public TdSendText removeKeyBoard(boolean isPersional) { this.replyMarkup = new ReplyMarkupRemoveKeyboard(isPersional);return this; }
	public TdSendText forceReply(boolean isPersional) { this.replyMarkup = new ReplyMarkupForceReply(isPersional);return this; }

		InputMessageContent content;

	public TdSendText input(InputMessageContent input) { this.content = input;return this; }

	public TdSendText inputText(FormattedText text) { 

			this.content = new InputMessageText(text,true,false);

			return this;

		}

	public TdSendText inputText(FormattedText text,boolean enablePreview) { 

			this.content = new InputMessageText(text,!enablePreview,false);

			return this;

		}

	public TdSendText inputText(FormattedText text,boolean enablePreview,boolean clearDraft) { 

			this.content = new InputMessageText(text,!enablePreview,clearDraft);

			return this;

		}

	public TdSendText inputText(TextBuilder text) { 

			this.content = new InputMessageText(text.build(),true,false);

			return this;

		}

	public TdSendText inputText(TextBuilder text,boolean enablePreview) { 

			this.content = new InputMessageText(text.build(),!enablePreview,false);

			return this;

		}

	public TdSendText inputText(TextBuilder text,boolean enablePreview,boolean clearDraft) { 

			this.content = new InputMessageText(text.build(),!enablePreview,clearDraft);

			return this;

		}

		public SendMessage build() {

			return new SendMessage(chatId,replyToMessageId,disableNotification,fromBackground,replyMarkup,content);

		}

	}
	
	
	*/
}
