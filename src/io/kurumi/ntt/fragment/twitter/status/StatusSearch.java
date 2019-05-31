package io.kurumi.ntt.fragment.twitter.status;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.twitter.archive.*;

public class StatusSearch extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("search");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		int index = 0;

		long from = -1;

		long to = -1;

		int media = 0;

		int utc = 8;

		String start;

		String end;

		for (;index < params.length;index ++) {

			String param = params[index];

			if (param.startsWith("--from=")) {

				String fromC = StrUtil.subAfter(param,"--from=",false);

				if (NumberUtil.isNumber(fromC)) {

					from = NumberUtil.parseLong(fromC);

				} else {

					if (fromC.startsWith("@")) fromC = fromC.substring(1);

					UserArchive archive = UserArchive.get(fromC);

					if (archive == null) {

						msg.send("没有这个人的记录 : " + fromC).exec();

						return;

					}

					from = archive.id;

				}

			} else if (param.startsWith("--to=")) {

				String toC = StrUtil.subAfter(param,"--to=",false);

				if (NumberUtil.isNumber(toC)) {

					from = NumberUtil.parseLong(toC);

				} else {

					if (toC.startsWith("@")) toC = toC.substring(1);

					UserArchive archive = UserArchive.get(toC);

					if (archive == null) {

						msg.send("没有这个人的记录 : " + toC).exec();

						return;

					}

					from = archive.id;

				}

			} else if (param.startsWith("--media")) {

				media = ("--media".equals(param) || "--media=true".equals(param)) ? 1 : 2;

			} else {
				
				break;
				
			}

		}

	}

}
