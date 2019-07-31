package io.kurumi.ntt.fragment.sticker;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.pengrad.telegrambot.model.Message;
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
import io.kurumi.ntt.utils.BotLog;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.coobird.thumbnailator.Thumbnails;

public class StickerExport extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("sticker");
        registerPoint(POINT_EXPORT_STICKER);

    }

    final String POINT_EXPORT_STICKER = "sticker_export";

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (user.blocked()) {

            msg.send("你不能这么做 (为什么？)").async();

            return;

        }

        PointData data = setPrivatePoint(user, POINT_EXPORT_STICKER).with(msg);

        msg.send("进入贴纸制作模式 :", "\n发送任意贴纸将返回原文件", "发送任意图片将返回可用于添加贴纸的.png格式文件", "\n使用 /cancel 结束导出").exec(data);

    }

    @Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        data.context.add(msg);

        Message message = msg.message();

        if (message.sticker() != null) {

            bot().execute(new SendDocument(msg.chatId(), getFile(message.sticker().fileId())).fileName("sticker.png"));

            msg.send(message.sticker().emoji()).exec();

        } else if (message.photo() != null || message.document() != null) {

            File photo = message.photo() != null ? msg.photo() : msg.file();

            if (photo == null) {

                msg.send("文件下载失败... (根据相关法律法规 BOT无法读取超过20m的文件)").withCancel().exec(data);

                return;

            }

            if (photo.getName().endsWith(".zip")) {

                File local = new File(Env.CACHE_DIR, "zip_stickers_export/" + (msg.update.updateId()));

                ZipUtil.unzip(photo, local);

                List<File> files = FileUtil.loopFiles(local);

                Collections.sort(files, new Comparator<File>() {

                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile())
                            return -1;
                        if (o1.isFile() && o2.isDirectory())
                            return 1;
                        return o1.getName().compareTo(o2.getName());
                    }

                });

                for (int index = 0; index < files.size(); index++) {

                    File file = files.get(index);


                    try {

                        if (file.length() > 512 * 1000) {


                            Thumbnails
                                    .of(file)
                                    .size(512, 512)
                                    .outputFormat("png")
                                    .toFile(file);


                            float outSize = ((512 * 1000) / local.length());

                            Thumbnails.of(local).outputQuality(outSize).scale(1).toFile(file);

                        }

                        bot().execute(new SendDocument(msg.chatId(), file).fileName("sticker" + index + ".png"));

                    } catch (IOException e) {

                        msg.send(file.getName() + " 转码失败 : " + BotLog.parseError(e)).exec(data);

                    }

                }

            } else {

                File local = new File(Env.CACHE_DIR, "sticker_convert_cache/" + (msg.message().photo() != null ? msg.message().photo()[0].fileId() : msg.doc().fileId()) + ".png");

                if (!local.isFile()) {

                    local.getParentFile().mkdirs();

                    try {

                        Thumbnails
                                .of(photo)
                                .size(512, 512)
                                .outputFormat("png")
                                .toFile(local);

                        if (local.length() > 512 * 1000) {

                            float outSize = ((512 * 1000) / local.length());

                            Thumbnails.of(local).outputQuality(outSize).scale(1).toFile(local);

                        }


                    } catch (IOException e) {

                        msg.send("转码失败 : " + BotLog.parseError(e)).exec(data);

                        return;

                    }

                }

                bot().execute(new SendDocument(msg.chatId(), local).fileName("sticker.png"));

            }

        } else {

            msg.send("正在贴纸制作模式 ，退出使用 /cancel").exec();

        }

    }


}
