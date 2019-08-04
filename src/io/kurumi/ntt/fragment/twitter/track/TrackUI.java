package io.kurumi.ntt.fragment.twitter.track;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class TrackUI extends Fragment {

	public static final String POINT_TRACK = "twi_track";
	
    final String POINT_SETTING_FOLLOWERS = "twi_fo";
    final String POINT_SETTING_FOLLOWERS_INFO = "twi_fo_info";
    final String POINT_SETTING_FOLLOWINGS_INFO = "twi_fr_info";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(
		        POINT_TRACK,
                POINT_SETTING_FOLLOWERS,
                POINT_SETTING_FOLLOWINGS_INFO,
                POINT_SETTING_FOLLOWERS_INFO);
				
    }

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
		
		
	}

}
