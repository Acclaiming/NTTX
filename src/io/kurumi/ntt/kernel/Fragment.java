package io.kurumi.ntt.kernel;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.kernel.structure.*;
import java.util.*;
import org.telegram.bot.handlers.*;
import org.telegram.bot.kernel.*;
import org.telegram.bot.kernel.database.*;
import org.telegram.bot.kernel.differenceparameters.*;
import org.telegram.bot.structure.*;

public class Fragment extends DefaultUpdatesHandler implements DatabaseManager {

	static Data<ChatSign> chatData = new Data<ChatSign>(ChatSign.class);
	static Data<UserSign> userData = new Data<UserSign>(UserSign.class);
	
	static class Differences {
		
		
	}
	
	@Override
	public Chat getChatById(int chatId) {
		
		return chatData.getById((long)chatId);
		
	}

	@Override
	public IUser getUserById(int userId) {
		
		return userData.getById((long)userId);
		
	}

	@Override
	public Map<Integer, int[]> getDifferencesData() {
		// TODO: Implement this method
		return null;
	}

	@Override
	public boolean updateDifferencesData(int botId,int pts,int date,int seq) {
		// TODO: Implement this method
		return false;
	}
	
	public Fragment(IKernelComm kernelComm, IDifferenceParametersService erenceParametersService, DatabaseManager databaseManager) {
		
		super(kernelComm,erenceParametersService,databaseManager);
		
	}
	
}
