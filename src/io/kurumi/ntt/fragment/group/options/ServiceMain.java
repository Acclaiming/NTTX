package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class ServiceMain extends Fragment {
	
	public static String POINT_SERVICE = "group_serv";

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerCallback(POINT_SERVICE);
		
	}
	
	final String TARGET_DELETE_SERVICE_MSG = "dsm";
	final String TARGET_DELETE_CHANNEL_MSG = "dcm";
	final String TARGET_NOT_TRUST_ADMIN = "nta";
	
	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
	
		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

			callback.invalidQuery();

			return;

		}

        final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));
		
		if (data == null) {
			
			callback.invalidQuery();
			
			return;
			
		}
		
		if (params.length == 1) {
			
			String message = "群组的管理设定. 点击名称查看功能说明";
			
			message += "\n\n" + OptionsMain.doc;
			
			callback.edit(message).buttons(mainMenu(data)).html().async();
			
			return;
			
		}
		
		String target = params[1];
		
		if (TARGET_DELETE_CHANNEL_MSG.equals(target)) {
			
			if (data.delete_channel_msg == null) {

				data.delete_channel_msg = 0;

				callback.text("🛠️  仅取消置顶");

			} else if (data.delete_channel_msg == 0) {

				data.delete_channel_msg = 1;

				callback.text("🛠️  全部删除");

			} else {

				data.delete_channel_msg = null;

				callback.text("🛠️  不处理");

			}
			
		} else if (TARGET_DELETE_SERVICE_MSG.equals(target)) {
			
			if (data.delete_service_msg == null) {

				data.delete_service_msg = 0;

				callback.text("🛠️  保留一条");

			} else if (data.delete_service_msg == 0) {

				data.delete_service_msg = 1;

				callback.text("🛠️  全部删除");

			} else {

				data.delete_service_msg = null;

				callback.text("🛠️  不处理");

			}
			
		} else if (TARGET_NOT_TRUST_ADMIN.equals(target)) {
			
			if (!GroupAdmin.fastAdminCheck(this, data, user.id, true)) {

				callback.alert("您不是该群组的创建者或全权限管理员 无法更改此项");

				return;

			}

			if (data.not_trust_admin == null) {

				data.not_trust_admin = true;

				callback.text("🛠️  已开启");

			} else {

				data.not_trust_admin = null;

				callback.text("🛠️  已关闭");

			}
			
		}
		
		callback.editMarkup(mainMenu(data));
		
	}
	
	ButtonMarkup mainMenu(final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine()
                    .newButton("删除频道消息")
                    .newButton(data.delete_channel_msg == null ? "不处理" : data.delete_channel_msg == 0 ? "取消置顶" : "全部删除", POINT_SERVICE, data.id, TARGET_DELETE_CHANNEL_MSG);

				newButtonLine()
                    .newButton("删除服务消息")
                    .newButton(data.delete_service_msg == null ? "不处理" : data.delete_service_msg == 0 ? "保留一条" : "全部删除", POINT_SERVICE, data.id, TARGET_DELETE_SERVICE_MSG);

				newButtonLine()
                    .newButton("不信任管理员")
                    .newButton(data.not_trust_admin != null ? "✅" : "☑", POINT_SERVICE, data.id, TARGET_NOT_TRUST_ADMIN);

				newButtonLine("🔙", OptionsMain.POINT_OPTIONS, data.id);

			}};

    }
	
}
