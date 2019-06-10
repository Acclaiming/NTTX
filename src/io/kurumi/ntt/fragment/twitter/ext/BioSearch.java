package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HtmlUtil;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.CountOptions;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

public class BioSearch extends Function {

    public static BioSearch INSTANCE = new BioSearch();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("bio");

    }

    @Override
    public int target() {

        return Private;

    }


    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        String query = ArrayUtil.join(msg.params(),"\n");

        if (query.isEmpty()) {

            msg.send("请输入查询内容 (").exec();

            return;

        }

        FindIterable<UserArchive> result = null;



        long count;

        try {

            count = UserArchive.data.collection.countDocuments(regex("bio",query),new CountOptions().maxTime(500,TimeUnit.MILLISECONDS));

        } catch (MongoExecutionTimeoutException ex) {

            msg.send("bad request.jpg").exec();

            return;

        }

        if (count > 0) {

            result = UserArchive.data.collection.find(regex("bio",query));

        }

        if (count == 0) {

            msg.send("没有结果 (´◉.◉)").exec();

            return;

        }

        msg.send("结果数量 : " + (count > 100L ? count + "条 (仅显示100条)" : count + " 条"),"",format(result.limit(100),query)).html().exec();

    }

    String format(FindIterable<UserArchive> result,String query)  {

        StringBuilder page = new StringBuilder();

        for (UserArchive archive : result) {

            page.append(archive.urlHtml());

            page.append(" :").append("\n\n");

            page.append(HtmlUtil.escape(archive.bio));

            page.append("\n\n---------------------------------------\n\n");


        }


        return page.toString();

    }

}
