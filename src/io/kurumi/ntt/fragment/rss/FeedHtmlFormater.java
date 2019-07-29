package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import io.kurumi.ntt.fragment.graph.TelegraphAccount;
import io.kurumi.ntt.utils.Html;
import io.kurumi.telegraph.Telegraph;
import io.kurumi.telegraph.model.Node;
import java.util.LinkedList;
import java.util.List;
import io.kurumi.telegraph.model.Page;
import io.kurumi.telegraph.model.NodeElement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.model.request.Send;

public class FeedHtmlFormater {

	public static Pattern matchAll = Pattern.compile("(<([^> ]+)([^>]+)?>([^<]+)?</([^<]+)>|<[^<]+>|</|[^<]+)",Pattern.MULTILINE);

	public static String matchAttr = "[^\">]+\"([^\">]*)\"";

	public static Pattern matchHref = Pattern.compile("href" + matchAll);

	public static Pattern matchSrc = Pattern.compile("src" + matchAll);

	public static Pattern matchImg = Pattern.compile("<img[^>].*src[^\">]+\"([^\">]+)\"[^>]*>");

	public static HTMLFilter removeTags = new HTMLFilter(true);
	public static HTMLFilter removeTagsWithoutImg = new HTMLFilter(false);

	public static Pattern matchTagInterrupted = Pattern.compile(".*</[^>]+>.+<[^>]+");

	public static String format(int type,SyndFeed feed,final SyndEntry entry) {

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

		if (type == 9) {

			TelegraphAccount account = TelegraphAccount.defaultAccount();

			List<Node> content = new LinkedList<>();

			final String htmlText = getContent(entry,false,true);

			final Matcher matcher = matchAll.matcher(htmlText);

			while (matcher.find()) {

				final String all = matcher.group(1);

				String tag = matcher.group(2);

				String attrs = matcher.group(3);

				final String chText = matcher.group(4);

				// String endTag = matcher.group(5);

				if (StrUtil.isBlank(tag)) {

					content.add(new Node() {{ text = all; }});

				} else {

					NodeElement node = new NodeElement();

					node.tag = tag;

					if (!StrUtil.isBlank(chText)) {

						node.children.add(new Node() {{ text = chText; }});

					}

					if ("a".equals(tag)) {

						if (StrUtil.isBlank(attrs)) continue;

						String href = ReUtil.getGroup1(matchHref,attrs);

						node.attrs = new HashMap<>();

						node.attrs.put("href",href);

						System.out.println("HREF : " + href);


					} else if ("img".equals(tag)) {

						if (StrUtil.isBlank(attrs)) continue;

						String src = ReUtil.getGroup1(matchSrc,attrs);

						node.attrs = new HashMap<>();

						node.attrs.put("src",src);

						System.out.println("SRC : " + src);

					}

					content.add(node);

				}

			}


			content.add(new NodeElement() {{ tag = "br"; }});
			content.add(new NodeElement() {{ tag = "br"; }});

			content.add(new NodeElement() {{

						tag = "a";

						attrs = new HashMap<>();

						attrs.put("href",entry.getLink());

						children = new LinkedList<>();

						children.add(new Node() {{ text = "点此查看原文" ; }});


					}});

			Page page = Telegraph.createPage(account.access_token,entry.getTitle(),StrUtil.isBlank(entry.getAuthor()) ? feed.getTitle() : entry.getAuthor().trim(),feed.getLink(),content,false);

			if (page == null) {}

			html.append(Html.a(entry.getTitle(),page.url));

		} else if (type == 1) {

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

			html.append(getContent(entry,true,false));

		} else if (type == 4) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,true,false));

			html.append("\n\n");

			html.append(entry.getLink());

		} else if (type == 5) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,true,false));

			html.append("\n\n");

			if (!StrUtil.isBlank(entry.getAuthor()) && !"rsshub".equals(entry.getAuthor().trim().toLowerCase())) {

				html.append("作者 : ");

				html.append(Html.b(entry.getAuthor())).append(" - ");

			} else {

				html.append("来自 : ");

			}

			html.append(Html.a(feed.getTitle(),entry.getLink()));

		} else if (type == 6) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,false,false));

		} else if (type == 7) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,false,false));

			html.append("\n\n");

			html.append(entry.getLink());

		} else {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,false,false));

			html.append("\n\n");

			if (!StrUtil.isBlank(entry.getAuthor()) && !"rsshub".equals(entry.getAuthor().trim().toLowerCase())) {

				html.append("作者 : ");

				html.append(Html.b(entry.getAuthor())).append(" - ");

			} else {

				html.append("来自 : ");

			}

			html.append(Html.a(feed.getTitle(),entry.getLink()));


		}

		return html.toString();

	}

	public static Pattern LINES = Pattern.compile("\n( +)?\n( +)?\n",Pattern.MULTILINE);

	private static String getContent(SyndEntry entry,boolean desciption,boolean withImg) {

		String html;

		if (entry.getContents() != null && !entry.getContents().isEmpty() && !StrUtil.isBlank(entry.getContents().get(0).getValue())) {

			// Atom Feed

			html = entry.getContents().get(0).getValue();

		} else {

			html = entry.getDescription().getValue();

		}

		html = Html.unescape(html);

		//	html = URLUtil.decode(html);

		if (!withImg) {

			html = html.replaceAll("<br ?/? ?>","\n");

		}
		
		html = html.replace("<strong>","<b>").replace("</strong>","</b>");
		
		html = removeADs(html);

		if (!withImg) {

			html = ReUtil.replaceAll(html,matchImg,"\n\n" + Html.a("图片","$1") + "\n\n");

		}


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

		if (withImg) {

			html = removeTagsWithoutImg.filter(html);

		} else {

			html = removeTags.filter(html);

		}
		
		while (ReUtil.isMatch(LINES,html)) {

			html = ReUtil.replaceAll(html,LINES,"\n\n");

		}

		if (html.startsWith(entry.getTitle())) {

			// html = html.substring(entry.getTitle().length()).trim();

		}

		if (desciption) {

			//	String after = html.substring(139,html.length());

			html = html.substring(140);

			if (ReUtil.isMatch(matchTagInterrupted,html)) {

				html = html + StrUtil.subBefore(html,"<",true);

				// 如果HTML被截断 向前截取开始符号

			}

			html = html + "...";

		}

		html.replace("\t","");
		
		return html.trim();

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
