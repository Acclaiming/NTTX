package io.kurumi.nttools.spam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.nttools.fragments.Fragment;
import java.io.File;
import java.util.LinkedList;

public class SpamVote extends JSONObject {

    private File voteFile;

    public String id;

    public String listId;

    public Long origin;

    public Long twitterAccountId;
    public String twitterScreenName;
    public String twitterDisplyName;

    public String spamCause;

    public Integer vote_message_id;

    public Long startTime;

    public LinkedList<Long> agree = new LinkedList<>();
    public LinkedList<Long> disagree = new LinkedList<>();

    public SpamVote(Fragment fragment, String voteId) {

        id = voteId;
        
        voteFile = new File(fragment.main.dataDir, "twitter_spam_vote/" + id + ".json");

        load();

    }

    public void load() {

        try {

            JSONObject spam = new JSONObject(FileUtil.readUtf8String(voteFile));

            putAll(spam);

        } catch (Exception e) {}

        listId = getStr("list_id");
        
        twitterAccountId = getLong("twitter_account_id");

        twitterScreenName = getStr("twitter_screen_name");
        twitterDisplyName = getStr("twitter_disply_name");

        spamCause = getStr("spam_cause");

        vote_message_id = getInt("vote_message_id");

        startTime = getLong("start_time");

        JSONArray agreeArray = getJSONArray("agree");

        agree.clear();

        if (agreeArray != null) {

            for (int index = 0;index < agreeArray.size();index ++) {

                agree.add(agreeArray.getLong(index));

            }

        }

        JSONArray disagreeArray = getJSONArray("disagree");

        disagree.clear();

        if (disagreeArray != null) {

            for (int index = 0;index < agreeArray.size();index ++) {

                disagreeArray.add(disagreeArray.getLong(index));

            }

        }



    }

    public void save() {

        put("origin", origin);
        
        put("list_id",listId);
        
        put("twitter_account_id", twitterAccountId);
        put("twitter_screen_name", twitterScreenName);
        put("twitter_disply_name", twitterDisplyName);
        put("spam_cause", spamCause);
        put("vote_message_id", vote_message_id);
        
        put("start_time",startTime);
        
        JSONArray agreeArray = new JSONArray();
        
        for (Long id : agree) {
            
            agreeArray.add(id);
            
        }
        
        put("agree",agreeArray);
        
        JSONArray disagreeArray = new JSONArray();

        for (Long id : disagree) {

            disagreeArray.add(id);

        }

        put("disagree",disagreeArray);
        
        FileUtil.writeUtf8String(toStringPretty(),voteFile);

    }
    
    public void delete() {
        
        FileUtil.del(voteFile);
        
    }

    public static final String nextId(Fragment fragment) {

        Long id = fragment.main.data.getByPath("count.twitter_spam_list",Long.class);

        if (id == null) id = 50L;

        id ++;

        fragment.main.data.putByPath("count.twitter_spam_list",id);

        return id.toString();
    }


}
