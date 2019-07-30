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
import java.net.URL;
import java.net.MalformedURLException;
import io.kurumi.ntt.Launcher;

public class FeedHtmlFormater {

	public static Pattern matchAll = Pattern.compile("(<([^> ]+)([^>]+)?>([^<]+)?</([^<]+)>|<[^<]+>|</|[^<]+)",Pattern.MULTILINE);

	public static String matchAttr = "[^\">]+\"([^\">]*)\"";

	public static Pattern matchHref = Pattern.compile("href" + matchAll);

	public static Pattern matchSrc = Pattern.compile("src" + matchAll);

	public static Pattern matchImg = Pattern.compile("<img[^>].*src[^\">]+\"([^\">]+)\"[^>]*>");

	public static HTMLFilter removeTags = new HTMLFilter(true);
	public static HTMLFilter removeTagsWithoutImg = new HTMLFilter(false);

	public static Pattern matchTagInterrupted = Pattern.compile(".*</[^>]+>.+<[^>]+");

	public static String format(RssSub.ChannelRss channel,SyndFeed feed,final SyndEntry entry) {

		return format(channel,feed,entry,false);

	}

	public static String format(final RssSub.ChannelRss channel,final SyndFeed feed,final SyndEntry entry,boolean debug) {

		int type = channel.format;

		if (type == 0) type = 2;

		StringBuilder html = new StringBuilder();

		String host = StrUtil.subBefore(entry.getLink(),"/",true);
		
		if (type > 8) {

			if (type == 9) {

				html.append(Html.b(feed.getTitle()));

				html.append("\n\n");

			}

			TelegraphAccount account = TelegraphAccount.defaultAccount();

			final String str = getContent(entry,false,true,false);
			
			final List<Node> content = removeTagsWithoutImg.formatTelegraph(str,host);

			content.add(new NodeElement() {{ tag = "hr"; }});

			if (channel.copyright == null) {

				content.add(new Node() {{ text = "由 "; }});

				content.add(new NodeElement() {{

							tag = "a";

							attrs = new HashMap<>();

							attrs.put("href","https://manual.kurumi.io");

							children = new LinkedList<>();

							children.add(new Node() {{ text = "NTT"; }});


						}});

				content.add(new Node() {{ text = " 制作"; }});

			} else {

				content.add(new Node() {{ text = channel.copyright; }});

			}

			content.add(new Node() {{ text = "   查看原文 : "; }});

			content.add(new NodeElement() {{

						tag = "a";

						attrs = new HashMap<>();

						attrs.put("href",entry.getLink());

						children = new LinkedList<>();

						children.add(new Node() {{ text = entry.getTitle() ; }});


					}});

			// content.add(new NodeElement() {{ tag = "br"; }});

			Page page = Telegraph.createPage(account.access_token,entry.getTitle(),StrUtil.isBlank(entry.getAuthor()) ? feed.getTitle() : entry.getAuthor().trim(),feed.getLink(),content,false);

			if (page == null) {



			} else {

				html.append(Html.a(entry.getTitle(),page.url));

			}

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

			html.append(getContent(entry,true,false,debug));

		} else if (type == 4) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,true,false,debug));

			html.append("\n\n");

			html.append(entry.getLink());

		} else if (type == 5) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,true,false,debug));

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

			html.append(getContent(entry,false,false,debug));

		} else if (type == 7) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,false,false,debug));

			html.append("\n\n");

			html.append(entry.getLink());

		} else if (type == 8) {

			html.append(Html.b(entry.getTitle()));

			html.append("\n\n");

			html.append(getContent(entry,false,false,debug));

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

	public static Pattern LINES = Pattern.compile("\n( |　)*\n( |　)*\n");

	private static String getContent(SyndEntry entry,boolean desciption,boolean withImg,boolean debug) {

		String html;

		if (entry.getContents() != null && !entry.getContents().isEmpty() && !StrUtil.isBlank(entry.getContents().get(0).getValue())) {

			// Atom Feed

			html = entry.getContents().get(0).getValue();

		} else {

			html = entry.getDescription().getValue();

		}

		html = Html.unescape(html);

		//	html = URLUtil.decode(html);

		html = html.replaceAll("<br ?/? ?>","\n");

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

		String host = StrUtil.subBefore(entry.getLink(),"/",true);

		if (withImg) {

			html = removeTagsWithoutImg.filter(html,host);

		} else {

			html = removeTags.filter(html,host);

		}

		if (!withImg && html.contains("<b> +<b>")) {

			html = html.replaceAll("</?b>","");

		}

		while (ReUtil.contains(LINES,html)) {

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
