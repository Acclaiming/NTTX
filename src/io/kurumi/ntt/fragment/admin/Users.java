package io.kurumi.ntt.fragment.admin;

import cn.hutool.http.HtmlUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.Html;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.bots.UserBot;

public class Users extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerAdminFunction("users","usage");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	
        StringBuilder export;

		int count = 0;

		
		if ("usage".equals(function)) {
			
			export = new StringBuilder(" >> Authed Users >> \n");
			
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
			
			export = new StringBuilder(" >> User Bots << \n");
			
			for (UserBot bot : UserBot.data.collection.find()) {
				
				count ++;

				export.append(UserData.get(bot.user).userName()).append(" -> [ " + bot.typeName() + " ] @").append(HtmlUtil.escape(bot.userName)).append("\n");

				if (count == 50) {

					msg.send(export.toString()).html().exec();

					export = new StringBuilder();

					count = 0;

				}

			}

			if (count > 0) {

				msg.send(export.toString()).html().exec();

			}
			
			
		} else if (msg.params().length == 0) {

			export = new StringBuilder(HtmlUtil.escape(" >> All Users << \n"));

			for (UserData userData : UserData.data.findByField("contactable",true)) {

				export.append("\n").append(userData.userName()).append(" ").append(Html.startPayload("Block","drop",userData.id));

				count++;

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

				count++;

				if (count == 50) {

					msg.send(export.toString()).html().exec();

					export = new StringBuilder();

					count = 0;

				}


			}

		} else {

			String kw = msg.params()[0];

			export = new StringBuilder(HtmlUtil.escape(" >> Search User << \n"));
			
			for (UserData userData : UserData.data.collection.find(or(regex("firstName",kw),regex("lastName",kw),regex("userName",kw)))) {

				export.append("\n").append(userData.userName()).append(" ").append(Html.startPayload("Block","drop",userData.id));

				count++;

				if (count == 50) {

					msg.send(export.toString()).html().exec();

					export = new StringBuilder();

					count = 0;

				}

			}


		}

        if (count > 0) {

            msg.send(export.toString()).html().exec();

        }

    }

}
