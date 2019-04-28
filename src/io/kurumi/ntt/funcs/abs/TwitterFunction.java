package io.kurumi.ntt.funcs.abs;

import com.mongodb.client.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.twitter.login.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import java.util.LinkedList;
import com.pengrad.telegrambot.response.SendResponse;

public abstract class TwitterFunction extends Function {

    public static final String POINT_CHOOSE_ACCPUNT = "t|s";

    public static class TwitterPoint {

        public TwitterFunction function;
        public Msg msg;
        public Msg send;

        public TwitterPoint(TwitterFunction function,Msg msg,Msg send) {

            this.function = function;
            this.msg = msg;
            this.send = send;

        }

    }

    public abstract void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account);

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (!TAuth.contains(user.id)) {

            msg.send("这个功能需要授权 Twitter账号 才能使用 (❁´▽`❁)","使用 /login 认证账号 ~").exec();

            return;

        } else {

            if (TAuth.data.countByField("user",user.id) == 1) {

                onFunction(user,msg,function,msg.params(),TAuth.getByUser(user.id).first());

                return;

            }
            
            final FindIterable<TAuth> accounts = TAuth.getByUser(user.id);

            
            Msg send = msg.send("请选择目标账号 Σ( ﾟωﾟ (使用 /cancel 取消) ~").keyboard(new Keyboard() {{

                        for (TAuth account : accounts) {

                            newButtonLine("@" + account.archive().screenName);

                        }

                        newButtonLine("/cancel");

                    }}).send();
                    
                    
            setPoint(user,POINT_CHOOSE_ACCPUNT,new TwitterPoint(this,send,msg));
            

        }

    }

    @Override
    public void points(LinkedList<String> points) {
        
        points.add(POINT_CHOOSE_ACCPUNT);
        
    }

    @Override
    public int target() {
       
        return Private;
        
    }
    
    @Override
    public void onPoint(UserData user,Msg msg,PointStore.Point point) {
        
        if (POINT_CHOOSE_ACCPUNT.equals(point.point)) {

            TwitterPoint data = (TwitterPoint)point.data;

            if (!msg.text().startsWith("@")) {

                msg.send("请选择 Twitter 账号 (˚☐˚! )/").exec();

                return;

            }

            String screenName = msg.text().substring(1);

            TAuth account = TAuth.getById(UserArchive.get(screenName).id);

            if (account == null) {

                msg.send("找不到这个账号 (？) 请重新选择 ((*゜Д゜)ゞ").exec();

                return;

            }
            
            data.send.delete();
            
            msg.send("选择了 : " + account.archive().urlHtml() + " (❁´▽`❁)").removeKeyboard().html().failedWith();
           
            clearPoint(user);
            
            data.function.onFunction(user,data.msg,data.msg.command(),data.msg.params(),account);
            

        }

    }



}
