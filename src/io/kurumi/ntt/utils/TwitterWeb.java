package io.kurumi.ntt.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import io.netty.handler.codec.HeadersUtils;
import cn.hutool.http.Header;
import cn.hutool.core.util.StrUtil;
import java.util.HashSet;
import cn.hutool.core.util.NumberUtil;

public class TwitterWeb {

	public static HashSet<Long> fetchStatusReplies(String screenName,Long statusId) {

		HashSet<Long> replies = new HashSet<>();

		HttpResponse result = HttpUtil.createGet("https://mobile.twitter.com/" + screenName + "/status/" + statusId).execute();

		if (result.getStatus() == 301) {

			result = HttpUtil.createGet("https://mobile.twitter.com" + result.header(Header.LOCATION)).execute();

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

				replies.addAll(fetchStatusReplies(replyFrom,replyId));

			}

		}

		return replies;

	}

}
