package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.StaticLog;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class StatusDeleteTask extends TimerTask {

    public static Timer timer = new Timer();

    public static void start() {

        timer.scheduleAtFixedRate(new StatusDeleteTask(), NTT.nextHour(1), 60 * 60 * 1000L);

    }

    public static void stop() {

        timer.cancel();

    }

    @Override
    public void run() {

        for (TAuth account : TAuth.data.getAll()) {

            if (account.ad_s == null && account.ad_r == null && account.ad_t == null) continue;

            try {

                int count = executeDelete(account);

                if (count > 0) {

                    // new Send(account.user,"[推文定时删除] 已删除 " + count + " 条 .").async();

                }

            } catch (TwitterException e) {

                StaticLog.warn(e, "Delete Status Error");

            }

        }

    }

    public static int executeDelete(TAuth account) throws TwitterException {

        int count = 0;

        Twitter api = account.createApi();

        LinkedList<Status> statues = TApi.getAllStatus(api, account.id);

        for (Status s : statues) {

            if (account.ad_a != null) {

                long day = 24L * 60L * 60L * 1000L;

                // 绝对时间

                if (account.ad_d == null) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < day) break;

                } else if (account.ad_d == 0) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < 3L * day) break;

                } else if (account.ad_d == 1) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < 7L * day) break;

                } else if (account.ad_d == 2) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < 30L * day) break;

                } else if (account.ad_d == 3) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < 60L * day) break;

                } else if (account.ad_d == 4) {

                    if (DateUtil.betweenMs(s.getCreatedAt(), new Date()) < 90L * day) break;

                }

            } else {

                if (account.ad_d == null) {

                    if (DateUtil.betweenDay(s.getCreatedAt(), new Date(), true) < 1) break;

                } else if (account.ad_d == 0) {

                    if (DateUtil.betweenDay(s.getCreatedAt(), new Date(), true) < 3) break;

                } else if (account.ad_d == 1) {

                    if (DateUtil.betweenDay(s.getCreatedAt(), new Date(), true) < 7) break;

                } else if (account.ad_d == 2) {

                    if (DateUtil.betweenMonth(s.getCreatedAt(), new Date(), true) < 1) break;

                } else if (account.ad_d == 3) {

                    if (DateUtil.betweenMonth(s.getCreatedAt(), new Date(), true) < 2) break;

                } else if (account.ad_d == 4) {

                    if (DateUtil.betweenMonth(s.getCreatedAt(), new Date(), true) < 3) break;

                }


            }

            if (s.isRetweet()) {

                if (account.ad_t == null) {

                    continue;

                }

            }
            if (s.getInReplyToStatusId() != -1) {

                if (account.ad_r == null) {

                    continue;

                }

            } else if (account.ad_s == null) {

                continue;

            }

            try {

                api.destroyStatus(s.getId());

                count++;

            } catch (TwitterException e) {

                if (e.getErrorCode() != 144) throw e;

            }

        }

        return count;

    }

}
