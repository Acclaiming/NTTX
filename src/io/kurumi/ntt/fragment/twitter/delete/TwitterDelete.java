package io.kurumi.ntt.fragment.twitter.delete;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.BotLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import twitter4j.Twitter;
import twitter4j.TwitterException;

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

            if (!threads.containsKey(account.id)) {

                msg.send("你没有正在处理中的删除...").exec();

                return;

            }

            threads.remove(account.id).stoped.set(true);
            
            msg.send("已取消...").exec();


        } else if (threads.containsKey(account.id)) {

            msg.send("你有正在处理中的删除... 这需要大量时间处理... 取消使用 /canceldelete .").exec();

            return;

        } else {

            setPoint(user,POINT_DELETE,account);

            msg.send("这个功能需要从 Twitter应用/网页 - 设置 - 账号 - 你的Twitter数据 输入密码下载数据zip，并找到tweet.js/like.js的说").exec();

            msg.send("现在发送 tweet.js / like.js 来删除所有推文/打心... (如果文件超过20m 需要打zip包发送哦 (tweet.js打tweet.zip / like.js打like.zip (❁´▽`❁)","使用 /cancel 取消 ~").exec();

        }
       
    }

    @Override
    public void onPoint(UserData user,Msg msg,PointStore.Point point) {

        super.onPoint(user,msg,point);

        if (!POINT_DELETE.equals(point.point)) return;

        if (msg.doc() == null || !msg.doc().fileName().matches("(tweet|like)\\.(js|zip)")) {

            msg.send("你正在删除twetter数据... 发送tweet.js删除推文 like.js 删除打心...","使用 /cancel 取消...").exec();

            return;

        }

        boolean like = msg.doc().fileName().startsWith("like");

        File file = msg.file();

        if (file == null) {

            if (msg.doc().fileName().endsWith(".js")) {

                msg.send("文件太大... Telegram禁止Bot下载超过20mb的文件... 请打zip包并修改为 tweet.zip / like.zip （●＾o＾●）").exec();

            } else {

                msg.send("可能真的太大...？ zip都超过20m...").exec();

            }
            
            return;


        }

        File zipOut = new File(Env.CACHE_DIR,"unzip/" + msg.doc().fileId());

        if (!zipOut.isDirectory() || zipOut.list().length == 0) {

            if (msg.doc().fileName().endsWith(".zip")) {

                try {

                    ZipUtil.unzip(file,zipOut);

                    file = new File(zipOut,like ? "like.js" : "tweet.js");

                    if (!file.isFile()) {

                        msg.send("这个压缩包里面有文件吗？ 注意 : tweet.js 需要打成 tweet.zip / like.js 需要打成 like.zip 否则无法识别呢 (˚☐˚! )/").exec();

                        return;

                    }

                } catch (Exception ex) {

                    msg.send("解压zip包失败... 你是打了rar包改后缀了吗？(˚☐˚! )/").exec();

                    return;

                }

            }

        }


        msg.send("解析" + (like ? "打心" : "推文") + "ing... Σ( ﾟωﾟ").exec();

        clearPoint(user);
        
        BufferedReader reader =  IoUtil.getReader(IoUtil.toStream(file),CharsetUtil.CHARSET_UTF_8);

        LinkedList<Long> ids = new LinkedList<>();

        try {

            String line = reader.readLine();

            boolean isRC = false;

            while (line != null) {

                if (like) {

                    if (line.contains("tweetId")) {

                        ids.add(Long.parseLong(StrUtil.subBetween(line,"\" : \"","\"")));

                    }

                } else if (isRC) {

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

        msg.send("已解析 " + ids.size() + " 条" + (like ? "打心" : "推文") + "... 正在启动删除线程...").exec();

        DeleteThread thread = new DeleteThread();

        thread.userId = user.id;

        thread.account = (TAuth)point.data;

        thread.api = thread.account.createApi();

        thread.ids = ids;

        thread.tweet = !like;

        threads.put(thread.account.id,thread);

        thread.start();


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

                progress = Math.round((current /count) * 1000) / 10;

                if (progress != last) {

                    status.edit("删除中 : " + progress + "%","取消删除使用 /canceldelete ...").exec();

                }

                if (progress == 100) {

                    status.edit("删除完成...").exec();

                }

            }

            threads.remove(account.id);


        }


    }


}
