package io.kurumi.ntt.fragment.td;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.client.TdException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.utils.BotLog;
import cn.hutool.log.StaticLog;
import cn.hutool.http.HtmlUtil;

public class TdTest extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("test_get_members");

	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

		return FUNCTION_PUBLIC;

	}

}
