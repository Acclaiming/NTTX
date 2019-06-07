package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.utils.Html;
import cn.hutool.http.HtmlUtil;

public class Users extends Fragment {

	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (!user.developer() || !"users".equals(msg.command())) return false;

		StringBuilder export = new StringBuilder();

		int count = 0;

		for (TAuth auth : TAuth.data.collection.find()) {

			count ++;

			export.append(UserData.get(auth.user).userName()).append(" -> ").append(auth.archive().urlHtml()).append("\n");

			if (count == 50) {

				msg.send(export.toString()).html().exec();

				export = new StringBuilder();

				count = 0;

			}

		}

		if (count > 0) {

			msg.send(export.toString()).html().exec();

		}

		count = 0;

		export = new StringBuilder(HtmlUtil.escape(" >> All Users << \n"));

		for (UserData userData : UserData.data.collection.find()) {

			export.append("\n").append(userData.userName()).append(" ").append(Html.startPayload("Block","drop",userData.id));

			count ++;

			if (count == 50) {

				msg.send(export.toString()).html().exec();

				export = new StringBuilder();

				count = 0;

			}

		}

		if (count > 0) {

			msg.send(export.toString()).html().exec();

		}

		count = 0;

		export = new StringBuilder(HtmlUtil.escape(" >> Blocked Users << \n"));

		for (Firewall.Id id : Firewall.block.collection.find()) {

			UserData userData = UserData.get(id.id);

			if (userData == null) {

				export.append("\n").append(Html.user("[ " + id.id + " ]",id.id)).append(" ").append(Html.startPayload("Accept","accept",id.id));


			} else {

				export.append("\n").append(userData.userName()).append(" ").append(Html.startPayload("Accept","accept",userData.id));

			}

			count ++;

			if (count == 50) {

				msg.send(export.toString()).html().exec();

				export = new StringBuilder();

				count = 0;

			}


		}

		if (count > 0) {

			msg.send(export.toString()).html().exec();

		}

		return true;

	}

}
