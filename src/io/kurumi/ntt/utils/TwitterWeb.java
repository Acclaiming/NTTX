package io.kurumi.ntt.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import io.netty.handler.codec.HeadersUtils;
import cn.hutool.http.Header;
import cn.hutool.core.util.StrUtil;
import java.util.HashSet;
import cn.hutool.core.util.NumberUtil;
import java.util.TreeSet;

public class TwitterWeb {

	public static TreeSet<Long> fetchStatusReplies(String screenName,Long statusId,boolean loop) {

		TreeSet<Long> replies = new TreeSet<>();

		HttpResponse result = HttpUtil
			.createGet("https://mobile.twitter.com/" + screenName + "/status/" + statusId)
			.header(Header.USER_AGENT,"MSIE 6.0")
			.execute();

		if (result.getStatus() == 301) {

			result = HttpUtil
				.createGet("https://mobile.twitter.com" + result.header(Header.LOCATION))
				.header(Header.USER_AGENT,"MSIE 6.0")
				.execute();

		}

		if (result.isOk()) {

			String statusHtml = result.body();

			statusHtml = StrUtil.subAfter(statusHtml,"<div class=\"timeline replies\">",false);

			while (statusHtml.contains("timestamp")) {

				statusHtml = StrUtil.subAfter(statusHtml,"timestamp",false);

				String replyUrl = StrUtil.subBefore(statusHtml,"</a>",false);

				String replyFrom = StrUtil.subBetween(replyUrl,"href=\"/","/status");

				Long replyId = NumberUtil.parseLong(StrUtil.subBetween(replyUrl,"status/","?"));

				replies.add(replyId);

				if (loop) {

					replies.addAll(fetchStatusReplies(replyFrom,replyId,loop));

				}

			}

		}

		return replies;

	}

}
