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
import cn.hutool.http.HttpRequest;

public class Telegraph {

		public static String API = "https://api.telegra.ph/";

		public static Gson gson = new Gson();

	  static <T extends Object> T send(String path,Class<T> resultClass,Object... params) {

				HttpRequest request = HttpUtil.createGet(API + path);

				for (int index = 0;index < params.length;index = index + 2) {

						if (params[index + 1] == null) continue;

						request.form(params[index].toString(),params[index + 1]);

				}

				HttpResponse resp = request.execute();

				if (!resp.isOk()) return null;

				JSONObject result = new JSONObject(resp.body());

				if (!result.getBool("ok",false)) return null;

				return gson.fromJson(result.getJSONObject("result").toString(),resultClass);


		}

		public static Account createAccount(String short_name,String author_name,String author_url) {

				return (Account) send("createAccount",Account.class,
															"short_name",short_name,
															"author_name",author_name,
															"author_url",author_url);

		}

		public static Account editAccountInfo(String access_token,String short_name,String author_name,String author_url) {

				return (Account) send("editAccountInfo",Account.class,
															"access_token",access_token,
															"short_name",short_name,
															"author_name",author_name,
															"author_url",author_url);

		}

		public static Account getAccountInfo(String access_token) {

				return (Account) send("getAccountInfo",Account.class,
															"access_token",access_token,
															"fields","[\"short_name\", \"author_name\", \"author_url\", \"auth_url\", \"page_count\""
															);

		}


		public static Account revokeAccessToken(String access_token) {

				return (Account) send("revokeAccessToken",Account.class,"access_token",access_token);

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

			  return (Page) send("createPage",Page.class,
													 "access_token",access_token,
													 "title",title,
													 "author_name",author_name,
													 "author_url",author_url,
													 "content",contentFormat.toString(),
													 "return_content",return_content);

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

				return (Page) send("editPage",Page.class,

													 "access_token",access_token,
													 "title",title,
													 "path",path,
													 "author_name",author_name,
													 "author_url",author_url,
													 "content",contentFormat.toString(),
													 "return_content",return_content);

		}

		public static Page getPage(String path,Boolean return_content) {

				return (Page) send("getPage",Page.class,

													 "path",path,
													 "return_content",return_content);


		}

		public static PageList getPageList(String access_token,Integer offset,Integer limit) {

				return (PageList) send("getPageList",PageList.class,

															 "access_token",access_token,
															 "offset",offset,
															 "limit",limit);

		}

		public static PageViews getViews(String path,Integer year,Integer month,Integer day,Integer hour) {

				return (PageViews) send("getViews",PageViews.class,

																"path",path,
																"year",year,
																"month",month,
																"day",day,
																"hour",hour);
				
		}

}
