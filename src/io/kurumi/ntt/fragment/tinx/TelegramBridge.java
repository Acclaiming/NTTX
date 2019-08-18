package io.kurumi.ntt.fragment.tinx;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.io.File;
import java.util.HashMap;
import io.kurumi.ntt.cqhttp.update.GroupRequest;
import cn.hutool.core.util.ArrayUtil;
import com.mongodb.client.model.Variable;
import io.kurumi.ntt.cqhttp.Variants;
import io.kurumi.ntt.fragment.qq.TelegramBridge.GroupBind;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.cqhttp.update.GroupDecreaseNotice;

public class TelegramBridge {

	public static Data<GroupBind> data = new Data<>(GroupBind.class);

	public static class GroupBind {

		public Long id;
		public Long groupId;

		public Boolean disable;
		
	}

	public static HashMap<Long,Long> telegramIndex = new HashMap<>();
	public static HashMap<Long,Long> qqIndex = new HashMap<>();
	
	public static HashMap<Long,Boolean> disable = new HashMap<>();
	
	static {

		for (GroupBind bind : data.getAll()) {

			telegramIndex.put(bind.id,bind.groupId);
			qqIndex.put(bind.groupId,bind.id);
			
			if (bind.disable != null) disable.put(bind.id,true);

		}

	}
	
}
