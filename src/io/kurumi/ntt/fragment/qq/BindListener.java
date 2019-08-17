package io.kurumi.ntt.fragment.qq;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import java.io.File;
import io.kurumi.ntt.Launcher;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.model.request.ParseMode;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;

public class BindListener extends TinxListener {

	@Override
	public void onGroup(MessageUpdate msg) {

		if (BindGroup.groupIndex.containsKey(msg.group_id)) {

			Long chatId = BindGroup.groupIndex.get(msg.group_id);

			String user = Html.b(StrUtil.isBlank(msg.sender.card) ? msg.sender.nickname : msg.sender.card) + " : ";

			if (msg.message.startsWith("[CQ:image,")) {

				String file = StrUtil.subBetween(msg.message,"file=",",");
				String url = StrUtil.subBetween(msg.message,"url=","]");

				File imageCache = new File(Env.CACHE_DIR,"qq_image/" + file);

				if (!imageCache.isFile()) {

					HttpUtil.downloadFile(url,imageCache);

				}

				if (imageCache.getName().endsWith(".gif")) {

					Launcher.INSTANCE.execute(new SendAnimation(chatId,imageCache).caption(user + " [GIF]").parseMode(ParseMode.HTML));

				} else {

					Launcher.INSTANCE.execute(new SendPhoto(chatId,imageCache).caption(user + " [图片]").parseMode(ParseMode.HTML));

				}

			} else {

				new Send(chatId,formatMessage(msg)).html().async();

			}

		}

	}

	String formatMessage(MessageUpdate update) {

		String message = Html.b(StrUtil.isBlank(update.sender.card) ? update.sender.nickname : update.sender.card);

		update.message = CqCodeUtil.replaceFace(update.message);

		message += " : " + HtmlUtil.escape(update.message);

		return message;

	}

}
