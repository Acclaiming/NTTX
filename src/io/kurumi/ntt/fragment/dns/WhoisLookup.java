package io.kurumi.ntt.fragment.dns;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.IOException;
import org.neverfear.whois.ResolveDefault;
import org.neverfear.whois.WhoisQuery;
import org.neverfear.whois.WhoisResponse;
import io.kurumi.ntt.fragment.BotFragment;

public class WhoisLookup extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("whois");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length == 0) {

			msg.invalidParams("domain").async();

			return;

		}

		String result;

		try {

			result = new WhoisQuery(params[0]).getResponse().getData();

		} catch (IOException e) {

			try {

				result = new ResolveDefault("whois.iana.org").query(params[0]).getData();

			} catch (IOException ex) {

				msg.send(ex.getMessage()).async();

				return;
				
			}

		}
		
		String message = null;
		
		for (String line : result.split("\n")) {
			
			if (line.trim().startsWith("%")) continue;
			
			if (message == null) message = line;
			else message += "\n" + line;
			
		}
		
		msg.send(message).async();
		
	}

}
