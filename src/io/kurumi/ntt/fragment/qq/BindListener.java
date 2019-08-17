package io.kurumi.ntt.fragment.qq;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import cn.hutool.core.util.StrUtil;

public class BindListener extends TinxListener {

	@Override
	public void onMsg(MessageUpdate msg) {
		
		if (BindGroup.groupIndex.containsKey(msg.group_id)) {
			
			Long chatId = BindGroup.groupIndex.get(msg.group_id);
			
			new Send(chatId,formatMessage(msg)).html().async();
			
		}
		
	}
	
	String formatMessage(MessageUpdate update) {
		
		String message = Html.b(StrUtil.isBlank(update.sender.card) ? update.sender.nickname : update.sender.card);
		
		message += " : " + update.message;
		
		return message;
		
	}
	
}
