package io.kurumi.ntt.fragment.rss;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import io.kurumi.ntt.db.AbsData;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;

import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rometools.rome.io.WireFeedOutput;
import cn.hutool.core.io.IoUtil;

import java.io.ByteArrayOutputStream;

import cn.hutool.core.util.CharsetUtil;

import java.io.IOException;

import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.db.PointData;
import com.rometools.rome.io.WireFeedInput;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.http.HtmlUtil;
import cn.hutool.core.thread.ThreadUtil;

public class RssSub extends Fragment {

    public static AbsData<String, RssInfo> info = new AbsData<String, RssInfo>(RssInfo.class);
    public static Data<ChannelRss> channel = new Data<ChannelRss>(ChannelRss.class);

    public static class RssInfo {

        public String id;
        public String title;
        public String link;
        public String last;

    }

    public static class ChannelRss {

        public Long id;

        public int format = 0;

        public boolean preview = false;

        public List<String> subscriptions;

        public String copyright;

        public Map<String, FeedError> error;

        public Long fromBot;

        public Long delay;

        public Long last;

        public static class FeedError {

            public Long startAt;

            public String errorMsg;

        }

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        if (isLauncher()) {

            registerFunction("rss_import","rss_export","rss_set_delay","rss_set_current","rss_sub","rss_set_copyright","rss_list","rss_unsub","rss_unsub_all","rss_set_format","rss_link_preview","rss_fetch");

        } else {

            registerFunction("import","export","set_delay","set_current","sub","set_copyright","list","unsub","unsub_all","set_format","link_preview","fetch");

        }

    }

    @Override
    public int checkFunction(UserData user,Msg msg,String function,String[] params) {

        return PROCESS_ASYNC;

    }

    final String POINT_IMPORT_OPML = "rss_import";

    @Override
    public void onPoint(UserData user,Msg msg,String point,PointData data) {

        if (data.with(msg).step == 0) {

            if (msg.doc() == null || !msg.doc().fileName().endsWith(".opml")) {

                msg.send("你正在导入 .opml 文件").withCancel().exec(data);

                return;

            }

            Opml opml;

            try {

                opml = (Opml) new WireFeedInput().build(msg.file());

            } catch (Exception ex) {

                msg.send("导入失败 \n{}",BotLog.parseError(ex)).async();

                return;

            }

            StringBuilder notice = new StringBuilder();

            for (Outline outline : opml.getOutlines()) {

                notice.append("\n").append(Html.b(outline.getTitle()));

            }

            data.data = opml;
            data.step = 1;
			
            msg.send("将要导入 : {}\n现在发送目标频道的用户名或者ID ~",notice).html().async();

        } else {

            Opml opml = data.data();

            if (!msg.hasText()) {

                msg.send("请输入目标频道").withCancel().async();
                return;

            }

            long channelId;

            if (NumberUtil.isNumber(msg.text())) {

                channelId = NumberUtil.parseLong(msg.text());

                GetChatResponse resp = execute(new GetChat(channelId));

                if (resp == null) {

                    msg.send("Telegram 服务器连接错误").async();

                    return;

                } else if (!resp.isOk()) {

                    msg.send("错误 : BOT不在该频道\n( {} )",resp.description()).async();

                    return;

                } else if (resp.chat().type() != Chat.Type.channel) {

                    msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").async();

                    return;

                }

                channelId = resp.chat().id();


            } else {

                String username = msg.text();

                if (!username.startsWith("@")) username = "@" + username;

                GetChatResponse resp = execute(new GetChat(username));

                if (resp == null) {

                    msg.send("Telegram 服务器连接错误").async();

                    return;

                } else if (!resp.isOk()) {

                    msg.send("错误 : BOT不在该频道\n( {} )",resp.description()).async();

                    return;

                } else if (resp.chat().type() != Chat.Type.channel) {

                    msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").async();

                    return;

                }

                channelId = resp.chat().id();

            }

            if (!user.admin() && !GroupAdmin.fastAdminCheck(this,channelId,user.id,false)) {

                GetChatMemberResponse resp = execute(new GetChatMember(channelId,user.id.intValue()));

                if (resp == null) {

                    msg.send("Telegram 服务器连接错误").async();

                    return;

                } else if (!resp.isOk()) {

                    msg.send("错误 : 频道读取失败\n( {} )",resp.description()).async();

                    return;

                } else if (!(resp.chatMember().status() == ChatMember.Status.creator || resp.chatMember().status() == ChatMember.Status.administrator)) {

                    msg.send("错误 : 你不是频道管理员").async();

                    return;

                }

                return;

            }

            ChannelRss conf = channel.getById(channelId);

            if (conf == null) {

                conf = new ChannelRss();
                conf.id = channelId;
                conf.subscriptions = new LinkedList<>();

            }

            for (Outline outline : opml.getOutlines()) {

                if (outline.getXmlUrl() == null) return;

                if (!info.containsId(outline.getXmlUrl())) {

                    RssInfo rss = new RssInfo();

                    rss.id = outline.getXmlUrl();
                    rss.title = outline.getTitle();
                    rss.link = outline.getHtmlUrl();

                    info.setById(rss.id,rss);

                }

                conf.subscriptions.add(outline.getXmlUrl());

            }

            clearGroupPoint(user.id);

            msg.send("导入成功 ！").async();

        }

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (function.endsWith("import")) {

            PointData data = setPrivatePoint(user,msg,POINT_IMPORT_OPML);

            msg.send("现在发送 .opml 文件 ( opml 1.0 或 2.0)").exec(data);

            return;

        }

        if (params.length == 0) {
            msg.invalidParams("频道","...").async();
            return;
        }

        long channelId;

        if (NumberUtil.isNumber(params[0])) {

            channelId = NumberUtil.parseLong(params[0]);

            GetChatResponse resp = execute(new GetChat(channelId));

            if (resp == null) {

				msg.send("Telegram 服务器连接错误").async();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : BOT不在该频道\n( {} )",resp.description()).async();

				return;

			} else if (resp.chat().type() != Chat.Type.channel) {

				msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").async();

				return;

			}
			
            channelId = resp.chat().id();


        } else {

            String username = params[0];

            if (!username.startsWith("@")) username = "@" + username;

            GetChatResponse resp = execute(new GetChat(username));

            if (resp == null) {

				msg.send("Telegram 服务器连接错误").async();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : BOT不在该频道\n( {} )",resp.description()).async();

				return;

			} else if (resp.chat().type() != Chat.Type.channel) {

				msg.send("这不是一个频道 注意 : 如果需要为群组订阅RSS，可以将该群组绑定为频道的讨论群组。").async();

				return;

			}
            channelId = resp.chat().id();

        }

        if (!user.admin() && !GroupAdmin.fastAdminCheck(this,channelId,user.id,false)) {

            GetChatMemberResponse resp = execute(new GetChatMember(channelId,user.id.intValue()));

            if (resp == null) {

				msg.send("Telegram 服务器连接错误").async();

				return;

			} else if (!resp.isOk()) {

				msg.send("错误 : 频道读取失败\n( {} )",resp.description()).async();

				return;

			} else if (!(resp.chatMember().status() == ChatMember.Status.creator || resp.chatMember().status() == ChatMember.Status.administrator)) {

				msg.send("错误 : 你不是频道管理员").async();

				return;

			}

        }

        ChannelRss conf = channel.getById(channelId);

        if (conf == null) {

            conf = new ChannelRss();
            conf.id = channelId;
            conf.subscriptions = new LinkedList<>();

        }

        if (function.endsWith("export")) {

            if (conf.subscriptions == null || conf.subscriptions.isEmpty()) {

                msg.send("这个频道还没有订阅过RSS，无法导出。").async();

                return;

            }

            Opml opml = new Opml();

            opml.setFeedType("opml_1.0");

            List<Outline> outlines = new LinkedList<>();

            for (String rss : conf.subscriptions) {

                RssInfo rssInfo = info.getById(rss);

                URL url = URLUtil.url(rss);

                Outline outline = rssInfo == null ? new Outline(rss,url,url) : new Outline(rssInfo.title,url,URLUtil.url(rssInfo.link));

                outlines.add(outline);

            }

            opml.setOutlines(outlines);

            WireFeedOutput output = new WireFeedOutput();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try {

                output.output(opml,IoUtil.getWriter(bytes,CharsetUtil.CHARSET_UTF_8));

                executeAsync(new SendDocument(msg.chatId(),bytes.toByteArray()).fileName("rss_list.opml"));

            } catch (Exception e) {

                msg.send("导出失败\n{}",BotLog.parseError(e)).async();

                return;

            }

        } else if (function.endsWith("set_copyright")) {

            if (params.length == 1) {

                conf.copyright = null;

                msg.send("已重置，感谢对NTT的支持。").async();

            } else {

                conf.copyright = ArrayUtil.join(ArrayUtil.remove(msg.params(),0)," ");

                msg.send("设置成功 ~").async();

            }

        } else if (function.endsWith("set_delay")) {

            if (params.length == 1) {

                conf.delay = null;

                msg.send("已经重置为 15分钟").async();

            } else {


                long delay = 0;

                int m;

                if (!NumberUtil.isNumber(params[1]) || (m = NumberUtil.parseInt(params[1])) > 60 || m < 0) {

                    msg.send("无效的分钟数量 : " + params[1]).async();

                    return;

                }

                delay += m;

                if (params.length > 2) {

                    int h;

                    if (!NumberUtil.isNumber(params[2]) || (h = NumberUtil.parseInt(params[2])) > 24 || h < 0) {

                        msg.send("无效的小时数量 : " + params[2]).async();

                        return;

                    }

                    delay += h * 60;

                }

                if (params.length > 3) {

                    int d;

                    if (!NumberUtil.isNumber(params[3]) || (d = NumberUtil.parseInt(params[3])) > 7 || d < 1) {

                        msg.send("无效的天数 : " + params[3]).async();

                        return;

                    }

                    delay += d * 24 * 60;

                }

                if (user.admin() ? delay < 5 : delay < 15) {

                    msg.send("无效的延时设置 ( 时间太短 )").async();

                    return;

                } else if (delay > 7 * 24 * 60) {

                    msg.send("无效的延时设置 ( < 7天 )").async();

                    return;

                }

                conf.delay = delay;

                msg.send("拉取间隔设置成功。").async();

            }

        } else if (function.endsWith("set_current")) {

            if (isMainInstance()) {

                conf.fromBot = null;

                msg.send("重置成功 来源为 NTT本体").async();

            } else {

                conf.fromBot = origin.me.id();

                msg.send("设置成功 , 来源为 " + UserData.get(origin.me).userName()).html().async();


            }

        } else if (function.endsWith("fetch")) {

            if (params.length < 2) {

                msg.invalidParams("频道","链接").async();

                return;

            }

            try {

                SyndFeedInput input = new SyndFeedInput();

                String link = params[1];

                link = URLUtil.encode(link);

                HttpResponse resp;

                try {

                    resp = HttpUtil.createGet(link).header(Header.USER_AGENT,"NTT Feed Fetcher ( https://github.com/HiedaNaKan/NTTools)").execute();

                } catch (HttpException ex) {

                    msg.send("拉取失败 :\n\n{}",ex.getMessage()).async();

                    return;

                }

                if (!resp.isOk()) {

                    StringBuilder error = new StringBuilder();

                    error.append("HTTP ERROR ").append(resp.getStatus());

					if (resp.getStatus() == 301 || resp.getStatus() == 302) {

						error.append("\n\n链接指向新地址 : ").append(Html.code(resp.header(Header.LOCATION)));

					} else {

						String content = resp.body();

						if (!StrUtil.isBlank(content)) {

							error.append(" : \n\n");

							error.append(HtmlUtil.escape(content));

						}

					}

                    msg.send("拉取失败\n\n{}").html().async();

                    return;

                }

                SyndFeed feed = input.build(new StringReader(resp.body()));

                msg.send("正在输出 " + Html.a(feed.getTitle(),feed.getLink())).html().async();

                List<SyndEntry> entries = feed.getEntries();

                Collections.reverse(entries);

                int limit = params.length < 3 ? -1 : NumberUtil.parseInt(params[2]);

                if (limit > 0 && limit < entries.size()) {

                    entries = entries.subList(entries.size() - limit,entries.size());

                }

                for (SyndEntry entry : entries) {


                    limit--;

                    Send request = new Send(this,conf.id,FeedHtmlFormater.format(conf,feed,entry));

                    if (conf.format > 8 || conf.preview) {

                        request.enableLinkPreview();

                    }

                    SendResponse result = request.html().exec();

                    // if (conf.format == 9) return;

                    if (result != null) {

                        if (result.isOk()) continue;

                        msg.send(result.errorCode() + " - " + result.description()).async();

                    }

                    msg.send(request.request().getText()).async();

					ThreadUtil.sleep(1000);
					

                }

            } catch (FeedException e) {

                msg.send("拉取出错 : \n\n{}",BotLog.parseError(e)).async();

            } catch (UtilException e) {

                msg.send("无效的RSS链接").async();

            }


        }

        if (function.matches("(rss_sub|sub)")) {

            if (params.length < 2) {

                msg.invalidParams("channelId","rssUrl").async();

                return;

            }

            try {

                SyndFeedInput input = new SyndFeedInput();

                String link = params[1];

                link = URLUtil.encode(link);

                HttpResponse resp;

                try {

                    resp = HttpUtil.createGet(link).header(Header.USER_AGENT,"NTT Feed Fetcher ( https://github.com/HiedaNaKan/NTTools)").execute();

                } catch (IORuntimeException ex) {

                    msg.send("拉取失败 :\n\n{}",ex.getMessage()).async();

                    return;

                }

                if (!resp.isOk()) {

                    StringBuilder error = new StringBuilder();

                    error.append("HTTP ERROR ").append(resp.getStatus());

                    String content = resp.body();

                    if (!StrUtil.isBlank(content)) {

                        error.append(" : \n\n");

                        error.append(content);

                    }

                    msg.send("拉取失败\n\n{}",error.toString()).async();

                    return;

                }

                SyndFeed feed = input.build(new StringReader(resp.body()));

                if (conf.subscriptions.contains(params[1])) {

                    msg.send("已经订阅过了 " + Html.a(feed.getTitle(),feed.getLink())).html().async();

                    return;

                }


                if (!isMainInstance() && !origin.me.id().equals(conf.fromBot)) {

                    conf.fromBot = origin.me.id();

                    if (!conf.subscriptions.isEmpty() || conf.fromBot != null) {

                        msg.send("已将输入源设为本机器人。").async();

                    }

                }

                conf.subscriptions.add(params[1]);


                channel.setById(channelId,conf);

                RssInfo rss = new RssSub.RssInfo();

                rss.id = params[1];
                rss.title = feed.getTitle();
                rss.link = feed.getLink();
                rss.last = FeedFetchTask.generateSign(feed.getEntries().get(0));

                msg.send("订阅成功 : " + Html.a(feed.getTitle(),feed.getLink())).html().async();

            } catch (FeedException e) {

                msg.send("拉取出错 : " + BotLog.parseError(e)).async();

            } catch (UtilException e) {

                msg.send("无效的RSS链接").async();

            }

        } else if (function.endsWith("list")) {

            if (conf.subscriptions.isEmpty()) {

                msg.send("没有订阅任何RSS").async();

                return;

            }

            StringBuilder list = new StringBuilder("订阅列表 :");

            for (String url : conf.subscriptions) {

                RssInfo rss = info.getById(url);
				
                list.append("\n\n").append(Html.b(rss == null ?url : rss.title)).append(" : ").append(Html.code(url));

            }

            msg.send(list.toString()).html().async();

        } else if (function.endsWith("unsub")) {

            if (params.length < 2) {

                msg.invalidParams("频道","链接");

                return;

            }

            if (conf.subscriptions.remove(params[1])) {

                msg.send("取消订阅成功").async();

            } else {

                msg.send("没有订阅这个链接").async();

            }

        } else if (function.endsWith("unsub_all")) {

            if (conf.subscriptions.isEmpty()) {

                msg.send("没有订阅任何源 :)").async();

            } else {

                conf.subscriptions.clear();

                msg.send("已经全部取消 :)").async();

            }

        } else if (function.endsWith("set_format")) {

            if (params.length < 2) {

                msg.invalidParams("频道","1 - 9").async();

                return;

            }

            int target;

            if (!NumberUtil.isNumber(params[1]) || (target = NumberUtil.parseInt(params[1])) < 0 || target > 12) {

                msg.send("请选择有效的格式 : 1 - 12").async();

                return;

            }

            conf.format = target;

            msg.send("修改成功 输出格式为 : " + target + " .").async();

        } else if (function.endsWith("link_preview")) {

            if (params.length < 2) {

                msg.invalidParams("频道","on/off").async();

                return;

            }

            if ("on".equals(params[1])) {

                conf.preview = true;

            } else if ("off".equals(params[1])) {

                conf.preview = false;

            }

            msg.send("修改成功 原文链接预览已设为" + (conf.preview ? "开启" : "关闭") + " .").async();


        }

        channel.setById(conf.id,conf);

    }

}
