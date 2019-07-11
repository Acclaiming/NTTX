package io.kurumi.ntt.fragment.sticker;

import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.File;
import net.coobird.thumbnailator.Thumbnails;
import java.io.IOException;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;

public class StickerExport extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("sticker");
		registerPoint(POINT_EXPORT_STICKER);
		
	}
	
	final String POINT_EXPORT_STICKER = "sticker_export";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	
		PointData data = setPrivatePoint(user,POINT_EXPORT_STICKER).with(msg);
		
		msg.send("进入贴纸制作模式 :","\n发送任意贴纸将返回原文件","发送任意图片将返回可用于添加贴纸的.png格式文件",/*"发送贴纸包链接将返回整个包 (.zip)",*/"\n使用 /cancel 结束导出").exec(data);
		
	}


	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {
		
		data.context.add(msg);
		
		Message message = msg.message();
		
		if (message.sticker() != null) {
			
			bot().execute(new SendDocument(msg.chatId(),getFile(message.sticker().fileId())).fileName("sticker.png"));
			
			msg.send("Sticker Emojis : " + Html.code(message.sticker().emoji())).exec();
			
		} else if (message.photo() != null || message.document() != null) {
			
			File photo = message.photo() != null ? msg.photo() : msg.file();
			
			if (photo == null) {
				
				msg.send("文件下载失败... 请重试").withCancel().exec(data);
				
				return;
				
			}
			
			File local = new File(Env.CACHE_DIR, "sticker_convert_cache/" + (msg.message().photo() != null ? msg.message().photo()[0].fileId() : msg.doc().fileId()) + ".png");

			if (!local.isFile()) {
				
				local.getParentFile().mkdirs();
				
				try {
					
					Thumbnails
						.of(photo)
						.size(512,512)
						.outputFormat("png")
						.toFile(local);
						
					if (local.length() > 512 * 1024) {

						float outSize = ((512 * 1024) / local.length())/* - 0.3f*/;

						Thumbnails.of(local).outputQuality(outSize).toFile(local);

					}
					
						
				} catch (IOException e) {
					
					msg.send("转码失败 : " + BotLog.parseError(e)).exec(data);
					
					return;
					
				}

			}

			bot().execute(new SendDocument(msg.chatId(),local).fileName("sticker.png"));
			
		} else {
			
			msg.send("正在制作贴纸").withCancel().exec(data);
			
		}
		
	}

	
	
}
