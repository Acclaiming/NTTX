package io.kurumi.ntt.fragment.qq;

import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.db.Data;
import java.util.HashMap;

public class TelegramBridge {
	
	public static Data<GroupBind> data = new Data<>(GroupBind.class);

	public static class GroupBind {

		public Long id;
		public Long groupId;

	}

	public static HashMap<Long,Long> telegramIndex = new HashMap<>();
	public static HashMap<Long,Long> qqIndex = new HashMap<>();

	static {

		for (GroupBind bind : data.getAll()) {

			telegramIndex.put(bind.id,bind.groupId);
			qqIndex.put(bind.groupId,bind.id);

		}
		
	}

	
	public static void qqTotelegram(MessageUpdate update) {
		
		
	}
	
}
