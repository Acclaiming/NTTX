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

public class PackExport extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("download_sticker_set");
        registerPoint(POINT_EXPORT_SET);

    }

    final String POINT_EXPORT_SET = "export_set";

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

        if (downloading.contains(user.id)) {

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

        final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

        if (!set.isOk()) {

            msg.send("无法读取贴纸包 " + target + " : " + set.description()).exec(data);

            downloading.remove(user.id);

            return;

        }


        Msg status = msg.send("正在下载贴纸包...").send();

        File cachePath = new File(Env.CACHE_DIR,"pack_export_cache/from_update" + msg.update.updateId());

        File cacheDir = new File(cachePath,set.stickerSet().title());

		new File(cacheDir,"src").mkdirs();
		// new File(cacheDir,"png").mkdirs();
		
        for (int index = 0; index < set.stickerSet().stickers().length; index++) {

            Sticker sticker = set.stickerSet().stickers()[index];

			File stickerFile = getFile(sticker.fileId());

			if (sticker != null) {

				File src = new File(cacheDir,"src/" + index + ".webp");
				
				FileUtil.copy(stickerFile,src,true);
				
				try {
					
					BufferedImage image = ImageUtil.read(src);
					
					Img img = new Img(image.getWidth(),image.getHeight(),Color.WHITE);

					img.drawImage(0,0,image,image.getWidth(),image.getHeight());
					
					img.toFile(new File(cacheDir,index + ".jpg"),"jpg");
					
					// Thumbnails.of(src).outputFormat("png").scale(1.0f).outputQuality(1.0f).toFile(new File(cacheDir,"png/" + index + ".png"));
					
				} catch (Exception e) {}

			}
			
		
            status.edit("正在下载贴纸 : " + (index + 1) + " / " + set.stickerSet().stickers().length + " ...").exec();

        }

        status.edit("下载完成 正在打包...").exec();

        File zip = new File(cachePath,set.stickerSet().title() + ".zip");

        ZipUtil.zip(cacheDir.getPath(),zip.getPath(),true);

        status.edit(Html.code(set.stickerSet().name()) + " 导出完成 :)").html().exec();

        msg.sendUpdatingFile();

        bot().execute(new SendDocument(msg.chatId(),zip));

        downloading.remove(user.id);

        msg.send("继续导出请发送简称/链接或目标贴纸包的贴纸\n\n退出导出使用 /cancel").exec(data);

        RuntimeUtil.exec("rm -rf " + cachePath.getPath());

    }

}
