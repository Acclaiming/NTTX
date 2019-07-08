package io.kurumi.ntt.fragment.sticker;

import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.request.AddStickerToSet;
import com.pengrad.telegrambot.request.CreateNewStickerSet;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.coobird.thumbnailator.Thumbnails;
import cn.hutool.core.io.FileUtil;

public class NewSet extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("newset");
		registerPoint(POINT_CREATE_SET);

	}

	final String POINT_CREATE_SET = "set_create";

	final String CREATE_COPY = "复制已有贴纸包";
	final String CREATE_BY_IMAGE = "使用任意图片创建";
	final String CREATE_BY_STICKER = "使用已有贴纸创建";

	class CreateSet extends PointData {

		int type = 0;

		String name;
		String title;

		public File file;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		CreateSet data = new CreateSet();

		setPrivatePoint(user,POINT_CREATE_SET,data);

		msg.send("好，一个新贴纸包 现在请发送标题 :","\n注意 : 虽然NTT支持通过自动重建的方式修改标题,但是还是想好再输入吧 ~").exec(data);

	}

	final String DOC = Html.a("相关法律法规","https://core.telegram.org/bots/api#createnewstickerset");

	@Override
	public int checkPoint(UserData user,Msg msg,String point,PointData data) {

		return ((CreateSet)data).type == 3 ? PROCESS_THREAD : PROCESS_ASYNC;

	}

	ArrayList<Long> forking = new ArrayList<>();

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		data.context.add(msg);

		CreateSet create = (CreateSet) data;

		if (create.type == 0) {

			if (!msg.hasText()) {

				msg.send("请输入新贴纸包的标题 :").withCancel().exec(data);

				return;

			} else if (msg.text().length() > 64) {

				msg.send("标题太长啦！根据 " + DOC + " 最多64个字 ~").exec(data);

				return;

			}

			create.title = msg.text();
			create.type = 1;

			msg.send("现在发送贴纸集的简称 : 用于添加贴纸的链接 https://t.me/addstickers/你设置的简称 。只能包含英文字母，数字和下划线。必须以字母开头，不能包含连续的下划线。 ","\n并且 : 根据 " + DOC + " , " + Html.b("必须以 '_by_" + origin.me.username().toLowerCase() + "' 结尾。") + " '" + origin.me.username().toLowerCase() + "' 不区分大小写 (不带引号)。").html().exec(data);

		} else if (create.type == 1) {

			if (!msg.hasText()) {

				msg.send("请输入新贴纸包的简称 :").withCancel().exec(data);

				return;

			} else if (msg.text().length() > 64) {

				msg.send("简称太长啦！根据 " + DOC + " 最多64个字 ~").html().exec(data);

				return;

			} else if (!msg.text().toLowerCase().endsWith("_by_" + origin.me.username().toLowerCase())) {

				msg.send("对不起，但是根据 " + DOC + " , " + Html.b("必须以 '_by_" + origin.me.username().toLowerCase() + "' 结尾。") + " '" + origin.me.username().toLowerCase() + "' 不区分大小写 (不带引号)。 :)").html().exec(data);

				return;

			}

			GetStickerSetResponse check = bot().execute(new GetStickerSet(msg.text()));

			if (check != null && check.isOk()) {

				msg.send("对不起，但是这个简称好像已经被使用了 :)").exec(data);

				return;

			}

			create.name = msg.text();
			create.type = 2;

			msg
				.send("好，一个新的贴纸包 选择创建方式 :")
				.keyboard(new Keyboard() {{

						newButtonLine(CREATE_COPY);
						newButtonLine(CREATE_BY_IMAGE);
						newButtonLine(CREATE_BY_STICKER);

					}})
				.withCancel()
				.exec(data);


		} else if (create.type == 2) {

			if (CREATE_COPY.equals(msg.text())) {

				msg.send("现在请发送 目标贴纸包的简称或链接 或目标贴纸包的任意贴纸 : ").removeKeyboard().withCancel().exec(data);

				create.type = 3;

			} else if (CREATE_BY_IMAGE.equals(msg.text())) {

				msg.send("现在请发送任意图片 (建议使用文件格式 直接发送图片会被压缩) : ").removeKeyboard().withCancel().exec(data);

				create.type = 4;

			} else if (CREATE_BY_STICKER.equals(msg.text())) {

				msg.send("现在请发送任意贴纸 : ").removeKeyboard().withCancel().exec(data);

				create.type = 5;

			}

		} else if (create.type == 3) {

			if (forking.contains(user.id)) {

				msg.send("对不起，但是还有未造成的贴纸包复制任务正在进行... 请等待").withCancel().exec(data);

				return;

			} else if (!TAuth.contains(user.id)) {

				msg.send("对不起，但是复制贴纸包需要大量时间，为防止滥用，仅认证了Twitter账号的用户可用。").exec(data);

				return;

			} else {

				forking.add(user.id);

			}

			String target;

			if (msg.hasText() && (target = msg.text()).contains("/")) {

				target = StrUtil.subAfter(target,"/",true);

			} else if (msg.message().sticker() != null) {

				target = msg.message().sticker().setName();

				if (target == null) {

					msg.send("这个贴纸没有贴纸集... 请重试 :)").exec(data);

					return;

				}

			} else {

				msg.send("请发送 目标贴纸包的简称或链接 或目标贴纸包的任意贴纸 : ").withCancel().exec(data);

				return;

			}

			final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

			if (!set.isOk()) {

				msg.send("无法读取贴纸包 " + target + " : " + set.description()).exec(data);

				forking.remove(user.id);

				return;

			}


			clearPrivatePoint(user);

			Msg status = msg.send("正在创建贴纸包...").send();

			BaseResponse resp = bot().execute(new CreateNewStickerSet(user.id.intValue(),create.name,create.title,readStiker(user.id,set.stickerSet().stickers()[0]),set.stickerSet().stickers()[0].emoji()));

			if (!resp.isOk()) {

				status.edit("创建贴纸集失败 请重试 : " + resp.description()).exec();

				forking.remove(user.id);

				return;

			}

			for (int index = 1;index < set.stickerSet().stickers().length;index ++) {

				final Sticker sticker = set.stickerSet().stickers()[index];

				bot().execute(new AddStickerToSet(user.id.intValue(),create.name,readStiker(user.id,sticker),sticker.emoji()) {{

							if (sticker.maskPosition() != null) {

								maskPosition(sticker.maskPosition());

							}

						}});

				status.edit("正在复制贴纸包 进度 : " + (index + 1) + " / " + set.stickerSet().stickers().length).exec();

			}

			forking.remove(user.id);

			status.edit("创建成功！ " + Html.a(create.title,"https://t.me/addstickers/" + create.name)).html().exec(data);

		} else if (create.type == 4) {

			File photo = msg.message().photo() != null ? msg.photo() : msg.file();

			if (photo == null) {

				msg.send("文件下载失败... 请重试").withCancel().exec(data);

				return;

			}

			File local = new File(Env.CACHE_DIR,"sticker_convert_cache/" + (msg.message().photo() != null ? msg.message().photo()[0].fileId() : msg.doc().fileId()) + ".png");

			if (!local.isFile()) {

				local.getParentFile().mkdirs();

				try {

					Thumbnails
						.of(photo)
						.size(512,512)
						.outputQuality(1f)
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

			create.file = local;
			create.type = 6;

			msg.send("输入代表该贴纸的Emoji表情 :").withCancel().exec(data);

		} else if (create.type == 6) {

			if (!msg.hasText()) {

				msg.send("请输入代表该贴纸的Emoji表情 :").withCancel().exec(data);

				return;

			}

			clearPrivatePoint(user);

			Msg status = msg.send("正在创建贴纸包...").send();

			BaseResponse resp = bot().execute(new CreateNewStickerSet(user.id.intValue(),create.name,create.title,FileUtil.readBytes(create.file),msg.text()));

			if (!resp.isOk()) {

				status.edit("创建贴纸集失败 请重试 : " + resp.description()).exec();

				forking.remove(user.id);

				return;

			}

			status.edit("创建成功！ " + Html.a(create.title
										  ,"https://t.me/addstickers/" + create.name)).html().exec(data);

		}

	} 

}
