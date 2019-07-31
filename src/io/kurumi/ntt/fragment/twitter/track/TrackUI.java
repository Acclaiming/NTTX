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

    public static Data<TrackSetting> data = new Data<TrackSetting>(TrackSetting.class);

    final String POINT_SETTING_FOLLOWERS = "tr|f";
    final String POINT_SETTING_FOLLOWERS_INFO = "tr|o";
    final String POINT_SETTING_FOLLOWINGS_INFO = "tr|r";
    final String POINT_SETTING_HIDE_ME = "tr|h";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("track");

        registerCallback(
                POINT_SETTING_FOLLOWERS,
                POINT_SETTING_FOLLOWINGS_INFO,
                POINT_SETTING_FOLLOWERS_INFO,
                POINT_SETTING_HIDE_ME);

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (user.blocked()) {

            msg.send("你不能这么做 (为什么？)").async();

            return;

        }

        requestTwitter(user, msg);

    }


    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, final TAuth account) {

        final TrackSetting setting = this.data.containsId(account.id) ? this.data.getById(account.id) : new TrackSetting();

        msg.send("一个监听的设置... (做成按钮UI了 (❁´▽`❁)").buttons(makeSettings(setting, account.id)).exec();

    }

    ButtonMarkup makeSettings(final TrackSetting setting, final long accountId) {

        return new ButtonMarkup() {{

            newButtonLine((setting.followers ? "「 关闭" : "「 开启") + " 关注者监听 」", POINT_SETTING_FOLLOWERS, accountId);
            newButtonLine((setting.followingInfo ? "「 关闭" : "「 开启") + " 账号更改监听 (关注中) 」", POINT_SETTING_FOLLOWINGS_INFO, accountId);
            newButtonLine((setting.followersInfo ? "「 关闭" : "「 开启") + " 账号更改监听 (关注者) 」", POINT_SETTING_FOLLOWERS_INFO, accountId);
            newButtonLine((setting.hideChange ? "「 显示" : "「 隐藏") + " 账号更改 (对其他用户) 」", POINT_SETTING_HIDE_ME, accountId);


        }};

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        long accountId = Long.parseLong(params[0]);

        final TrackSetting setting = this.data.containsId(accountId) ? this.data.getById(accountId) : new TrackSetting();

        setting.id = accountId;

        boolean target = true;

        switch (point) {

            case POINT_SETTING_FOLLOWERS:
                target = setting.followers = !setting.followers;
                break;
            case POINT_SETTING_FOLLOWINGS_INFO:
                target = setting.followingInfo = !setting.followingInfo;
                break;
            case POINT_SETTING_FOLLOWERS_INFO:
                target = setting.followersInfo = !setting.followersInfo;
                break;
            case POINT_SETTING_HIDE_ME:
                target = setting.hideChange = !setting.hideChange;
                break;

        }

        if (setting.followers || setting.followingInfo || setting.followersInfo) {

            this.data.setById(accountId, setting);

        } else {

            this.data.deleteById(accountId);

        }

        callback.text("已" + (target ? "开启" : "关闭") + " ~");
        callback.editMarkup(makeSettings(setting, accountId));


    }

    public static class TrackSetting {

        public long id;

        public boolean followers = false;
        public boolean followingInfo = false;
        public boolean followersInfo = false;

        public boolean hideChange = false;

    }

}
