package io.kurumi.ntt.kernel;

import io.kurumi.ntt.db.*;
import java.util.*;
import org.telegram.bot.kernel.database.*;
import org.telegram.bot.structure.*;
import io.kurumi.ntt.kernel.DBManager.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;


public class DBManager implements DatabaseManager {

	public static DBManager INSTANCE = new DBManager();
	
	@Override
	public Chat getChatById(int chatId) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public IUser getUserById(int userId) {
		// TODO: Implement this method
		return null;
	}
	
	public static class Differences {
		
		public int id;
		public int[] diff;
		
	}
	
	static Data<Differences> differencesData = new Data<Differences>("DifferencesData",Differences.class);

	@Override
	public Map<Integer, int[]> getDifferencesData() {
		
		HashMap<Integer, int[]> data = new HashMap<>();
		
		for (Differences diff : differencesData.collection.find()) {
			
			data.put(diff.id,diff.diff);
			
		}
		
		return data;
	}

	@Override
	public boolean updateDifferencesData(int botId,int pts,int date,int seq) {
		
		DBManager.Differences diff = new Differences();
		
		diff.id = botId;
		diff.diff = new int[] { pts,date,seq };
		
		if (differencesData.collection.countDocuments(eq("_id",botId)) > 0) {
			
			return differencesData.collection.replaceOne(eq("_id",botId),diff).getMatchedCount() > 0;
			
		} else {
			
			differencesData.collection.insertOne(diff);
			
			return true;
			
		}

	}

}
