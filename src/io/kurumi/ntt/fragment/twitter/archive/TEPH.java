package io.kurumi.ntt.fragment.twitter.archive;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.LongArrayData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.group.JoinCaptcha;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;

public class TEPH extends Fragment {

    public static LongArrayData data = new LongArrayData(TEPH.class);

    public static boolean needPuhlish(UserArchive archive) {

        if (data.containsId(archive.id)) return false;

        if (archive.name.contains("\u200F") || (archive.bio != null && archive.bio.contains("\u200F"))) return false;

        if (ReUtil.contains(JoinCaptcha.arabicCharacter, archive.name)) return false;

        return archive.bio == null || !ReUtil.contains(JoinCaptcha.arabicCharacter, archive.bio);

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("teph", "teps");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length < 2) {

            msg.invalidParams("user", "reason").async();

            return;

        }

        UserArchive archive;

        if (NumberUtil.isNumber(params[0])) {

            archive = UserArchive.get(NumberUtil.parseLong(params[0]));

        } else {

            archive = UserArchive.get(NTT.parseScreenName(params[0]));

        }

        if (archive == null) {

            msg.send("找不到用户").async();

            return;

        }

        String reason = ArrayUtil.join(ArrayUtil.remove(msg.params(), 0), " ");

        if (function.endsWith("h")) {

            if (!data.add(archive.id)) {

                msg.send("已经在列表中").async();

            } else {

                new Send(Env.TEP_CHANNEL, "#通知 #隐藏更改\n\n现在开始隐藏对 " + archive.urlHtml() + " ( #" + archive.screenName + " ) 的公开通知。", "\n原因是 : " + reason).html().async();

            }

        } else {

            if (!data.deleteById(archive.id)) {

                msg.send("不在列表中").async();

            } else {

                new Send(Env.TEP_CHANNEL, "#通知 #消失更改\n\n现在开始显示对 " + archive.urlHtml() + " ( #" + archive.screenName + " ) 的公开通知。", "\n原因是 : " + reason).html().async();

            }

        }

    }

}
