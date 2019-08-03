package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MediaDownload extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("media");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (NTT.checkDropped(user,msg)) return;
		
        if (params.length == 0) {

            msg.send("/media [推文ID|链接]...").exec();

            return;

        }

        requestTwitter(user, msg);


    }

    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        Twitter api = account.createApi();

        try {

            Status status = api.showStatus(NTT.parseStatusId(params[0]));

            MediaEntity[] medias = status.getMediaEntities();

            if (medias.length == 0) {

                msg.send("这条推文好像没有媒体... (").exec();

                return;

            }

            StringBuilder urls = new StringBuilder();

            for (MediaEntity entry : medias) {

                MediaEntity.Variant[] varints = entry.getVideoVariants();

                for (MediaEntity.Variant variant : varints) {

                    urls.append("\n").append(variant.getUrl());

                }

            }

            msg.send("视频链接 :", urls.toString()).enableLinkPreview().async();

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).async();

        }

    }

}
