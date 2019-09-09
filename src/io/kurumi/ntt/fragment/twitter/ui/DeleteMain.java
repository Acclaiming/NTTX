package io.kurumi.ntt.fragment.twitter.ui;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class DeleteMain extends Fragment {

    public static final String POINT_DELETE = "twi_ad";

    final String POINT_SETTING_AD_STATUS = "twi_ads";
    final String POINT_SETTING_AD_REPLY = "twi_adr";
    final String POINT_SETTING_AD_RETWEET = "twi_adrt";
    final String POINT_SETTING_AD_DELAY = "twi_add";
    final String POINT_SETTING_AD_ABS = "twi_abs";
    final String POINT_AD_EXECUTE = "twi_adex";

    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_DELETE, POINT_SETTING_AD_STATUS, POINT_SETTING_AD_REPLY, POINT_SETTING_AD_RETWEET, POINT_SETTING_AD_DELAY, POINT_SETTING_AD_ABS, POINT_AD_EXECUTE);

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

            callback.invalidQuery();

            return;

        }

        long accountId = NumberUtil.parseLong(params[0]);

        TAuth account = TAuth.getById(accountId);

        if (account == null) {

            callback.alert("无效的账号 .");

            callback.delete();

            return;

        }

        if (POINT_DELETE.equals(point)) {

            deleteMain(user, callback, account);

        } else if (POINT_SETTING_AD_DELAY.equals(point)) {

            setDelay(user, callback, "null".equals(params[1]) ? null : NumberUtil.parseInt(params[1]), account);

        } else if (POINT_AD_EXECUTE.equals(point)) {

            //deleteNow(user,callback,account);

        } else {

            setConfig(user, callback, point, account);

        }

    }

    void deleteMain(UserData user, Callback callback, TAuth account) {

        String message = "推文定时自动删除设置  : [ " + account.archive().name + " ]";

        ButtonMarkup config = new ButtonMarkup();

        config.newButtonLine()
                .newButton("推文")
                .newButton(account.ad_s != null ? "✅" : "☑", POINT_SETTING_AD_STATUS, account.id);

        config.newButtonLine()
                .newButton("回复")
                .newButton(account.ad_r != null ? "✅" : "☑", POINT_SETTING_AD_REPLY, account.id);

        config.newButtonLine()
                .newButton("转推")
                .newButton(account.ad_t != null ? "✅" : "☑", POINT_SETTING_AD_RETWEET, account.id);

        config.newButtonLine("推文删除间隔");

        config.newButtonLine()
                .newButton("保留一天")
                .newButton(account.ad_d == null ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, null);

        config.newButtonLine()
                .newButton("保留三天")
                .newButton(((Integer) 0).equals(account.ad_d) ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, 0);

        config.newButtonLine()
                .newButton("保留七天")
                .newButton(((Integer) 1).equals(account.ad_d) ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, 1);

        config.newButtonLine()
                .newButton("保留一月")
                .newButton(((Integer) 2).equals(account.ad_d) ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, 2);

        config.newButtonLine()
                .newButton("保留二月")
                .newButton(((Integer) 3).equals(account.ad_d) ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, 3);

        config.newButtonLine()
                .newButton("保留三月")
                .newButton(((Integer) 4).equals(account.ad_d) ? "●" : "○", POINT_SETTING_AD_DELAY, account.id, 4);

        config.newButtonLine()
                .newButton("使用绝对时间")
                .newButton(account.ad_a != null ? "✅" : "☑", POINT_SETTING_AD_ABS, account.id);

        // config.newButtonLine("立即执行",POINT_AD_EXECUTE,account.id);

        config.newButtonLine("🔙", AccountMain.POINT_ACCOUNT, account.id);

        callback.edit(message).buttons(config).async();

    }

    void setConfig(UserData user, Callback callback, String point, TAuth account) {

        if (POINT_SETTING_AD_STATUS.equals(point)) {

            if (account.ad_s == null) {

                account.ad_s = true;

                callback.text("✅ 已开启");

            } else {

                account.ad_s = null;

                callback.text("✅ 已关闭");

            }

        } else if (POINT_SETTING_AD_REPLY.equals(point)) {

            if (account.ad_r == null) {

                account.ad_r = true;

                callback.text("✅ 已开启");

            } else {

                account.ad_r = null;

                callback.text("✅ 已关闭");

            }

        } else if (POINT_SETTING_AD_RETWEET.equals(point)) {

            if (account.ad_t == null) {

                account.ad_t = true;

                callback.text("✅ 已开启");

            } else {

                account.ad_t = null;

                callback.text("✅ 已关闭");

            }

        } else if (POINT_SETTING_AD_ABS.equals(point)) {

            if (account.ad_a == null) {

                account.ad_a = true;

                callback.text("✅ 已开启");

            } else {

                account.ad_a = null;

                callback.text("✅ 已关闭");

            }

        }

        TAuth.data.setById(account.id, account);

        deleteMain(user, callback, account);

    }

    void setDelay(UserData user, Callback callback, Integer delay, TAuth account) {

        callback.confirm();

        account.ad_d = delay;

        TAuth.data.setById(account.id, account);

        deleteMain(user, callback, account);

    }

}
