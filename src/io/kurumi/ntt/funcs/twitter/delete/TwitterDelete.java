package io.kurumi.ntt.funcs.twitter.delete;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.TwitterFunction;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import java.util.concurrent.atomic.AtomicBoolean;
import io.kurumi.ntt.utils.BotLog;

public class TwitterDelete extends TwitterFunction {

    public static TwitterDelete INSTANCE = new TwitterDelete();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("delete");
        names.add("canceldelete");

    }

    final String POINT_DELETE = "td";

    @Override
    public void points(LinkedList<String> points) {

        super.points(points);

        points.add(POINT_DELETE);

    }

    @Override
    public int target() {

        return Private;

    }

    HashMap<Long,DeleteThread> threads = new HashMap<>();

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        if (function.startsWith("cancel")) {

            if (!threads.containsKey(user.id)) {

                msg.send("你没有正在处理中的删除...").exec();

                return;

            }

            threads.get(user.id).stoped.set(true);

            msg.send("已取消...").exec();


        } else if (threads.containsKey(user.id)) {

            msg.send("你有正在处理中的删除... 这需要大量时间处理... 取消使用 /canceldelete .").exec();

            return;

        } else {

            setPoint(user,POINT_DELETE,account);

            msg.send("这个功能需要从 Twitter应用/网页 - 设置 - 账号 - 你的Twitter数据 输入密码下载数据zip，并找到tweet.js/like.js的说").exec();
            
            msg.send("现在发送 tweet.js / like.js 来删除所有推文/打心...").exec();

        }

    }

    @Override
    public void onPoint(UserData user,Msg msg,PointStore.Point point) {

        if (msg.doc() == null) {

            msg.send("你正在删除twetter数据... 发送tweet.js删除推文 like.js 删除打心...","使用 /cancel 取消...").exec();

            return;

        } if (msg.doc().fileName().equals("tweet.js")) {

            msg.send("读取推文ing...").exec();

            clearPoint(user);

            BufferedReader reader =  IoUtil.getReader(IoUtil.toStream(msg.file()),CharsetUtil.CHARSET_UTF_8);

            LinkedList<Long> ids = new LinkedList<>();

            try {

                String line = reader.readLine();

                boolean isRC = false;

                while (line != null) {

                    if (isRC) {

                        ids.add(Long.parseLong(StrUtil.subBetween(line,"\" : \"","\"")));

                        isRC = false;

                    } else if (line.contains("retweet_count")) {

                        isRC = true;

                    }

                    line = reader.readLine();

                }

                reader.close();

            } catch (IOException e) {

                msg.send("读取文件错误...",e.toString()).exec();

                return;

            }

            msg.send("已解析 " + ids.size() + " 条推文... 正在启动删除线程...").exec();

            DeleteThread thread = new DeleteThread();

            thread.userId = user.id;

            thread.account = (TAuth)point.data;

            thread.api = thread.account.createApi();

            thread.ids = ids;

            thread.tweet = true;

            threads.put(user.id,thread);

            thread.start();

        } else if (msg.doc().fileName().equals("like.js")) {

            msg.send("读取打心ing...").exec();

            clearPoint(user);

            BufferedReader reader =  IoUtil.getReader(IoUtil.toStream(msg.file()),CharsetUtil.CHARSET_UTF_8);

            LinkedList<Long> ids = new LinkedList<>();

            try {

                String line = reader.readLine();

                while (line != null) {

                    if (line.contains("tweetId")) {

                        ids.add(Long.parseLong(StrUtil.subBetween(line,"\" : \"","\"")));

                    }

                    line = reader.readLine();

                }

                reader.close();

            } catch (IOException e) {

                msg.send("读取文件错误...",e.toString()).exec();

                return;

            }

            msg.send("已解析 " + ids.size() + " 条打心... 正在启动删除线程...").exec();

            DeleteThread thread = new DeleteThread();

            thread.userId = user.id;

            thread.account = (TAuth)point.data;

            thread.api = thread.account.createApi();

            thread.ids = ids;

            thread.tweet = false;

            threads.put(user.id,thread);

            thread.start();


        }

    }

    class DeleteThread extends Thread {

        long userId;
        TAuth account;
        Twitter api;
        LinkedList<Long> ids;
        boolean tweet;

        AtomicBoolean stoped = new AtomicBoolean(false);

        @Override
        public void run() {

            Msg status =  new Send(userId,"正在开始删除... ").send();

            float count = ids.size();
            float current = 0;

            float progress = 0;

            for (Long id : ids) {

                float last = progress;

                try {

                    if (tweet) {

                        api.destroyStatus(id);

                    } else {

                        api.destroyFavorite(id);

                    }


                } catch (TwitterException e) {

                    BotLog.info("删除，错误",e);

                }

                if (stoped.get()) {

                    status.edit("已手动取消...").exec();

                    break;

                }

                current ++;

                progress = Math.round(progress * 100);

                if (progress != last) {

                    status.edit("删除中 : " + progress + "%","取消删除使用 /canceldelete ...").exec();

                }

                if (progress == 1) {

                    status.edit("删除完成...").exec();

                }

            }

            threads.remove(userId);

        }



    }

}
