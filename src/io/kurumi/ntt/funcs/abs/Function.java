package io.kurumi.ntt.funcs.abs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.db.PointStore.*;

public abstract class Function extends Fragment {

    public abstract String name();

    public int target() {

        return All;

    }

    public String description() {

        return null;

    }

    public String[] points() {

        return None;

    }

    public String[] params() {

        return None;

    }

    public Boolean isVarargs() {

        return false;

    }

    public abstract void onFunction(UserData user,Msg msg,String[] params);
    public void onPoint(UserData user,Msg msg,PointStore.Point point) {}

    public static final String[] None = new String[0];

    public static final int All = 1;
    public static final int Private = 2;
    public static final int Group = 3;


    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        if (!name().equals(msg.command())) return false;

        if (target() == Group && msg.isPrivate())  {

            msg.send("请在群组使用 (˚☐˚! )/").exec();

            return true;

        }

        if (target() == Private && !msg.isPrivate())  {

            msg.send("请使用私聊 (˚☐˚! )/").exec();

            return true;

        }

        if (!isVarargs() && params().length != msg.params().length) {

            if (params().length == 0) {

                msg.send("( /" + name() + " ) 没有参数 !").exec();

            } else {

                msg.send("/" + name() + ArrayUtil.join(params()," "),description()).exec();

            } 

            return true;

        }

        msg.sendTyping();
        
        onFunction(user,msg,msg.params());

        return true;

    }

    @Override
    public boolean onPointedMsg(UserData user,Msg msg) {

        PointStore.Point point = point().get(user);
        
        switch (target()) {

                case Group : if (msg.isPrivate()) return false;break;
                case Private : if (msg.isGroup()) return false;break;

        }
        

        for (String used : points()) {

            if (used.equals(point.point)) {

                onPoint(user,msg,point);

                return true;

            }

        }

        return false;

    }

}
