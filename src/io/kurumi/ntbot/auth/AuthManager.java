package io.kurumi.ntbot.auth;

import java.util.*;
import twitter4j.auth.*;
import io.kurumi.ntbot.twitter.*;
import java.io.*;
import cn.hutool.log.*;
import cn.hutool.http.*;
import twitter4j.*;
import cn.hutool.core.util.*;

public class AuthManager {

    public static Log log = StaticLog.get("AuthManager");

    public HashMap<String,RequestToken> cache = new HashMap<>();
    public HashMap<String,AuthListener> listeners = new HashMap<>();
    
    public NanoAuthServer server = new NanoAuthServer(this, 18964);

    public String domain = "127.0.0.1";
    
    public Twitter api = ApiToken.defaultToken.createApi();

    public boolean init(String domain) {

        try {

            server.start();

        } catch (IOException e) {

            log.error(e, "认证服务器启动失败");

            return false;
            
        }

        try {

            if ("ok".equals(HttpUtil.get("https://" + domain + "/check"))) {

                log.debug("认证服务器正常...");

                this.domain = domain;
                
                return true;

            }

        } catch (Exception e) {

            log.error(e, "认证服务器无法访问.. nginx配置好了吗..？");

        }
        
        return false;

    }


    public String newRequest(AuthListener listener) {

        try {

            RequestToken token = api.getOAuthRequestToken(domain);

            cache.put(token.getToken(),token);
            
            return token.getAuthenticationURL();
            
        } catch (TwitterException e) {

            log.error(e, "RequestToken请求失败...");

            return null;
            
        }

    }
    
    public TwiAccount authByUrl(String url) {
        
        HashMap<String, String> params = HttpUtil.decodeParamMap(StrUtil.subAfter(url, "?", true), "UTF-8");

        String requestToken = params.get("oauth_token");
        String oauthVerifier = params.get("oauth_verifier");

        // println("verifier : " + oauthVerifier);

        return auth(requestToken,oauthVerifier);
        
    }
    
    public TwiAccount auth(String oauthToken,String oauthVerifier) {
        
        if (!cache.containsKey(oauthToken)) {
            
            log.error("不存在的 OAuthToken : " + oauthToken);
            log.error("是你的服务器重启了吗？");
            
        }
        
        try {
            
            AccessToken accToken = api.getOAuthAccessToken(cache.get(oauthToken), oauthVerifier);

            TwiAccount acc =  new TwiAccount(ApiToken.defaultToken.apiToken, ApiToken.defaultToken.apiSecToken, accToken.getToken(), accToken.getTokenSecret());

            if (!acc.refresh()) {
                
                log.error("账号刷新失败...");
                
            }
            
            listeners.get(oauthToken).onAuth(acc);
            
            return acc;

        } catch (TwitterException e) {
            
            log.error(e , "认证出错... ");
            
        }
        
        return null;

    }

}
