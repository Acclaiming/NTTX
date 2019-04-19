package io.kurumi.ntt.funcs;

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


public class BioSearch extends Fragment {

    public static BioSearch INSTANCE = new BioSearch();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {

        switch (T.checkCommand(msg)) {

            case "bio" : searchBio(user,msg,false);break;
            case "bioregex" : searchBio(user,msg,true);break;
                
            default : return false;

        }

        return true;

    }

   // final String POINT_NEXT_PAGE = "b|n";

    void searchBio(UserData user,Msg msg,boolean useRegex) {

        String query = ArrayUtil.join(msg.params(),"\n");

        FindIterable<UserArchive> result = null;
        
        long count;

        if (!useRegex) {

            count = BotDB.userArchiveCollection.count(elemMatch("bio",text(query)));
            
            if (count > 0) {
            
            result = BotDB.userArchiveCollection.find(elemMatch("bio",text(query)));

            }
            
        } else {

            count = BotDB.userArchiveCollection.count(regex("bio",query));

            if (count > 0) {
            
            result = BotDB.userArchiveCollection.find(regex("bio",query));

            }
            
        }

        if (count == 0) {
            
            msg.send("没有结果 (´◉.◉)").exec();
            
            return;
            
        }
        
        msg.send("结果数量 : " + (count > 39L ? count + " (仅显示39条)" : ""),format(result.limit(39),query,useRegex)).exec();
    }

    String format(FindIterable<UserArchive> result,String query,boolean useRegex)  {

        StringBuilder page = new StringBuilder();

        for (UserArchive archive : result) {

            page.append(archive.urlHtml()).append(" :").append("\n\n");

            if (!useRegex) {

                int cursor = archive.bio.indexOf(query);

                int end = archive.bio.length() - cursor;

                if (cursor > 10) {

                    cursor = 10;

                }

                if (end - query.length() - cursor > 11) {

                    end = cursor + query.length() + 11;

                }

                page.append(HtmlUtil.escape(archive.bio.substring(cursor,end)));


            } else {

                List<String> match  = ReUtil.findAllGroup0(query,archive.bio);

                if (match.size() > 0) {

                    query = match.get(0);

                }

                int cursor = archive.bio.indexOf(query);

                int end = archive.bio.length() - cursor;

                if (cursor > 10) {

                    cursor = 10;

                }

                if (end - query.length() - cursor > 11) {

                    end = cursor + query.length() + 11;

                }

                page.append(HtmlUtil.escape(archive.bio.substring(cursor,end)));


            }

            page.append("---------------------------------------\n");

        }

        return page.toString();

    }

}
