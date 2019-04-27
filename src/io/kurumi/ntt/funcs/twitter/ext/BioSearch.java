package io.kurumi.ntt.funcs.twitter.ext;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import cn.hutool.core.util.*;
import com.mongodb.client.*;
import io.kurumi.ntt.twitter.archive.*;
import java.util.*;
import cn.hutool.http.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.funcs.abs.*;
import com.mongodb.client.model.CountOptions;
import java.util.concurrent.TimeUnit;

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

        long count = UserArchive.data.collection.countDocuments(regex("bio",query),new CountOptions().maxTime(500,TimeUnit.MILLISECONDS));

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
