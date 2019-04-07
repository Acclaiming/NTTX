package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.T;
import io.kurumi.ntt.twitter.TApi;
import io.kurumi.ntt.twitter.TAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import java.util.LinkedList;
import io.kurumi.ntt.twitter.archive.UserArchive;

public class AnalysisJsp extends Fragment {

    public static AnalysisJsp INSTANCE = new AnalysisJsp();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if ("jsp".equals(msg.command())) {

            analysis(user,msg);

            return true;

        }

        return false;

    }

    LinkedList<Long> fos;
    LinkedList<Long> frs;

    void analysis(UserData user,Msg msg) {

        if (T.checkUserNonAuth(user,msg)) return;

        Twitter api = TAuth.get(user).createApi();

        try {

            if (fos == null) fos = TApi.getAllFoIDs(api,917716145121009664L);
            if (frs == null) frs = TApi.getAllFrIDs(api,917716145121009664L);

            LinkedList<Long> ifr = TApi.getAllFrIDs(api,api.getId());

            LinkedList<Long> fj = new LinkedList<Long>(ifr);
            fj.retainAll(fos);

            LinkedList<Long> jf = new LinkedList<Long>(ifr);
            jf.retainAll(frs);

            LinkedList<Long> hg = new LinkedList<Long>(fj);
            hg.retainAll(jf);

            StringBuilder result = new StringBuilder();

            result.append("与互相关注 :");

            for (Long id : hg) {

                if (!UserArchive.INSTANCE.exists(id)) {

                    UserArchive.saveCache(api.showUser(id));

                }
                
                result.append("\n").append(UserArchive.INSTANCE.get(id).getHtmlURL());

            }

            result.append("-----------------------------\n单向关注的 :");

            for (Long id : fj) {

                if (!UserArchive.INSTANCE.exists(id)) {

                    UserArchive.saveCache(api.showUser(id));

                }

                result.append("\n").append(UserArchive.INSTANCE.get(id).getHtmlURL());

            }
            
            result.append("-----------------------------\n被关注的 :");

            for (Long id : jf) {

                if (!UserArchive.INSTANCE.exists(id)) {

                    UserArchive.saveCache(api.showUser(id));

                }

                result.append("\n").append(UserArchive.INSTANCE.get(id).getHtmlURL());

            }
            
            msg.send(result.toString()).exec();

            
        } catch (TwitterException e) {}

    }

}
