package io.kurumi.nttools.fragments;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.User;
import io.kurumi.nttools.spam.SpamList;
import io.kurumi.nttools.spam.SpamVote;
import io.kurumi.nttools.timer.TimerThread;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import io.kurumi.nttools.spam.TwitterSpam;
import cn.hutool.core.util.ArrayUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MainFragment extends Fragment {

    public ExecutorService threadPool; {
        
        threadPool = Executors.newFixedThreadPool(9);
        
    }
    
    
    public int serverPort = -1;
    public String serverDomain;
    public File dataDir;
    
    public TimerThread timer = new TimerThread(this,1 * 60 * 1000) {{ start(); }};
    
    public TwitterSpam spam = new TwitterSpam(this);
    
    public JSONObject data;
    
    private HashMap<Long,UserData> userDataCache = new HashMap<>();

    public LinkedList<UserData> getUsers() {

        return new LinkedList<UserData>(userDataCache.values());

    }

    public UserData findUserData(String userName) {

        for (UserData user : userDataCache.values()) {

            if (userName.equals(user.userName)) return user;

        }

        return null;

    }


    public UserData getUserData(User user) {

        UserData ud = getUserData(user.id());

        ud.update(user);

        return ud;

    }

    public UserData getUserData(long id) {

        if (userDataCache.containsKey(id)) return userDataCache.get(id);

        UserData ud = new UserData(this, id);

        userDataCache.put(id, ud);

        return ud;

    }

    private HashMap<String,SpamList> spamListCahche = new HashMap<>();

    public SpamList getSpamList(String id) {

      return spamListCahche.get(id);
        
        

    }

    public SpamList deleteSpamList(String id) {

        SpamList list = spamListCahche.remove(id);

        list.delete();

        return list;

    }

    public LinkedList<SpamList> getSpamLists() {

        return new LinkedList<SpamList>(spamListCahche.values());

    }

    public SpamList newSpamList(String name) {

        SpamList list = new SpamList(this, SpamList.nextId(this));

        list.name = name;

        list.save();

        spamListCahche.put(list.id, list);

        return list;

    }
   
    private HashMap<String,SpamVote> spamVoteCahche = new HashMap<>();

    public SpamVote getSpamVote(String id) {

        return spamVoteCahche.get(id);

    }

    public SpamVote deleteSpamVote(String id) {

        SpamVote vote = spamVoteCahche.remove(id);
        
        vote.delete();

        return vote;

    }

    public LinkedList<SpamVote> getSpamVotes() {

        return new LinkedList<SpamVote>(spamVoteCahche.values());

    }

    public SpamVote newSpamVote(SpamList list,Long origin,Long accountId,String screenName,String displayName,String cause) {

        SpamVote vote = new SpamVote(this,SpamVote.nextId(this));
        
        vote.origin = origin;
        
        vote.listId = list.id;
        
        vote.twitterAccountId = accountId;
        
        vote.twitterScreenName = screenName;
        
        vote.twitterDisplyName = displayName;

        vote.spamCause = cause;
        
        vote.startTime = System.currentTimeMillis();
        
        vote.save();

        spamVoteCahche.put(vote.id,vote);

        return vote;

    }
    

    public MainFragment(File dataDir) {

        super(null);
        main = this;
        this.dataDir = dataDir;

        File[] ul = new File(main.dataDir, "/users").listFiles();

        if (ul != null) {

            for (File userDataFile : ul) {

                long userId = Long.parseLong(StrUtil.subBefore(userDataFile.getName(), ".json", true));

                if (userDataCache.containsKey(userId)) continue;

                userDataCache.put(userId, new UserData(main, userId));

            }

        }

        File[] sl = new File(main.dataDir, "/twitter_spam_list").listFiles();

        if (sl != null) {

            for (File userDataFile : sl) {

                String listId = StrUtil.subBefore(userDataFile.getName(), ".json", true);

                if (spamListCahche.containsKey(listId)) continue;

                spamListCahche.put(listId, new SpamList(main, listId));

            }

        }

        File[] vl = new File(main.dataDir, "/twitter_spam_vote").listFiles();

        if (vl != null) {

            for (File voteFile : vl) {

                String voteId = StrUtil.subBefore(voteFile.getName(), ".json", true);

                if (spamVoteCahche.containsKey(voteId)) continue;

                spamVoteCahche.put(voteId, new SpamVote(main, voteId));

            }

        }
        


        refresh();


    }

    @Override
    public String name() { return "main"; }

    public Map<String,String> tokens = new HashMap<>();;

    public void refresh() {

        try {

            JSONObject botData = new JSONObject(FileUtil.readUtf8String(new File(dataDir, "config.json")));

            tokens = (Map<String,String>)((Object)botData.getJSONObject("bot_token"));

            serverPort = botData.getInt("local_port", serverPort);
            serverDomain = botData.getStr("server_domain");

            data = botData.getJSONObject("data");
            
            if (data == null) data = new JSONObject();

        } catch (Exception e) {}

    }

    public void save() {

        JSONObject botData = new JSONObject();

        botData.put("bot_token", new JSONObject(tokens));

        botData.put("local_port", serverPort);
        botData.put("server_domain", serverDomain);
        
        botData.put("data",data);

        FileUtil.writeUtf8String(botData.toStringPretty(), new File(dataDir, "config.json"));

    } 

}
