package io.kurumi.ntt.fragment.sticker;

import com.pengrad.telegrambot.request.DeleteStickerFromSet;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.sticker.NewStickerSet;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.model.request.KeyboradButtonLine;
import java.util.List;

public class MoveSticker extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("move_sticker");
		registerPoint(POINT_MOVE_STICKER);

	}

	final String POINT_MOVE_STICKER = "remove_sticker";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		final List<PackOwner> all = PackOwner.getAll(user.id);

		if (all.isEmpty()) {

			msg.send("你没有使用NTT创建过贴纸包....","使用 /new_sticker_set 创建").exec();

			return;

		}
		
		PointData data = setPrivatePoint(user,POINT_MOVE_STICKER);

		msg
			.send("请选择贴纸包 / 或直接发送要移动的贴纸")
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

		

	}
	
}
