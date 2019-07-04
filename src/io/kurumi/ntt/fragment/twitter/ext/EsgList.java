package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.group.AntiEsu;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.ArrayList;
import java.util.LinkedList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class EsgList extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("esg");

	}

	static ArrayList<Long> processing = new ArrayList<>();

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg);

	}

	@Override
	public int checkTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		return PROCESS_THREAD;

	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		if (processing.contains(user.id)) {

			msg.send("乃有正在处理中的... 请等待完成后再试...").exec();

			return;

		}

		processing.add(user.id);

		Twitter api = account.createApi();

		long target;

		if (params.length == 0) {

			target = account.id;

		} else {

			try {

				target = UserArchive.save(api.showUser(params[0])).id;

			} catch (TwitterException e) {

				msg.send(NTT.parseTwitterException(e)).exec();

				processing.remove(user.id);

				return;

			}

		}

		msg.send("正在检查 这可能需要几(十)分钟的时间 ~").exec();

		LinkedList<Long> foids;

		try {

			foids =  TApi.getAllFoIDs(api,target);

		} catch (TwitterException e) {

			msg.send(NTT.parseTwitterException(e)).exec();

			processing.remove(user.id);

			return;

		}

		LinkedList<Long> esgs = new LinkedList<>();
		StringBuilder esgStr = new StringBuilder();

		for (Long id : foids) {

			if (StatusArchive.data.countByField("from",id) > 50) {

				for (StatusArchive status : StatusArchive.data.findByField("from",id)) {

					if (AntiEsu.keywordMatch(status.text)) {
						
						esgs.add(status.from);
						esgStr.append(status.user().urlHtml()).append(" : ").append(Html.code(status.text)).append("\n");

						if (esgs.size() % 10 == 0) {

							msg.send(esgStr.toString()).html().exec();
							esgStr = new StringBuilder();

						}

						break;

					}

				}


			} else {

				ResponseList<Status> tl;

				try {

					tl = api.getUserTimeline(id,new Paging().count(200));

				} catch (TwitterException e) {

					if (target == account.id) continue;

					TAuth targetAcc = NTT.loopFindAccessable(target);

					if (targetAcc == null) continue;

					try {

						tl = targetAcc.createApi().getUserTimeline(id,new Paging().count(200));

					} catch (TwitterException ex) {

						continue;

					}


				}

				boolean esg = false;

				for (Status status : tl) {

					StatusArchive archive = StatusArchive.save(status);

					if (!esg && AntiEsu.keywordMatch(archive.text)) {

						esg = true;

						esgs.add(archive.from);
						esgStr.append(archive.user().urlHtml()).append(" : ").append(Html.code(archive.text)).append("\n");
						
						if (esgs.size() % 10 == 0) {

							msg.send(esgStr.toString()).html().exec();
							esgStr = new StringBuilder();

						}

					}

				}

			}

		}


		if (esgStr.length() > 0) {

			msg.send(esgStr.toString()).html().exec();

		}

		msg.sendUpdatingFile();

		bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(esgs.toArray(),"\n"))).fileName("EsgList.csv"));

		processing.remove(user.id);

	}

}
