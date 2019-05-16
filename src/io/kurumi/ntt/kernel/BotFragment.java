package io.kurumi.ntt.kernel;

import java.lang.reflect.*;
import org.telegram.bot.*;
import org.telegram.bot.handlers.*;
import org.telegram.bot.handlers.interfaces.*;
import org.telegram.bot.kernel.*;
import org.telegram.bot.kernel.database.*;
import org.telegram.bot.kernel.differenceparameters.*;
import org.telegram.bot.structure.*;

public class BotFragment extends TelegramBot {
	
	static final int APP_ID = 205444;
	static final String API_HASH = "799f4903cc45b287cc897d30a082a2db";
	
	public BotFragment(String botToken) {
		
		super(new BotApiConfig(botToken),new Processer(),APP_ID,API_HASH);
		
	}
	
	static class BotApiConfig extends BotConfig {

		public String token;

		public BotApiConfig(String botToken) {
			this.token = botToken;
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
	
 class Processer implements ChatUpdatesBuilder {

		private IKernelComm kernelComm;
		private IUsersHandler usersHandler;
		private BotConfig botConfig;
		private IChatsHandler chatsHandler;
		private IDifferenceParametersService differenceParametersService;
		private DatabaseManager databaseManager;

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
			return databaseManager;
		}

		public Processer setUsersHandler(IUsersHandler usersHandler) {
			this.usersHandler = usersHandler;
			return this;
		}

		public Processer setChatsHandler(IChatsHandler chatsHandler) {
			this.chatsHandler = chatsHandler;
			return this;
		}

		public Processer setBotConfig(BotConfig botConfig) {
			this.botConfig = botConfig;
			return this;
		}

		public Processer setDatabaseManager(DatabaseManager databaseManager) {
			this.databaseManager = databaseManager;
			return this;
		}

		@Override
		public UpdatesHandlerBase build() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		
			if (kernelComm == null) {
				throw new NullPointerException("Can't build the handler without a KernelComm");
			}
			if (differenceParametersService == null) {
				throw new NullPointerException("Can't build the handler without a differenceParamtersService");
			}

			
			return ;
			
		}
	}
	
}
