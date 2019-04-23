package io.kurumi.ntt.twitter.track;

import cn.hutool.core.util.*;
import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.twitter.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.twitter.stream.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class UTTask extends TimerTask {

    static UTTask INSTANCE = new UTTask();
    static Timer timer;

	private static LinkedHashSet<Long> pedding = new LinkedHashSet<>();

    public static void start() {

        stop();

        timer = new Timer("NTT Twitter User Track Task");
        timer.schedule(INSTANCE,new Date(),5 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();
        timer = null;

    }

    int indexG = 0;
    HashMap<String,Integer> useH = new HashMap<>();

    @Override
    public void run() {

        if (TAuth.auth.isEmpty()) return;

        if (indexG == 15) {

            for (long id : FTTask.enable.toList(Long.class)) {

                List<Long> followers = BotDB.getFollowers(id);
                List<Long> friends = BotDB.getFriends(id);

                if (followers != null) pedding.addAll(followers);
                if (friends != null) pedding.addAll(friends);

            }
            
            indexG = 0;

            useH.clear();

        }

        indexG ++;

        HashMap<Long,List<Long>> subIndex = new HashMap<>();
        HashMap<UserArchive,String> changes = new HashMap<>();

		synchronized (pedding) {

			Collection<JSONArray> values = (Collection<JSONArray>)(Object)subs.values();

			for (JSONArray arr : values) {

				pedding.addAll(arr.toList(Long.class));

			}

		}
        
 

        boolean finished = false;

        try {

            int index = 0;

            while (!finished) {

                if (index > 850) {

                    // 十五分钟user上限900次

                    return;

                }

                LinkedList<String> names;

                synchronized (TAuth.auth) {

                    names = new LinkedList<String>(TAuth.auth.keySet());

                }
                
                for (String id : names) {

                    if (useH.containsKey(id)) {

                        if (useH.get(id) > index) {

                            continue;

                        }

                        useH.put(id,useH.get(id));

                    } else {

                        useH.put(id,1);

                    }


                    Long userId = Long.parseLong(id);

					for (Map.Entry<String,Object> subl : subs.entrySet()) {

						subIndex.put(Long.parseLong(subl.getKey()),((JSONArray)subl.getValue()).toList(Long.class));

					}



                    Twitter api = TAuth.get(userId).createApi();

					List<Long> globals;
                    List<Long> target;

					synchronized (pedding) {

						globals = new LinkedList<Long>(pedding);

					}

					if (globals.size() > 100) {

                        target = globals.subList(0,100);

                        globals = globals.subList(99,globals.size());


                    } else {

                        target = globals;

                        finished = true;

                    }

					synchronized (pedding) {

						pedding.clear();
						pedding.addAll(globals);

					}

                    try {

						ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

                        for (User tuser : result) {

                            target.remove(tuser.getId());

                            UserArchive.save(tuser);

                        }

                        for (Long da : target) {

                            UserArchive.saveDisappeared(da);

                        }

                    } catch (TwitterException e) {

                        if (e.getErrorCode() == 17) {

							for (Long da : target) {

								UserArchive.saveDisappeared(da);

							}

                        } else if (e.getErrorCode() == 89 || e.getErrorCode() == 326) {

							synchronized (pedding) {

								pedding.addAll(target);

							}

							if (finished) finished = false;

							new Send(userId,"对不起！但是乃的认证 " + TAuth.get(userId).getFormatedNameHtml() + " 已无法访问 移除了！ Σ( ﾟω / ").html().exec();

							TAuth.auth.remove(id);

						} else throw e;

					}

				}
            }

            if (changes.isEmpty()) return;

            for (Map.Entry<UserArchive,String> change : changes.entrySet()) {

                UserArchive archive = change.getKey();

                if (!subIndex.containsKey(archive.id)) continue;

                List<Long> subscribers = subIndex.get(archive.id);

                for (Long id : subscribers) {

                    new Send(id,archive.urlHtml(),change.getValue()).html().exec();

                }

            }

        } catch (TwitterException ex) {

            if (ex.getErrorCode() != 130) {

				BotLog.error("UserArchive Failed...",ex);

            }

        }



    }

    public static void onUserChange(UserArchive user,String change) {

		LinkedHashSet<Long> subA = new LinkedHashSet<>();

		LinkedList<Long> subD = new LinkedList<>();

		List<Long> subL = BotDB.getOriginFollowers(user.id);
		List<Long> subR = BotDB.getOriginFriends(user.id);

		if (subL != null) subA.addAll(subL);
		if (subR != null) subA.addAll(subR);

        for (Map.Entry<String,JSONArray> sub : ((Map<String,JSONArray>)(Object)subs).entrySet()) {

            if (sub.getValue().contains(user.id)) {

				long id = Long.parseLong(sub.getKey());

				subA.add(id);
				subD.add(id);

            }

        }

		for (Long sub : subA) {

            if (HideMe.hideList.contains(user.id)) continue;

			if (subL != null && subR != null && subL.contains(sub) && subR.contains(sub)) {

				new Send(sub,"与乃相互关注的 " + user.urlHtml() + " :",change).html().exec();

			} else if (subD.contains(sub)) {

				new Send(sub,"乃订阅的 " + user.urlHtml() + " :",change).html().exec();

			}  else if (subL != null && subL.contains(sub)) {

				new Send(sub,"关注乃的 " + user.urlHtml() + " :",change).html().exec();

			} else if (subR != null && subR.contains(sub)) {

				new Send(sub,"乃关注的 " + user.urlHtml() + " :",change).html().exec();

			}

 		}


    }


    public static JSONObject subs = LocalData.getJSON("data","subscriptions",true);

    public static boolean exists(UserData user) {

        return subs.containsKey(user.id.toString());

    }

    public static List<Long> get(UserData user) {

        if (subs.containsKey(user.id.toString())) {

            return subs.getJSONArray(user.id.toString()).toList(Long.class);

        }

        return null;

    }

    public static boolean add(UserData user,Long id) {

        synchronized (subs) {

            LinkedHashSet<Long> list = subs.containsKey(user.id.toString()) ? new LinkedHashSet<Long>(subs.getJSONArray(user.id.toString()).toList(Long.class)) : new LinkedHashSet<>();

            boolean result = list.add(id);

            subs.put(user.id.toString(),list);

           // SubTask.needReset.set(true);

            return result;

        }

    }

    public static boolean rem(UserData user,Long id) {

        synchronized (subs) {

            LinkedHashSet<Long> list = subs.containsKey(user.id.toString()) ? new LinkedHashSet<Long>(subs.getJSONArray(user.id.toString()).toList(Long.class)) : new LinkedHashSet<>();

            boolean result = list.remove(id);

            subs.put(user.id.toString(),list);

       //     SubTask.needReset.set(true);

            return result;

        }



    }
	
	public static JSONArray remAll(UserData user) {

        synchronized (subs) {

			//     SubTask.needReset.set(true);

           return (JSONArray)subs.remove(user.id.toString());

        }



    }

    public static void save() {

        SubTask.needReset.set(true);
        
        LocalData.setJSON("data","subscriptions",subs);

    }



}
