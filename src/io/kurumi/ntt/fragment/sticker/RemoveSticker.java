package io.kurumi.ntt.fragment.sticker;

import com.pengrad.telegrambot.request.DeleteStickerFromSet;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class RemoveSticker extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("remove_sticker");
		registerPoint(POINT_REMOVE_STICKER);
		
	}
	
	final String POINT_REMOVE_STICKER = "remove_sticker";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		PointData data = setPrivatePoint(user,POINT_REMOVE_STICKER).with(msg);

		msg.send("请直接发送要移除的贴纸 (必须是要操作贴纸包里的):","注意 : 根据 " + NewStickerSet.DOC + " NTT只能操作由NTT创建的贴纸包...").html().withCancel().exec(data);
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {
	
		data.with(msg);
		
		if (msg.sticker() == null) {
			
			msg.send("请发送要移除的贴纸 :").withCancel().exec(data);
			
			return;
			
		} else if (msg.sticker().setName() == null) {
			
			msg.send("这个贴纸不属于任何贴纸包...").withCancel().exec(data);
			
			return;
			
		} else if (!msg.sticker().setName().toLowerCase().endsWith("_by_" + origin.me.username().toLowerCase())) {
			
			msg.send("根据 " + NewStickerSet.DOC + " ，BOT只能操作由自己创建的贴纸包....").html().withCancel().exec(data);
			
			return;
			
		}
		
		BaseResponse resp = bot().execute(new DeleteStickerFromSet(msg.sticker().fileId()));

		if (!resp.isOk()) {

			msg.send("移除失败！请重试",resp.description()).withCancel().exec(data);

			return;

		}

		msg.reply("移除成功！ 重启客户端生效 ","退出移除模式使用 /cancel").exec(data);
		
		
	}
	
	
	
}
