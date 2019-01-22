package io.kurumi.ntt.auth;

import java.util.*;
import twitter4j.auth.*;
import io.kurumi.ntt.twitter.*;
import java.io.*;
import cn.hutool.log.*;
import cn.hutool.http.*;
import twitter4j.*;
import cn.hutool.core.util.*;

public class AuthManager {

    public static Log log = StaticLog.get("AuthManager");

    public HashMap<String,RequestToken> cache = new HashMap<>();
    public HashMap<String,AuthListener> listeners = new HashMap<>();

    public NanoAuthServer server;

    public String domain = "http://127.0.0.1";

    public Twitter api = ApiToken.defaultToken.createApi();

    public boolean init(int port, String domain) {

        try {

            server = new NanoAuthServer(this, port,domain);

            server.start();

        } catch (IOException e) {

            log.error(e, "认证服务器启动失败");

            server = null;
            
            return false;

        }

        try {

            if ("ok".equals(HttpUtil.get(domain + "/check"))) {

                log.debug("认证服务器正常...");

                this.domain = domain;

                return true;

            }

        } catch (Exception e) {

            log.error(e);

        }

        log.error("认证服务器无法访问.. nginx配置好了吗..？");

        server.stop();
        
        server = null;

        return false;


    }


    public String newRequest(AuthListener listener) {

        try {

            RequestToken token = api.getOAuthRequestToken(domain + "/callback");

            cache.put(token.getToken(), token);
            
            listeners.put(token.getToken(),listener);

            return token.getAuthenticationURL();

        } catch (TwitterException e) {

            log.error(e, "RequestToken请求失败...");

            return null;

        }

    }

    public TwiAccount authByUrl(String url) {

        HashMap<String, String> params = HttpUtil.decodeParamMap(StrUtil.subAfter(url, "?", true), "UTF-8");

        if (params == null) return null;
        
        String requestToken = params.get("oauth_token");
        String oauthVerifier = params.get("oauth_verifier");
        
        
        // println("verifier : " + oauthVerifier);

        return auth(requestToken, oauthVerifier);

    }

    public TwiAccount auth(String requestToken, String oauthVerifier) {

        if (requestToken == null || oauthVerifier == null) return null;
        
        if (!cache.containsKey(requestToken)) {

            log.error("不存在的 OAuthToken : " + requestToken);
            log.error("是你的服务器重启了吗？");

        }

        try {

            AccessToken accToken = api.getOAuthAccessToken(cache.remove(requestToken), oauthVerifier);

            TwiAccount acc =  new TwiAccount(ApiToken.defaultToken.apiToken, ApiToken.defaultToken.apiSecToken, accToken.getToken(), accToken.getTokenSecret());

            if (!acc.refresh()) {

                log.error("账号刷新失败...");

            }

            listeners.remove(requestToken).onAuth(acc);

            return acc;

        } catch (TwitterException e) {

            log.error(e , "认证出错... ");

        }

        return null;

    }

}
