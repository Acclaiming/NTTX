package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.Env;

public class StatusFetch extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

        registerFunction("fetch");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (params.length == 0) {

            msg.send("/fetch <用户ID|用户名|链接>").exec();

            return;

        }

				requestTwitter(user,msg);

		}

		@Override
		public int checkTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

				return PROCESS_ASYNC;

		}

		@Override
		public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        Twitter api = account.createApi();

        String target = null;
        long targetL = -1;

        UserArchive archive = null;

        if (NumberUtil.isNumber(params[0])) {

            targetL = NumberUtil.parseLong(params[0]);

        } else {

            target = NTT.parseScreenName(params[0]);

        }

        Msg status = msg.send("正在拉取...").send();

        if (UserArchive.contains(targetL)) {

            archive = UserArchive.get(targetL);

        } else if (UserArchive.contains(target)) {

            archive = UserArchive.get(target);

        }

        boolean accessable = false;

        TwitterException exc = null;

        status.edit("正在检查可用...").exec();

        ResponseList<Status> tl = null;

				if (archive == null) {

						try {

								archive = UserArchive.save(targetL == -1 ? api.showUser(target) : api.showUser(targetL));

								try {

										tl = api.getUserTimeline(archive.id,new Paging().count(200));

										status.edit("检查完成...").exec();

										accessable = true;

								} catch (TwitterException e) {

										exc = e;

								}

						} catch (TwitterException ex) {

								if (ex.getErrorCode() == 136) {

										exc = ex;

								}

						}
				}


        if (!accessable) {

            status.edit("尝试拉取...").exec();

            TAuth accessableAuth = NTT.loopFindAccessable(targetL == -1 ? target : targetL);

            if (accessableAuth == null) {

                if (exc != null) {

                    status.edit("尝试失败",NTT.parseTwitterException(exc)).exec();

                    return;

                } else {

                    status.edit("尝试失败","这个人锁推了...").exec();

                    return;

                }

            }

            api = accessableAuth.createApi();

            if (archive == null) {

                archive = targetL == -1 ? UserArchive.get(target) : UserArchive.get(targetL);

            }

        }

        int count = 0;

        int exists = 0;

        boolean all = params.length > 1 && params[1].equals("--all");

				new Send(Env.GROUP,"对 " + archive.url() + " 的推文拉取由 " + user.userName() + " 执行").html().exec();
				
        try {

            if (tl == null) {

                tl = api.getUserTimeline(archive.id,new Paging().count(200));

            }
            if (tl.isEmpty()) {

                status.edit("这个用户没有发过推文...").exec();

                return;

            }

            long sinceId = -1;

            for (Status s : tl) {

                if (s.getId() < sinceId || sinceId == -1) {

                    sinceId = s.getId();

                }

                if (!all) {

                    if (exists >= 20) {

                        break;

                    }

                    if (StatusArchive.data.containsId(s.getId())) {

                        exists++;

                        count--;

                    } else {

                        exists = 0;

                    }

                }

                StatusArchive.save(s).loop(api);

                count++;

            }

            status.edit("正在拉取中... : ",count + "条推文已拉取").exec();

            w:
            while (!tl.isEmpty()) {

                tl = api.getUserTimeline(archive.id,new Paging().count(200).maxId(sinceId - 1));

                if (exists >= 10) {

                    break w;

                }

                for (Status s : tl) {

                    if (s.getId() < sinceId || sinceId == -1) {

                        sinceId = s.getId();

                    }

                    if (!all) {

                        if (exists >= 10) {

                            break w;

                        }

                        if (StatusArchive.data.containsId(s.getId())) {

                            exists++;

                            count--;

                        } else {

                            exists = 0;

                        }

                    }


                    StatusArchive.save(s).loop(api);

                    count++;

                }


                if (tl.isEmpty()) break;

                status.edit("正在拉取中...",count + "条推文已拉取").exec();

            }

            status.edit("已拉取完成 :",count + "条推文已拉取").exec();

        } catch (TwitterException e) {

            status.edit("拉取失败",NTT.parseTwitterException(e)).exec();

        }

    }

}
