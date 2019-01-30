package io.kurumi.nttools.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.util.HashMap;

// 没有好用的所以写了一个简单的API

public class Telegraph {

    public static String API_URL = "https://api.telegra.ph/";

    /**

     Use this method to create a new Telegraph account. Most users only need one account, but this can be useful for channel administrators who would like to keep individual author names and profile links for each of their channels. On success, returns an Account object with the regular fields and an additional access_token field.

     short_name (String, 1-32 characters)

     Required. Account name, helps users with several accounts remember which they are currently using. Displayed to the user above the "Edit/Publish" button on Telegra.ph, other users don't see this name.

     author_name (String, 0-128 characters)

     Default author name used when creating new articles.

     author_url (String, 0-512 characters)

     Default profile link, opened when users click on the author's name below the title. Can be any link, not necessarily to a Telegram profile or channel.

     Sample request

     https://api.telegra.ph/createAccount?short_name=Sandbox&author_name=Anonymous

     */

    public static Account createAnonymousAccount()  {   

        return createAccount("Sandbox", "Anonymous");

    }

    public static Account createAccount(String shortName, String authorName) {

        return createAccount(shortName, authorName, null);

    }

    public static Account createAccount(String shortName, String authorName, String authorUrl) {

        HashMap<String,Object> paramsMap = new HashMap<>();

        paramsMap.put("short_name", shortName);

        paramsMap.put("author_name", authorName);

        if (authorUrl != null) {

            paramsMap.put("author_url", authorUrl);

        }

        try {

            JSONObject resp = new JSONObject(HttpUtil.get(API_URL + "createAccount"));

            if (resp.getBool("ok", false)) {

                return new Account(resp);

            }

        } catch (Exception e) {}

        return null;

    }
    
    public static Page createPage(String title, String content) {

        return createPage(null,title,content);

    }
    
    
    public static Page createPage(String authorName, String title, String content) {

        return createPage(authorName,null,title,content);

    }
    
    public static Page createPage(String authorName, String authorUrl, String title, String content) {

        return createPage(authorName,authorUrl,title,content,null);

    }
    
    public static Page createPage(String authorName, String authorUrl, String title, String content, Boolean returnContent) {
    
        return createPageWithAuth(createAccount(authorName,authorName).getAccessToken(),authorName,authorUrl,title,content,returnContent);

    }
    
    public static Page createPageWithAuth(Account account, String title, String content) {

        return createPageWithAuth(account.getAccessToken(),account.getAuthorName(),account.getAuthorUrl(),title,content,null);

    }
    
    public static Page createPageWithAuth(Account account, String title, String content, Boolean returnContent) {
        
        return createPageWithAuth(account.getAccessToken(),account.getAuthorName(),account.getAuthorUrl(),title,content,returnContent);
        
    }
    
    public static Page createPageWithAuth(String accessToken, String title, String content) {

        return createPageWithAuth(accessToken,null,title,content);

    }
    
    
    public static Page createPageWithAuth(String accessToken, String authorName, String title, String content) {

        return createPageWithAuth(accessToken,authorName,null,title,content);

    }
    
    public static Page createPageWithAuth(String accessToken, String authorName, String authorUrl, String title, String content) {
    
        return createPageWithAuth(accessToken,authorName,authorUrl,title,content,null);
        
    }

    public static Page createPageWithAuth(String accessToken, String authorName, String authorUrl, String title, String content, Boolean returnContent) {

        HashMap<String,Object> paramsMap = new HashMap<>();

        if (accessToken != null) {

            paramsMap.put("access_token", accessToken);

        }

        if (authorName != null) {

            paramsMap.put("author_name", authorName);

        }

        if (authorUrl != null) {

            paramsMap.put("author_url", authorUrl);

        }

        paramsMap.put("title", title);

        paramsMap.put("content", content);

        if (returnContent != null) {

            paramsMap.put("return_content", returnContent);

        }

        try {

            JSONObject resp = new JSONObject(HttpUtil.get(API_URL + "createPage"));

            if (resp.getBool("ok", false)) {

                return new Page(resp);

            }

        } catch (Exception e) {}
        
        return null;

    }

    /**

     This object represents a Telegraph account. 

     */

    public static class Account extends JSONObject {

        public Account() {}

        public Account(JSONObject obj) { super(obj); }

        public Account(String json) { super(json); }

        /**

         Account name, helps users with several accounts remember which they are currently using. Displayed to the user above the "Edit/Publish" button on Telegra.ph, other users don't see this name.

         */

        public String getShortName()  {

            return getStr("short_name");

        }

        public void setShortName(String shortName) {

            put("short_name", shortName);

        }

        /**

         Default author name used when creating new articles.

         */

        public String getAuthorName() {

            return getStr("author_name");

        }

        public void setAuthorName(String authorName) {

            put("author_name", authorName);

        }

        /**

         Profile link, opened when users click on the author's name below the title. Can be any link, not necessarily to a Telegram profile or channel.

         */

        public String getAuthorUrl() {

            return getStr("author_url");

        }

        public void setAuthorUrl(String authorUrl) {

            put("author_url", authorUrl);

        }

        /**

         Optional. Only returned by the createAccount and revokeAccessToken method. Access token of the Telegraph account.

         */

        public String getAccessToken() {

            return getStr("access_token");

        }

        public void setAccessToken(String accessToken) {

            put("access_token", accessToken);

        }

        /**

         Optional. Number of pages belonging to the Telegraph account.

         */

        public Integer getPageCount() {

            return getInt("page_count");

        }

        public void setPageCount(int pageCount) {

            put("page_count", pageCount);

        }


    }

    /**

     This object represents a page on Telegraph.

     */

    public static class Page extends JSONObject {

        public Page() {}

        public Page(JSONObject obj) { super(obj); }

        public Page(String json) { super(json); }

        /**

         Path to the page.

         */
        public String getPath() {

            return getStr("path");

        }

        public void setPath(String path) {

            put("path", path);

        }

        /**

         URL of the page.

         */
        public String getUrl() {

            return getStr("url");

        }

        public void setUrl(String url) {

            put("url", url);

        }

        /**

         Title of the page.

         */

        public String getTitle() {

            return getStr("title");

        }

    }

}
