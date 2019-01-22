package io.kurumi.nt.bots;

import twitter4j.*;
import io.kurumi.nt.taip.*;
import io.kurumi.nt.*;

public class TextPolatBot extends StatusListenerBot {

    @Override
    public String getBotName() {
        return "TextPolatBot";
    }

    @Override
    public String getConfigKey() {
        return "bot_text_polat";
    }

    @Override
    public void onStatus(Twitter api, Status status) throws TwitterException {

        User target = status.getUser();
        
        api.createFavorite(status.getId());

        ResponseList<Status> tl = api.getUserTimeline(target.getId(), new Paging().count(233));

        int count = 0;
        float polar = 0;

        boolean next = false;
        
        for (Status s : tl) {

            if (s.isRetweet()) continue;

            try {

                float tp = AIUtil.nlpTextpolar(s.getText());

                if (tp != 0 || next) {
                    
                    
                    polar += tp;
                    count ++;
                    
                    next = false;
                    
                } else {
                    
                    next = true;
                    
                }

            } catch (Exception e) {}

        }

        polar = polar / (float)count;
        
        polar = polar * 100f;

        String r;

        if (((Float)polar).isNaN()) {

            r = "无结果...";

        } else if (polar == 0f) {

            r = "无感情...";

        } else if (polar > 0f) {

            r = "偏正面 : " + polar + "%";

        } else {

            r = "偏负面 : " + polar + "%";

        }

        NTApi.reply(api, status, "结果 : " +  NTApi.formatUsernName(status.getUser()) + "\n" + r);

    }

}
