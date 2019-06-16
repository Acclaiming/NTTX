package io.kurumi.ntt.fragment.abs;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;

import java.util.LinkedList;

public abstract class Function extends Fragment {

    public static final String[] None = new String[0];
    public static final int All = 1;
    public static final int Private = 2;
    public static final int PrivateOnly = 3;
    public static final int Group = 4;

    private LinkedList<String> functions = new LinkedList<String>() {{
			functions(this);
		}};

    private LinkedList<String> points = new LinkedList<String>() {{
			points(this);
		}};

    public abstract void functions(LinkedList<String> names);

    public int target() {

        return All;

    }

    public void points(LinkedList<String> points) {
    }

    public abstract void onFunction(UserData user, Msg msg, String function, String[] params);

    public void onPoint(UserData user, Msg msg, PointStore.Point point) {
    }

    public void onCallback(UserData user, Callback callback, String point, String[] params) {
    }

	public boolean async() {

		return true;

	}

	@Override
	public int checkMsg(UserData user, Msg msg) {

		if (!msg.isCommand()) return 0;

        if (!functions.contains(msg.command())) return 0;

		return async() ? 1 : -1;

	}

	@Override
	public int checkPointedMsg(UserData user, Msg msg) {

		PointStore.Point point = point().get(user);

        switch (target()) {
			
            case Group:
                if (msg.isPrivate()) return 0;
                break;
            case Private: case PrivateOnly :
                if (msg.isGroup()) return 0;
                break;

        }

		if (!points.contains(point.point)) return 0;

		return  async() ? 1 : -1;


	}

	@Override
	public void onAsyncMsg(UserData user, Msg msg, int checked) {

		sendTyping(msg.chatId());

        if (target() == Group && !msg.isGroup()) {

            msg.send("请在群组使用 (˚☐˚! )/").exec();

            return;

        }

        if ((target() == Private && !msg.isPrivate()) && !(this instanceof TwitterFunction)) {

            if (!user.contactable()) {

                msg.send("请使用私聊 (˚☐˚! )/").publicFailed();

                return;

            } else {

                msg.send("咱已经在私聊回复了你。", "如果BOT有删除信息权限,命令和此回复将被自动删除。:)").failedWith();

                msg.targetChatId = user.id;

            }

        }

        msg.sendTyping();

        onFunction(user, msg, msg.command(), msg.params());

        return;

	}

    @Override
    public boolean onMsg(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        if (!functions.contains(msg.command())) return false;

        sendTyping(msg.chatId());

        if (target() == Group && !msg.isGroup()) {

            msg.send("请在群组使用 (˚☐˚! )/").exec();

            return true;

        }

        if ((target() == Private && !msg.isPrivate()) && !(this instanceof TwitterFunction)) {

            if (!user.contactable()) {

                msg.send("请使用私聊 (˚☐˚! )/").publicFailed();

                return true;

            } else {

                msg.send("咱已经在私聊回复了你。", "如果BOT有删除信息权限,命令和此回复将被自动删除。:)").failedWith();

                msg.targetChatId = user.id;

            }

        }

        msg.sendTyping();

        onFunction(user, msg, msg.command(), msg.params());

        return true;

    }


    @Override
    public boolean onPointedMsg(UserData user, Msg msg) {

        PointStore.Point point = point().get(user);

        switch (target()) {

            case Group:
                if (msg.isPrivate()) return false;
                break;
            case Private:
                if (msg.isGroup()) return false;
                break;

        }

		if (points.contains(point.point)) {

			onPoint(user, msg, point);

			return true;

		}
		
        return false;

    }

	@Override
	public void onAsyncPointedMsg(UserData user, Msg msg, int checked) {

		onPoint(user, msg, point().get(user));

	}

    @Override
    public boolean onCallback(UserData user, Callback callback) {

        for (String used : points) {

            if (used.equals(callback.params[0])) {

                onCallback(user, callback, callback.params[0], ArrayUtil.remove(callback.params, 0));

                return true;

            }

        }

        return false;
    }

}
