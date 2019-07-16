package io.kurumi.ntt.fragment.rss;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndEntry;
import io.kurumi.ntt.utils.Html;
import java.util.LinkedList;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReUtil;

public class FeedHtmlFormater {

		public static String format(int type,SyndFeed feed,SyndEntry entry) {

				if (type == 0) type = 2;

				StringBuilder html = new StringBuilder();

				/*

				 1 : 

				 [文章标题](文章链接)

				 2 : 

				 **来源**

				 [文章标题](文章链接)

				 3 :

				 **来源**

				 [文章标题](文章链接)

				 简介...

				 4.

				 **标题**

				 简介...

				 链接

				 5.

				 **标题**

				 简介

				 [作者 如果存在 - ] [来源](文章链接)

				 6.

				 **标题**

				 全文

				 7.

				 **标题**

				 全文

				 链接

				 8.

				 **标题**

				 全文

				 来自 [作者 如果存在 - ] [来源](文章链接)


				 */


				if (type == 1) {

						html.append(Html.a(entry.getTitle(),entry.getLink()));

				} else if (type == 2) {

					  html.append(Html.b(feed.getTitle()));

						html.append("\n\n");

						html.append(Html.a(entry.getTitle(),entry.getLink()));


				} else if (type == 3) {

						html.append(Html.b(feed.getTitle()));

						html.append("\n\n");

						html.append(Html.a(entry.getTitle(),entry.getLink()));

						html.append("\n\n");

						html.append(getContent(entry,true));

				} else if (type == 4) {

						html.append(Html.b(entry.getTitle()));

						html.append("\n\n");

						html.append(getContent(entry,true));

						html.append("\n\n");

						html.append(entry.getLink());

				} else if (type == 5) {

						html.append(Html.b(entry.getTitle()));

						html.append("\n\n");

						html.append(getContent(entry,true));

						html.append("\n\n");

						if (!StrUtil.isBlank(entry.getAuthor()) && "rsshub".equals(entry.getAuthor().trim().toLowerCase())) {

								html.append("作者 : ");

								html.append(Html.b(entry.getAuthor())).append(" - ");

						} else {

								html.append("来自 : ");

						}

						html.append(Html.a(feed.getTitle(),entry.getLink()));

				} else if (type == 6) {

						html.append(Html.b(entry.getTitle()));

						html.append("\n\n");

						html.append(getContent(entry,false));

				} else if (type == 7) {

						html.append(Html.b(entry.getTitle()));

						html.append("\n\n");

						html.append(getContent(entry,false));

						html.append("\n\n");

						html.append(entry.getLink());

				} else {

						html.append(Html.b(entry.getTitle()));

						html.append("\n\n");

						html.append(getContent(entry,false));

						html.append("\n\n");

						if (!StrUtil.isBlank(entry.getAuthor()) && "rsshub".equals(entry.getAuthor().trim().toLowerCase())) {

								html.append("作者 : ");

								html.append(Html.b(entry.getAuthor())).append(" - ");

						} else {

								html.append("来自 : ");

						}

						html.append(Html.a(feed.getTitle(),entry.getLink()));


				}

				return html.toString();

		}

		private static String getContent(SyndEntry entry,boolean desciption) {

				String html;

				if (entry.getContents() != null && !entry.getContents().isEmpty() && !StrUtil.isBlank(entry.getContents().get(0).getValue())) {

						// Atom Feed

						html = entry.getContents().get(0).getValue();

				} else {

						html = entry.getDescription().getValue();

				}

				html = Html.unescape(html);

				html = URLUtil.decode(html);

				html = html.replace("<br>","\n");

				
				//html = ReUtil.replaceAll(html,"<img[^>].*src[^\">]+\"([^\">]*)\"[^>]*>");
				/*
				
				while (html.contains("<img")) {

						String before = StrUtil.subBefore(html,"<img",false);

						String after = StrUtil.subAfter(html,"<img",false);

						if (!after.contains(">")) break;

						String code = StrUtil.subBefore(after,">",false);

						after = StrUtil.subAfter(after,">",false);

						if (!code.contains("src")) break;

						code = StrUtil.subAfter(code,"src",false);

						code = StrUtil.subAfter(code,"\"",false);
						code = StrUtil.subBefore(code,"\"",false);

						html = before + Html.a("图片",code) + after;

				}
				
				*/

				html = html.replaceAll("<(?!/?(a|b|i|code|pre|em)\b)[^>]+>","");

				html = removeADs(html).trim();

				if (html.startsWith(entry.getTitle())) {

						html = html.substring(entry.getTitle().length()).trim();

				}

				if (desciption) {

						//	String after = html.substring(139,html.length());

						html = html.substring(140);

						if (html.matches(".*</[^>]+>.+<[^>]+")) {

								html = StrUtil.subBefore(html,"<",true);

								// 如果HTML被截断 向前截取开始符号

						}

						html = html + "...";

				}

				return html;

		}

		private static String removeADs(String content) {

				String[] lines = content.split("\n");

				LinkedList<String> result = new LinkedList<>();

				for (String line : ArrayUtil.reverse(lines)) {

						// FeedX 广告

						if (line.contains("获取更多RSS")) continue;
						if (line.contains("https://feedx.net")) continue;
						if (line.contains("https://feedx.co")) continue;

						// FetchRSS

						if (line.contains("http://fetchrss.com")) continue;

						// Feed43
						
						if (line.contains("http://feed43.com/")) {

								line = StrUtil.subBefore(line,"<p><sub><i>-- Delivered by",false);

						}
						
						result.add(line);

				}

				return ArrayUtil.join(ArrayUtil.reverse(result.toArray()),"\n");

		}

}
