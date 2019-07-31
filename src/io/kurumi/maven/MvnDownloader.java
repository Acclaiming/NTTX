package io.kurumi.maven;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.google.gson.Gson;
import cn.hutool.json.JSONObject;

public class MvnDownloader extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("mvn");

	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {

		return PROCESS_ASYNC;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length < 3) {

			msg.invalidParams("groupId","artifactId","version").async();

			return;

		}

		MvnResolver resolver = new MvnResolver();

		MvnArtifact result;

		StringBuilder log = new StringBuilder();
		
		try {

			result = resolver.resolve(params[0],params[1],params[2],null,log);

		} catch (MvnException e) {

			msg.send(e.getMessage()).async();

			return;

		}
		
		msg.send(log.toString()).async();

		msg.send(new JSONObject(new Gson().toJson(result,MvnArtifact.class)).toStringPretty()).async();

	}

}
