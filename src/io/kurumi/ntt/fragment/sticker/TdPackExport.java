package io.kurumi.ntt.fragment.sticker;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

import java.io.File;
import java.util.ArrayList;
import io.kurumi.ntt.utils.NTT;
import net.coobird.thumbnailator.Thumbnails;
import java.io.IOException;
import io.kurumi.ntt.utils.Img;
import cn.hutool.core.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.awt.Color;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.listeners.TdMain;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.client.TdException;
import cn.hutool.log.StaticLog;

public class TdPackExport extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("td_ds");
        registerPoint(POINT_EXPORT_SET);

    }

    final String POINT_EXPORT_SET = "td_export_set";

    ArrayList<Long> downloading = new ArrayList<>();

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        PointData data = setPrivatePoint(user,POINT_EXPORT_SET);

        msg.send("现在发送要导出的贴纸包的简称/链接 或 贴纸包中的任意贴纸").exec(data);

    }

	@Override
	public int checkPoint(UserData user,Msg msg,String point,PointData data) {

		return PROCESS_ASYNC;

	}

    @Override
    public void onPoint(UserData user,Msg msg,String point,PointData data) {

        if (!user.admin() && downloading.contains(user.id)) {

            msg.send("请等待上一个贴纸包导出完成").withCancel().exec(data.with(msg));

            return;

        }

        String target;

        if (msg.hasText()) {

            target = msg.text();

            if (target.contains("/")) target = StrUtil.subAfter(target,"/",true);

        } else if (msg.message().sticker() != null) {

            target = msg.message().sticker().setName();

            if (target == null) {

                msg.send("这个贴纸没有贴纸包... 请重试 :)").withCancel().exec(data);

                return;

            }

        } else {

            msg.send("请发送 目标贴纸包的简称或链接 或目标贴纸包的任意贴纸 : ").withCancel().exec(data);

            return;

        }

        downloading.add(user.id);

		TdApi.StickerSet stickerSet;

		TdMain td = Launcher.BETA;

		try {

			stickerSet = td.execute(new TdApi.SearchStickerSet(target));

		} catch (TdException ex) {

            msg.send("无法读取贴纸包 {} : {}",target,ex).exec(data);

            downloading.remove(user.id);

            return;

        }

        Msg status = msg.send("正在下载贴纸包...").send();

        File cachePath = new File(Env.CACHE_DIR,"pack_export_cache/update_" + msg.update.updateId());

        File cacheDir = new File(cachePath,stickerSet.title);

		new File(cacheDir,"src").mkdirs();
		// new File(cacheDir,"png").mkdirs();

		status.edit("正在下载贴纸 这可能需要几分钟的时间...").async();
		
		for (TdApi.Sticker sticker : stickerSet.stickers) {

			TdApi.File stickerFile = sticker.sticker;

			if (stickerFile.local.isDownloadingCompleted || stickerFile.local.isDownloadingActive) continue;

			sticker.sticker = td.execute(new TdApi.DownloadFile(stickerFile.id,32,0,0,true));

		}
		
		status.edit("下载完成 正在转换格式...").async();
		
		int index = 0;

        for (TdApi.Sticker sticker : stickerSet.stickers) {
			
            TdApi.File stickerFile = sticker.sticker;

			StaticLog.debug("贴纸路径 : {}",stickerFile.local.path);
		
			File localFile = new File(stickerFile.local.path);

			if (localFile.isFile()) {

				File src = new File(cacheDir,"src/" + index + ".webp");

				FileUtil.copy(localFile,src,true);

				try {

					BufferedImage image = ImageUtil.read(src);

					Img img = new Img(image.getWidth(),image.getHeight(),Color.WHITE);

					img.drawImage(0,0,image,image.getWidth(),image.getHeight());

					img.toFile(new File(cacheDir,index + ".jpg"),"jpg");

				} catch (Exception e) {}

			}

            index ++;
			
        }
        
        File zip = new File(cachePath,stickerSet.title + ".zip");

        ZipUtil.zip(cacheDir.getPath(),zip.getPath(),true);

        status.edit(Html.code(stickerSet.title) + " 导出完成 :)").html().exec();

        msg.sendUpdatingFile();

        bot().execute(new SendDocument(msg.chatId(),zip));

        downloading.remove(user.id);

        msg.send("继续导出请发送简称/链接或目标贴纸包的贴纸\n\n退出导出使用 /cancel").exec(data);

        RuntimeUtil.exec("rm -rf " + cachePath.getPath());

    }

}
