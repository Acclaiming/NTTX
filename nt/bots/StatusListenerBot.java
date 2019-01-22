package io.kurumi.nt.bots;

import twitter4j.*;
import io.kurumi.nt.*;

public abstract class StatusListenerBot {

    public abstract String getBotName();
    public abstract String getConfigKey();
    public abstract void onStatus(Twitter api, Status status) throws TwitterException;

    public long getStatusId(TwiAccount acc) {

        return acc.userData.getLong(getConfigKey(), -1L);

    }

    public void setStatusId(TwiAccount acc, long id) {

        acc.userData.put(getConfigKey(), id);
        acc.save();

    }

    public Adapter createAdapter(TwiAccount acc) {

        return new Adapter(acc);

    }

    public class Adapter extends StatusAdapter {

        private TwiAccount acc;
        private Twitter api;

        private long statusId;

        public Adapter(TwiAccount acc) {
            this.acc = acc;
            api = acc.createApi();
            statusId = getStatusId(acc);


            System.out.println("「" + getBotName() + "」"  + "正在监听 : " + statusId);
        }


        @Override
        public void onStatus(final Status status) {

            if (status.getInReplyToStatusId() == statusId) {

                process(status);

                return;

            }

            try {

                Status inrs =  api.showStatus(status.getInReplyToStatusId());
                
                if (inrs.isRetweet() && inrs.getRetweetedStatus().getId() == statusId) {

                    process(status);

                }


            } catch (TwitterException e) {}

        }

        private void process(Status status) {

            System.out.println("「" + getBotName() + "」" + "正在处理 : ");
            System.out.println(NTApi.formatUsernName(status.getUser()) + " : " + status.getText());

            try {

                StatusListenerBot.this.onStatus(api, status);

            } catch (TwitterException e) {

                System.out.println("「" + getBotName() + "」"  + "出错 : ");
                e.printStackTrace();

            }

            System.out.println("「" + getBotName() + "」 处理完成.");



        }





    }

}
