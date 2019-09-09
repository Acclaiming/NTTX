package io.kurumi.ntt.fragment.spam;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.Html;

public class GroupReport extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("report");

    }

    final String POINT_REPORT = "report_continue";
    final String POINT_REASON = "report_reason";

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (msg.isGroup()) {

            if (msg.replyTo() == null) {

                msg.reply("请对消息回复").failedWith();

                return;

            }

            Msg replyTo = msg.replyTo();

            if (replyTo.from().id.equals(user.id)) {

                msg.send("你不能举报你自己").failed();

                return;

            }

            if (replyTo.message().from().isBot()) {

                msg.send("暂不支持举报机器人").failedWith();

                return;

            }

            if (!msg.contactable()) {

                msg.send("机器人不能给您发送消息 :(").failedWith();

            } else {

                msg.delete();

                Msg target = replyTo.forwardTo(user.id);

                ButtonMarkup buttons = new ButtonMarkup();

                buttons.newButtonLine("垃圾广告", POINT_REASON, 0, target.messageId(), replyTo.from().id);
                buttons.newButtonLine("恶意消息", POINT_REASON, 0, target.messageId(), replyTo.from().id);

                target.reply("你正在举报 " + replyTo.from().userName() + " 的这条消息，请选择 " + Html.b("或直接发送举报原因") + " : ").buttons(buttons).html().async();

            }

        } else {


        }

    }

    class ReportData extends PointData {


    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (POINT_REPORT.equals(point)) {

            long userId = NumberUtil.parseLong(params[0]);

            long target = NumberUtil.parseLong(params[1]);

            int messageId = NumberUtil.parseInt(params[2]);

            if (!user.id.equals(userId)) {

                callback.alert("这不是你发起的投票 :(");

                return;

            }

        }

    }

}
