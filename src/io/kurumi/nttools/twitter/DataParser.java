package io.kurumi.nttools.twitter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.Markdown;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import twitter4j.ResponseList;
import twitter4j.User;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.nttools.fragments.Fragment;

public class DataParser {

    private static Document doc;

    public static void parse(UserData user, Msg msg) {

        doc = msg.message().document();

        if (doc == null) return;

        switch (doc.fileName()) {

                case "following.js" : processAccounts(user, msg, true);return;
                case "follower.js" : processAccounts(user, msg, false);return;


        }

    }

    public static void processAccounts(UserData user, Msg msg, boolean friend) {

        LinkedList<TwiAccount> accounts = user.getTwitterAccounts();
        
        if(accounts.isEmpty()) {
            
            msg.send("还没有认证账号 无法调用接口 >_<").exec();
            
            return;
            
        }
        
        Twitter api = accounts.getFirst().createApi();

        File doc = msg.getFile();
        
        File result = new File(msg.fragment.main.dataDir, "/cache/twittr_data_parse/follow_friends/" + msg.message().document().fileId() + ".html");

        if (result.isFile()) {
         
            msg.send("这份结果已经被分析过啦 (｡>∀<｡) :");
            
            msg.sendUpdatingFile();
            
            msg.sendFile(result);
            
            return;

        }
        
        JSONArray json = new JSONArray(StrUtil.subAfter(FileUtil.readUtf8String(doc), " = ", false));
        
        StringBuilder page = new StringBuilder();

        LinkedList<Long> showCache = new LinkedList<>();

        int index = 1;

        try {
            
            for (JSONObject obj : (List<JSONObject>)(Object)json) {

                long id;

                if (friend) {

                    id = obj.getJSONObject("following").getLong("accountId");
                } else {

                    id = obj.getJSONObject("follower").getLong("accountId");

                }

                if (showCache.size() < 100) {

                    // lookUpUsers 上限 100

                    showCache.add(id);

                } else {

                    long[] ids = ArrayUtil.unWrap(showCache.toArray(new Long[showCache.size()]));

                    showCache.clear();

                    ResponseList<User> users = api.lookupUsers(ids);

                    for (User u : users) {

                        page.append("「").append(index).append("」 ");
                        page.append(TApi.formatUserNameMarkdown(u));
                        page.append("  \n");

                        index ++;

                    }

                }

            }

            if (showCache.size() != 0) {

                long[] ids = ArrayUtil.unWrap(showCache.toArray(new Long[showCache.size()]));

                showCache.clear();

                ResponseList<User> users = api.lookupUsers(ids);

                for (User u : users) {

                    page.append("「").append(index).append("」 ");
                    page.append(TApi.formatUserNameMarkdown(u));
                    page.append("  \n");

                    index ++;

                }


            }
            String html = Markdown.parsePage("所有 " + (friend ? "关注的人" : "关注者"), "# 这是你的结果 (｡>∀<｡)\n" + page.toString());

            FileUtil.writeUtf8String(html, result);

            msg.send("分析成功 (｡>∀<｡) :").exec();
            
            msg.sendUpdatingFile();
            
            msg.sendFile(result);
            
        } catch (TwitterException e) {}
        
    }

}
