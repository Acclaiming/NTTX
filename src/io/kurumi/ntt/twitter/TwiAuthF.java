package io.kurumi.ntt.twitter;

import cn.hutool.core.util.URLUtil;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.server.ServerFragment;
import io.kurumi.ntt.utils.Markdown;
import java.util.HashMap;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwiAuthF implements ServerFragment {

    public static final TwiAuthF INSTANCE = new TwiAuthF();

    public static HashMap<String,Listener> pre = new HashMap<>();
    public static HashMap<String,Listener> auth = new HashMap<>();

    public interface Listener {

        public void onAuth(String oauthVerifier);

        public void onAuth(TwiAccount account);

    }

    public static void pre(UserData user, Listener listener) {

        pre.put(user.id.toString(), listener);

    }

    @Override
    public Response handle(IHTTPSession session) {

        switch (URLUtil.url(session.getUri()).getPath()) {

                case "/auth" : {

                    final String userId = session.getParms().get("userId");

                    if (userId == null) return null;

                    if (userId == null) {

                        Configuration conf = new ConfigurationBuilder()
                            .setOAuthConsumerKey(BotConf.TWITTER_CONSUMER_KEY)
                            .setOAuthConsumerSecret(BotConf.TWITTER_CONSUMER_KEY_SEC)
                            .build();

                        final Twitter api = new TwitterFactory(conf).getInstance();

                        try {

                            auth(Integer.parseInt(userId), api);

                        } catch (Exception e) {}

                    }


                } break;

                case "/callback" : {

                    String requestToken = session.getParms().get("oauth_token");
                    String oauthVerifier = session.getParms().get("oauth_verifier");

                    if (requestToken != null && oauthVerifier != null && auth.containsKey(requestToken)) {

                        auth.remove(requestToken).onAuth(oauthVerifier);

                        return Response.newFixedLengthResponse(Markdown.parsePage("请返回Bot", "#NTTBot", "账号认证 ~\n请返回Bot (´▽`ʃƪ)"));


                    } else {

                        return Response.newFixedLengthResponse(Markdown.parsePage("请返回Bot", "#NTTBot", "账号认证 ~\n这个认证链接过期了啦 ~\n是不是刷新了界面/登录了两次 ？ (´▽`ʃƪ)", "\n"));


                    }


                }

        }

        return null;

    }

    public void auth(final Integer userId, final Twitter api) throws TwitterException {

        final RequestToken requestToken = api.getOAuthRequestToken();

        final Listener preL = pre.remove(userId);

        auth.put(requestToken.getToken(), new Listener() {

                @Override
                public void onAuth(String oauthVerifier) {

                    if (preL != null) preL.onAuth(oauthVerifier);

                    try {

                        AccessToken token = api.getOAuthAccessToken(oauthVerifier);

                        TwiAccount account = new TwiAccount();

                        account.apiToken = BotConf.TWITTER_CONSUMER_KEY;
                        account.apiSec = BotConf.TWITTER_CONSUMER_KEY_SEC;
                        account.accToken = token.getToken();
                        account.accSec = token.getTokenSecret();

                        account.belong = userId;

                        if (account.refresh()) {

                            account.save();

                            preL.onAuth(account);

                        }

                    } catch (TwitterException e) {}

                }

                @Override
                public void onAuth(TwiAccount account) {
                }



            });


    }

}
