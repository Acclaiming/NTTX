package io.kurumi.ntt.fragment.qq;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.model.request.Send;

public class BindListener extends TinxListener {

	@Override
	public void onGroup(MessageUpdate msg) {
		
		if (BindGroup.groupIndex.containsKey(msg.group_id)) {
			
			Long chatId = BindGroup.groupIndex.get(msg.group_id);
			
			new Send(chatId,formatMessage(msg)).async();
			
		}
		
	}
	
	String formatMessage(MessageUpdate update) {
		
		String message = "「 " + update.sender.card == null ? update.sender.nickname : update.sender.card + " 」";
		
		message += " : " + update.message;
		
		return message;
		
	}
	
}
