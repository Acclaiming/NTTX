package io.kurumi.telegraph;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import io.kurumi.telegraph.model.Account;
import io.kurumi.telegraph.model.Node;
import io.kurumi.telegraph.model.Page;
import java.util.List;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.telegraph.model.NodeElement;
import io.kurumi.telegraph.model.PageList;
import io.kurumi.telegraph.model.PageViews;

public class Telegraph {

		public static String API = "https://api.telegra.ph/";

		public static Gson gson = new Gson();

		public static Account createAccount(String short_name,String author_name,String author_url) {

				HttpResponse resp = HttpUtil.createGet(API + "/createAccount")

						.form("short_name",short_name)
						.form("author_name",author_name)
						.form("author_url",author_url)

						.execute();
						
					System.out.println(resp);

				return resp.isOk() ? gson.fromJson(resp.body(),Account.class) : null;

		}

		public static Account editAccountInfo(String access_token,String short_name,String author_name,String author_url) {

				HttpResponse resp = HttpUtil.createGet(API + "/editAccountInfo")

						.form("access_token",access_token)
						.form("short_name",short_name)
						.form("author_name",author_name)
						.form("author_url",author_url)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),Account.class) : null;

		}

		public static Account getAccountInfo(String access_token) {

				HttpResponse resp = HttpUtil.createGet(API + "/getAccountInfo")

						.form("access_token",access_token)
						.form("fields","[\"short_name\", \"author_name\", \"author_url\", \"auth_url\", \"page_count\"")

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),Account.class) : null;

		}


		public static Account revokeAccessToken(String access_token) {

				HttpResponse resp = HttpUtil.createGet(API + "/revokeAccessToken")

						.form("access_token",access_token)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),Account.class) : null;

		}

		public static Page createPage(String access_token,String title,String author_name,String author_url,List<Node> content,Boolean return_content) {

				JSONArray contentFormat = new JSONArray();

				for (Node node : content) {

						if (node instanceof NodeElement) {

								contentFormat.add(new JSONObject(gson.toJson(node)));

						} else {

								contentFormat.add(node.text);

						}


				}

				HttpResponse resp = HttpUtil.createGet(API + "/createPage")

						.form("access_token",access_token)
						.form("title",title)
						.form("author_name",author_name)
						.form("author_url",author_url)
						.form("content",contentFormat.toString())
						.form("return_content",return_content)

						.execute();
						
						System.out.println(resp);

				return resp.isOk() ? gson.fromJson(resp.body(),Page.class) : null;

		}

		public static Page editPage(String access_token,String path,String title,String author_name,String author_url,List<Node> content,Boolean return_content) {

				JSONArray contentFormat = new JSONArray();

				for (Node node : content) {

						if (node instanceof NodeElement) {

								contentFormat.add(new JSONObject(gson.toJson(node)));

						} else {

								contentFormat.add(node.text);

						}


				}

				HttpResponse resp = HttpUtil.createGet(API + "/editPage")

						.form("access_token",access_token)
						.form("title",title)
						.form("path",path)
						.form("author_name",author_name)
						.form("author_url",author_url)
						.form("content",contentFormat.toString())
						.form("return_content",return_content)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),Page.class) : null;

		}

		public static Page getPage(String path,Boolean return_content) {

				HttpResponse resp = HttpUtil.createGet(API + "/getPage")

						.form("path",path)
						.form("return_content",return_content)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),Page.class) : null;

		}

		public static PageList getPageList(String access_token,Integer offset,Integer limit) {

				HttpResponse resp = HttpUtil.createGet(API + "/getPageList")

						.form("access_token",access_token)
						.form("offset",offset)
						.form("limit",limit)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),PageList.class) : null;

		}

		public static PageViews getViews(String path,Integer year,Integer month,Integer day,Integer hour) {

				HttpResponse resp = HttpUtil.createGet(API + "/getViews")

						.form("path",path)
						.form("year",year)
						.form("month",month)
						.form("day",day)
						.form("hour",hour)

						.execute();

				return resp.isOk() ? gson.fromJson(resp.body(),PageViews.class) : null;

		}

}
