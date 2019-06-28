package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.BotFragment;

public class Firewall extends Fragment {

    public static Data<Id> block = new Data<Id>("UserBlock",Id.class);

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("accept","drop");
		registerAdminPayload("accept","drop");

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (params.length == 0) {

            msg.send("invlid params").exec();

            return;

        }

        long target = -1;

        if (NumberUtil.isNumber(params[0])) {

            target = NumberUtil.parseLong(params[0]);

        } else {

            UserData.data.getByField("userName",params[0]);

        }

        if (target == -1) {

            msg.send("无记录").exec();

            return;

        }

        boolean exists = block.containsId(target);

        if ("accept".equals(function)) {

            if (exists) {

                block.deleteById(target);

                msg.send("removed block").exec();

            } else {

                msg.send("not blocked").exec();

            }

        } else {

            if (exists) {

                msg.send("already blocked").exec();

            } else {

                block.setById(target,new Id(target));

                msg.send("blocked").exec();

            }

        }

    }

	@Override
	public void onPayload(UserData user,Msg msg,String payload,String[] params) {

		if ("accept".equals(payload) || "drop".equals(payload)) {

			if (params.length < 1) {

				msg.send("invlid params").exec();

				return;

			}

			UserData target = UserData.get(NumberUtil.parseLong(params[0]));

			if (target.developer()) {

				msg.send("不可以...！").exec();

				return;

			}

			boolean exists = block.containsId(target.id);

			if ("accept".equals(payload)) {

				if (exists) {

					block.deleteById(target.id);

					msg.send("removed block").exec();

				} else {

					msg.send("not blocked").exec();

				}

			} else {

				if (exists) {

					msg.send("already blocked").exec();

				} else {

					block.setById(target.id,new Id(target.id));

					msg.send("blocked").exec();

				}

			}

			return;


		}

    }
	
    public static class Id {

        public long id;

        public Id() {
        }

        public Id(long id) {
            this.id = id;
        }

    }

}
