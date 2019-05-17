package io.kurumi.ntt.kernel;

import com.pengrad.telegrambot.*;
import io.kurumi.ntt.fragment.*;
import java.lang.reflect.*;
import org.telegram.api.update.*;
import org.telegram.bot.*;
import org.telegram.bot.handlers.*;
import org.telegram.bot.kernel.*;
import org.telegram.bot.kernel.database.*;
import org.telegram.bot.kernel.differenceparameters.*;
import org.telegram.bot.structure.*;

import org.telegram.bot.kernel.TelegramBot;
import org.telegram.api.functions.account.*;
import org.telegram.api.functions.bots.*;
import org.telegram.api.message.*;

public class MtProtoBot extends TelegramBot {

	static final int APP_ID = 205444;
	static final String API_HASH = "799f4903cc45b287cc897d30a082a2db";

	public MtProtoBot(String botToken,BotFragment fragment) {

		super(new BotApiConfig(botToken),new ProcesserBuilder(fragment),APP_ID,API_HASH);
		
	}

	static class BotApiConfig extends BotConfig {

		public String token;

		public BotApiConfig(String botToken) {
			this.token = botToken;
			
			setAuthfile(botToken + ".bin");
		setRegistered(true);
			
		}

		@Override
		public String getPhoneNumber() {
			return null;
		}

		@Override
		public String getBotToken() {
			return token;
		}

		@Override
		public boolean isBot() {
			return true;
		}

	}

	static class UserApiConfig extends BotConfig {

		public String number;

		public UserApiConfig(String number) {
			this.number = number;
		}

		@Override
		public String getPhoneNumber() {
			return number;
		}

		@Override
		public String getBotToken() {
			return null;
		}

		@Override
		public boolean isBot() {
			return false;
		}

	}

	static  class Processer extends DefaultUpdatesHandler {

		BotFragment fragment;

		public Processer(org.telegram.bot.kernel.IKernelComm kernelComm,org.telegram.bot.kernel.differenceparameters.IDifferenceParametersService differenceParametersService,org.telegram.bot.kernel.database.DatabaseManager databaseManager,BotFragment fragment) {

			super(kernelComm,differenceParametersService,databaseManager);

			this.fragment = fragment;

		}

		@Override
		protected void onTLUpdateBotWebhookJSONQueryCustom(TLUpdateBotWebhookJSONQuery update) {
			
			String json = update.getData().getData();

			fragment.processAsync(BotUtils.parseUpdate(json));
			
		}
		
		@Override
		protected void onTLUpdateBotWebhookJSONCustom(TLUpdateBotWebhookJSON update) {

			String json = update.getData().getData();

			fragment.processAsync(BotUtils.parseUpdate(json));

		}

		@Override
		protected void onTLUpdateMessageIdCustom(TLUpdateMessageId update) {
			// TODO: Implement this method
			super.onTLUpdateMessageIdCustom(update);
		}

		@Override
		protected void onTLAbsMessageCustom(TLAbsMessage message) {
			
			if (message instanceof TLMessage) {
				
				
				
			}
			
	    }
		

	}

	static class ProcesserBuilder implements ChatUpdatesBuilder {

		public ProcesserBuilder(BotFragment fragment) {

			this.fragment = fragment;

		}

		private IKernelComm kernelComm;
		private IDifferenceParametersService differenceParametersService;
		private BotFragment fragment;

		@Override
		public void setKernelComm(IKernelComm kernelComm) {
			this.kernelComm = kernelComm;
		}

		@Override
		public void setDifferenceParametersService(IDifferenceParametersService differenceParametersService) {
			this.differenceParametersService = differenceParametersService;
		}

		@Override
		public DatabaseManager getDatabaseManager() {
			return DBManager.INSTANCE;
		}

		@Override
		public UpdatesHandlerBase build() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

			if (kernelComm == null) {
				throw new NullPointerException("Can't build the handler without a KernelComm");
			}
			if (differenceParametersService == null) {
				throw new NullPointerException("Can't build the handler without a differenceParamtersService");
			}

			return new Processer(kernelComm,differenceParametersService,getDatabaseManager(),fragment);

		}
	}

}
