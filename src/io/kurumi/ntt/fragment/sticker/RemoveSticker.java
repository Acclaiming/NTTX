package io.kurumi.ntt.fragment.sticker;

import com.pengrad.telegrambot.model.StickerSet;
import com.pengrad.telegrambot.request.DeleteStickerFromSet;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.inline.ShowSticker;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.model.request.KeyboradButtonLine;
import java.util.List;

public class RemoveSticker extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("remove_sticker");
		registerPoint(POINT_REMOVE_STICKER);

	}

	final String POINT_REMOVE_STICKER = "remove_sticker";

	class StickerRemove extends PointData {

		int type = 0;
		String setName;
		StickerSet  set;

		@Override
		public void onCancel(UserData user,Msg msg) {
			
			ShowSticker.current.remove(user.id);
			
		}

	}

	@Override
	public void onFunction(UserData user,Msg msg,String functipon,String[] params) {

		final List<PackOwner> all = PackOwner.getAll(user.id);

		if (all.isEmpty()) {

			msg.send("你没有使用NTT创建过贴纸包....","使用 /new_sticker_set 创建").exec();

			return;


		}

		StickerRemove data = new StickerRemove();
		
		setPrivatePoint(user,POINT_REMOVE_STICKER,data);

		msg
			.send("选择贴纸包")
			.keyboard(new Keyboard() {{

					KeyboradButtonLine line = null;

					for (PackOwner pack : all) {

						if (line == null) {

							line = newButtonLine();

							line.newButton(pack.id);

						} else {

							line.newButton(pack.id);

							line = null;

						}

					}

				}})
			.withCancel().exec(data);

	}


	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		StickerRemove rm = (StickerRemove)data;

		if (rm.type == 0) {

			String target = msg.text();

			if (target == null || !PackOwner.data.fieldEquals(target,"owner",user.id)) {

				msg.send("请选择你的贴纸包").withCancel().exec(data);

				return;

			} else if (!target.toLowerCase().endsWith("_by_" + origin.me.username().toLowerCase())) {

				msg.send("根据 " + NewStickerSet.DOC + " ，BOT只能操作由自己创建的贴纸包....").html().withCancel().exec(data);

				return;

			}

			final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

			if (!set.isOk()) {

				msg.send("无法读取贴纸包 ，已删除本地记录 " + target + " : " + set.description()).exec(data);

				PackOwner.data.deleteById(target);

				return;

			}

			rm.set = set.stickerSet();
			rm.setName = set.stickerSet().name();
			ShowSticker.current.put(user.id,set.stickerSet());
			
			msg.send("好，现在发送任意贴纸 / 图片 / 图片文件 :","不建议直接发送图片，会被压到看不清").withCancel().exec(data);

			ShowSticker.current.put(user.id,set.stickerSet());

			rm.type = 1;
			
			msg.send("请选择要移动的贴纸或直接发送")
				.buttons(new ButtonMarkup() {{

						newCurrentInlineButtonLine("选择贴纸","SM_CH");

					}})
				.withCancel().exec(data);

		} else {

			data.with(msg);

			if (msg.sticker() == null) {

				msg.send("请选择 / 发送要移除的贴纸 :").withCancel().exec(data);

				return;

			} else if (msg.sticker().setName() == null) {

				msg.send("这个贴纸不属于任何贴纸包...").withCancel().exec(data);

				return;

			} else if (!msg.sticker().setName().toLowerCase().endsWith("_by_" + origin.me.username().toLowerCase())) {

				msg.send("根据 " + NewStickerSet.DOC + " ，BOT只能操作由自己创建的贴纸包....").html().withCancel().exec(data);

				return;

			}

			if (PackOwner.data.containsId(msg.sticker().setName()) && !PackOwner.data.fieldEquals(msg.sticker().setName(),"owner",user.id)) {

				msg.send("这不是你的贴纸包 :)").exec(data);

				return;

			}

			BaseResponse resp = bot().execute(new DeleteStickerFromSet(msg.sticker().fileId()));

			if (!resp.isOk()) {

				msg.send("移除失败！请重试",resp.description()).withCancel().exec(data);

				return;

			}

			msg.reply("移除成功！ 这可能需要几个小时的时间来生效。","退出移除模式使用 /cancel").exec(data);

		}
		
	}



}
