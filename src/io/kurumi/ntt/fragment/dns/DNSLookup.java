package io.kurumi.ntt.fragment.dns;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import io.kurumi.ntt.utils.Html;

public class DNSLookup extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("dns");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length == 0) {

			msg.invalidParams("domain").async();

			return;

		}

		int type = Type.A;

		if (params.length > 1) {

			type = Type.value(params[1]);

			if (type < 0) {

				msg.send("Invalid DNS Type").async();

				return;

			}

		}

		Lookup lookup;

		try {

			lookup = new Lookup(params[0],type);

		} catch (TextParseException e) {

			msg.send("Invalid Domain Name").async();

			return;

		}

		lookup.run();

		if (lookup.getResult() != Lookup.SUCCESSFUL) {

			msg.send(lookup.getErrorString()).async();

			return;

		}

		String message = "域名 : " + params[0] + " 查询结果 (" + type + ") : ";

		Record[] records = lookup.getAnswers();

		if (records.length == 0) {

			message += "没有记录";

		} else {

			message += "\n";

			for (Record record : records) {

				message += "\n" + Type.string(record.getType()) + " " + Html.code(record.rdataToString());

			}

		}

		msg.send(message).async();

	}

}
