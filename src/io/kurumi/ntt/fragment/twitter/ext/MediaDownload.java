package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;
import twitter4j.MediaEntity;
import cn.hutool.core.util.ArrayUtil;
import twitter4j.MediaEntity.Variant;

public class MediaDownload extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("media");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        if (params.length == 0) {

            msg.send("/media [推文ID|链接]...").exec();

            return;

        }

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
            
            msg.send("视频链接 :",urls.toString()).enableLinkPreview().exec();

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).exec();

        }

    }

}
