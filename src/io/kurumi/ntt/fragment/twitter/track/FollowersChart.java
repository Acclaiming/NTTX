package io.kurumi.ntt.fragment.twitter.track;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.user.Img;
import java.awt.Color;
import org.jfree.data.category.DefaultCategoryDataset;
import com.pengrad.telegrambot.request.SendPhoto;

public class FollowersChart extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("chart");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		Img img = new Img(1000,800,Color.WHITE);
		
		DefaultCategoryDataset data = new DefaultCategoryDataset();
		
		data.addValue(114,"Followers","9:00");
		
		data.addValue(514,"Followers","10:00");
		
		data.addValue(1919,"Followers","11:00");
		
		data.addValue(810,"Followers","12:00");
		
		img.drawLineChart("Twitter Chart","","",data);
		
		bot().execute(new SendPhoto(msg.chatId(),img.getBytes()));
		
	}
	
	
}
