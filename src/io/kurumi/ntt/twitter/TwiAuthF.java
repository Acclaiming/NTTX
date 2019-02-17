package io.kurumi.ntt.twitter;

import cn.hutool.core.util.URLUtil;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.server.ServerFragment;
import io.kurumi.ntt.utils.Markdown;
import java.util.HashMap;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwiAuthF implements ServerFragment {

    public static final TwiAuthF INSTANCE = new TwiAuthF();

    public static HashMap<Integer,Listener> pre = new HashMap<>();
    public static HashMap<String,Listener> auth = new HashMap<>();

    public interface Listener {

        public void onAuth(String oauthVerifier);

        public void onAuth(TwiAccount account);

    }

    public static void pre(UserData user, Listener listener) {

        pre.put(user.id, listener);

    }

    @Override
    public Response handle(IHTTPSession session) {

        switch (URLUtil.url(session.getUri()).getPath()) {

                case "/auth" : {

                    final String userId = session.getParms().get("userId");

                    if (userId == null) return null;

                    Configuration conf = new ConfigurationBuilder()
                        .setOAuthConsumerKey(BotConf.TWITTER_CONSUMER_KEY)
                        .setOAuthConsumerSecret(BotConf.TWITTER_CONSUMER_KEY_SEC)
                        .build();

                    final Twitter api = new TwitterFactory(conf).getInstance();

                    try {

                        String url = auth(Integer.parseInt(userId), api);

                        Response resp = Response.newFixedLengthResponse(Status.REDIRECT_SEE_OTHER, "text/plain", "");

                        resp.addHeader("Location", url);

                        return resp;

                    } catch (Exception e) {

                        return Response.newFixedLengthResponse("认证出错，请稍后再来 （￣～￣） \n\n" + e.getMessage());

                    }



                }

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

    public String auth(final Integer userId, final Twitter api) throws TwitterException {

        final RequestToken requestToken = api.getOAuthRequestToken("https://" + BotConf.SERVER_DOMAIN + "/callback");

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

                            if (preL != null) {

                                preL.onAuth(account);

                            }

                        }

                    } catch (TwitterException e) {}

                }

                @Override
                public void onAuth(TwiAccount account) {
                }



            });

        return requestToken.getAuthorizationURL();

    }

}
