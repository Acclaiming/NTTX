package io.kurumi.ntt.fragment.group;

import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Img;
import io.kurumi.ntt.utils.NTT;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class JoinCaptcha extends Fragment {

		public static JoinCaptcha INSTANCE;

		final String POINT_AUTH = "join_auth";
		final String POINT_INTERFERE = "join_interfere";
		final String POINT_ANSWER = "join_answer";
		final String POINT_ACC = "join_acc";
		final String POINT_REJ = "join_rej";
		final String POINT_DELETE = "join_del";

    HashMap<Long, HashMap<Long, AuthCache>> cache = new HashMap<>();

		public void stop() {

				for (Map.Entry<Long,HashMap<Long,AuthCache>> g : cache.entrySet()) {

						GroupData data = GroupData.data.getById(g.getKey());

						for (Map.Entry<Long,AuthCache> c : g.getValue().entrySet()) {

								AuthCache auth = c.getValue();

								final UserData user = UserData.get(c.getKey());

								auth.task.cancel();

								if (auth.serviceMsg != null)  {

										auth.serviceMsg.delete();

										auth.serviceMsg.restrict(user.id);


										if (data.passive_msg != null && data.passive_msg.containsKey(user.id.toString())) {

												int id = data.passive_msg.remove(user.id.toString());

												execute(new DeleteMessage(data.id,id));

										}

										if (data.captchaFailed != null) {

												data.captchaFailed.remove(user.id.toString());

												if (data.captchaFailed.isEmpty()) data.captchaFailed = null;

										}

										SendResponse resp = new Send(this,data.id,"你好，新成员 " + user.userName(),"因为服务中断，已将你暂时禁言。请点击下方重新验证。")
												.buttons(new ButtonMarkup() {{

																newButtonLine("开始验证",POINT_AUTH,user.id);

																newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

														}}).html().exec();

										if (resp.isOk()) {

												if (data.passive_msg == null) data.passive_msg = new HashMap<>();
												data.passive_msg.put(user.id.toString(),resp.message().messageId());

										}

								}

						}

				}

		}

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				INSTANCE = this;

				registerPoints(POINT_AUTH,POINT_DELETE,POINT_ANSWER,POINT_ACC,POINT_REJ);

		}

		@Override
		public int checkMsg(UserData user,Msg msg) {

				if (!msg.isSuperGroup()) return PROCESS_ASYNC;

				return msg.message().newChatMembers() != null ? PROCESS_SYNC : PROCESS_ASYNC;

		}

		@Override
		public void onMsg(final UserData user,Msg msg) {

				if (!msg.isSuperGroup()) return;

				GroupData data = GroupData.get(msg.chat());

				if (data.join_captcha == null) return;

				if (msg.message().newChatMembers() != null) {

            User newMember = msg.message().newChatMembers()[0];

						final UserData newData = UserData.get(newMember);

						if (user.admin() || msg.isGroupAdmin()) return;

						if (!user.id.equals(newData.id)) return;

						if (data.waitForCaptcha == null) {

								data.waitForCaptcha = new ArrayList<>();

								data.waitForCaptcha.add(user.id);

						} else if (!data.waitForCaptcha.contains(user.id)) {

								data.waitForCaptcha.add(user.id);

						}

						msg.restrict();

						if (data.passive_mode != null) {

								if (data.delete_service_msg != null) {

										SendResponse resp = msg.send("你好，新成员 " + newData.userName() + " 为确保群组安全，已将你暂时禁言。请点击下方按钮开始验证。")
												.buttons(new ButtonMarkup() {{

																newButtonLine("开始验证",POINT_AUTH,user.id);

																newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

														}}).html().exec();

										if (resp.isOk()) {

												if (data.passive_msg == null) data.passive_msg = new HashMap<>();
												data.passive_msg.put(user.id.toString(),resp.message().messageId());

										}




								} else {

										SendResponse resp = msg.reply("新成员你好，为确保群组安全，已将你暂时禁言。请点击下方按钮开始验证。")
												.buttons(new ButtonMarkup() {{

																newButtonLine("开始验证",POINT_AUTH,user.id);

																newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

														}}).exec();

										if (resp.isOk()) {

												if (data.passive_msg == null) data.passive_msg = new HashMap<>();
												data.passive_msg.put(user.id.toString(),resp.message().messageId());

										}

								}

								return;

						}

						startAuth(user,msg,data,null);

				} else if (data.waitForCaptcha != null && data.waitForCaptcha.contains(user.id)) {

						data.waitForCaptcha.remove(user.id);

						// 管理员取消 禁言

				}

		}

		class AuthCache extends PointData {

				UserData user;
				Msg serviceMsg;

				boolean input;
				VerifyCode code;

				TimerTask task;

		}

	  static abstract class VerifyCode {

				public final boolean input;

				public VerifyCode(boolean input) {

						this.input = input;

				}

				public abstract String question();
				public abstract String code();

				public abstract String validCode();
				public abstract String[] invalidCode();
				public abstract boolean verify(String input);

				public abstract VerifyCode fork();

		}

		static class BaseCode extends VerifyCode {

				public BaseCode(boolean input) { super(input); }

				boolean code = RandomUtil.randomBoolean();

				@Override
				public VerifyCode fork() {

						return new BaseCode(input);

				}

				@Override
				public String question() {

						return "请" + (input ? "发送" : "选择") + " " + (code ? "喵" : "嘤") + " 以通过验证 ~";

				}

				@Override
				public String code() {

						return null;

				}

				@Override
				public String validCode() {

						return code ? "喵" : "嘤";

				}

				@Override
				public String[] invalidCode() {

						return new String[] { code ? "嘤" : "喵" };

				}

				@Override
				public boolean verify(String input) {

						return code ? input.contains("喵") : (input.contains("嘤") || input.contains("嚶"));

				}

		}

		static class MathCode extends VerifyCode {

				public MathCode(boolean input) { super(input); }

				int left = RandomUtil.randomInt(101);
				int right = RandomUtil.randomInt(101);

				int type = RandomUtil.randomInt(2);

				@Override
				public String question() {

						return "请" + (input ? "发送" : "选择") + " 答案以通过验证 ~";

				}

				@Override
				public VerifyCode fork() {

						return new MathCode(input);

				}

				String typeCode() {

						switch (type) {

								case 0 : return "加";
							  default : return "减";

						}

				}

				@Override
				public String code() {

						return left + " " + typeCode() + " " + right;

				}

				@Override
				public String validCode() {

						return (type == 0 ? left + right : left - right) + "";

				}

				@Override
				public String[] invalidCode() {

						return new String[] {

								RandomUtil.randomInt(-100,101) + "",
								RandomUtil.randomInt(-100,101) + "",
								RandomUtil.randomInt(-100,101) + "",
								RandomUtil.randomInt(-100,101) + ""

						};

				}

				@Override
				public boolean verify(String input) {

						try {

								return (type == 0 ? left + right : left - right) == NumberUtil.parseInt(input.trim());

						} catch (Exception ex) { return false; }

				}

		}

		static class StringCode extends VerifyCode {

				public StringCode(boolean input) { super(input); }

				RandomGenerator gen = new RandomGenerator("苟利国家生死以岂因祸福避趋之",5);

			  String code = gen.generate();

				@Override
				public String question() {

						return "请" + (input ? "发送" : "选择") + " 验证码以通过验证 ~";

				}

				@Override
				public VerifyCode fork() {

						return new StringCode(input);

				}

				@Override
				public String code() {

						return code;

				}

				@Override
				public String validCode() {

						return code;

				}


				@Override
				public String[] invalidCode() {

						return new String[] {

								gen.generate(),
								gen.generate(),
								gen.generate(),
								gen.generate()

						};

				}

				@Override
				public boolean verify(String input) {

						return code.equals(input.trim().replace("國","国"));

				}

		}

		static class CustomCode extends VerifyCode {

				private GroupData data;

				List<String> validCode = new ArrayList<>();
				List<String> invalidCode = new ArrayList<>();

				public CustomCode(boolean input,GroupData data) {

						super(input);

						this.data = data;

						for (GroupData.CustomItem item : data.custom_items) {

								if (item.isValid) validCode.add(item.text);
								else invalidCode.add(item.text);

						}

				}

				@Override
				public String question() {

						return input ? data.custom_a_question : data.custom_i_question;

				}

				@Override
				public String code() {

						return null;

				}

				@Override
				public String validCode() {

						return null;

				}

				String[] codes;

				@Override
				public String[] invalidCode() {

						String[] codes = new String[validCode.size() + invalidCode.size()];

						int index = 0;

						for (String code : validCode) {

								codes[index] = code;

								index ++;

						}

						for (String code : invalidCode) {

								codes[index] = code;

								index ++;

						}

						return codes;

				}

				@Override
				public boolean verify(String text) {

						if (input) {

								for (String kw : data.custom_kw) {

										if (text.contains(kw)) return true;

								}

								return false;

						} else {

								return validCode.contains(text);

						}

				}

				@Override
				public VerifyCode fork() {

						return this;

				}




		}

		void startAuth(final UserData user,final Msg msg,final GroupData data,VerifyCode left) {

				final HashMap<Long, AuthCache> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, AuthCache>();

				setGroupPoint(user,POINT_DELETE);

				final VerifyCode code;

				if (left != null) {

						code = left.fork();

				} else if (data.captcha_mode == null) {

						code = new BaseCode(data.require_input != null);

				} else if (data.captcha_mode == 0) {

						code = new StringCode(data.require_input != null);

				} else if (data.captcha_mode == 1) {

						code = new MathCode(data.require_input != null);

				} else {

						code = new CustomCode(data.require_input != null,data);

				}

				ButtonMarkup buttons = new ButtonMarkup() {{

								if (data.interfere != null) {

										newButtonLine()
												.newButton("□",POINT_INTERFERE,user.id)
												.newButton("□",POINT_INTERFERE,user.id)
												.newButton("□",POINT_INTERFERE,user.id)
												.newButton("□",POINT_INTERFERE,user.id);

								}

								if (data.require_input == null) {

										List<String> all = new LinkedList<>();

										for (String interfere : code.invalidCode()) all.add(interfere);

										if (code.validCode() != null) all.add(code.validCode());

										Collections.shuffle(all);

										for (String show : all) newButtonLine(show,POINT_ANSWER,user.id,show);

								}

								newButtonLine()
										.newButton("通过",POINT_ACC,user.id)
										.newButton("滥权",POINT_REJ,user.id);

								if (data.interfere != null) {

										newButtonLine()
												.newButton("■",POINT_INTERFERE,user.id)
												.newButton("■",POINT_INTERFERE,user.id)
												.newButton("■",POINT_INTERFERE,user.id)
												.newButton("■",POINT_INTERFERE,user.id);

								}

						}};


				final AuthCache auth = new AuthCache();

				auth.user = user;

				auth.input = data.require_input != null;
				auth.code = code;

				if (data.with_image == null) {

						if (auth.input) {

								msg.unrestrict(user.id);

								setGroupPoint(user,POINT_ANSWER,auth);

						} else {

								clearGroupPoint(user);

						}

						if (code.code() == null) {

								if (left != null) {

										auth.serviceMsg =  msg.send(user.userName() + " 请重试","\n" + code.question()).buttons(buttons).html().send();

								} else {

										auth.serviceMsg =  msg.send(user.userName() + " 请验证","\n" + code.question()).buttons(buttons).html().send();


								}

						} else {

								if (left != null) {

										auth.serviceMsg =  msg.send(user.userName() + " 请重试","\n" + code.question(),"\n" + code.code()).buttons(buttons).html().send();

								} else {

										auth.serviceMsg =  msg.send(user.userName() + " 请验证","\n" + code.question(),"\n" + code.code()).buttons(buttons).html().send();


								}

						}

						if (auth.serviceMsg == null) return;

						clearGroupPoint(user);

					  AuthCache old = group.put(user.id,auth);

						if (old != null) {

								old.serviceMsg.delete();
								old.task.cancel();

						}

				} else {

						Img info = new Img(1000,600,Color.WHITE);

						info.drawLineInterfere(50);

						info.font("Noto Sans CJK SC Thin",39);

						if (code.code() != null) {

								info.drawRandomColorTextCenter(0,0,0,400,left == null ? "请验证 :)" : "请重试 :)");
								info.drawRandomColorTextCenter(0,200,0,200,code.code());
								info.drawRandomColorTextCenter(0,400,0,0,code.question());

						} else {

								info.drawRandomColorTextCenter(0,0,0,300,left == null ? "请验证 :)" : "请重试 :)");
								info.drawRandomColorTextCenter(0,300,0,0,code.question());

						}

						SendResponse resp = execute(new SendPhoto(msg.chatId(),info.getBytes()).caption(user.userName()).parseMode(ParseMode.HTML).replyMarkup(buttons.markup()));

						if (resp != null && resp.isOk()) {

								auth.serviceMsg = new Msg(this,resp.message());

						}

						if (auth.serviceMsg == null) {

								clearGroupPoint(user);

						}

						if (auth.input) {

								msg.unrestrict(user.id);

								setGroupPoint(user,POINT_ANSWER,auth);

						} else {

								clearGroupPoint(user);

						}

						AuthCache old = group.put(user.id,auth);

						if (old != null) {

								old.serviceMsg.delete();
								old.task.cancel();

						}


				}

				auth.task = new TimerTask() {

						@Override
						public void run() {

								final HashMap<Long, AuthCache> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, AuthCache>();

								if (!group.containsKey(user.id)) return;

								failed(user,msg,auth,data,true);

						}

				};

				if (group.isEmpty()) cache.remove(msg.chatId());
				else cache.put(msg.chatId(),group);

				BotFragment.mainTimer.schedule(auth.task,new Date(System.currentTimeMillis() + ((data.captcha_time == null ? 50 : data.captcha_time) * 1000)));

		}

		@Override
		public void onPoint(final UserData user,Msg msg,String point,PointData data) {

				if (POINT_DELETE.equals(point)) { msg.delete(); return; }

				final GroupData gd = GroupData.get(msg.chat());
				final AuthCache auth = (AuthCache)data;

				if (msg.message().leftChatMember() != null) {

						failed(user,msg,auth,gd);

						return;

				} else if (msg.message().newChatMembers() != null && msg.message().newChatMembers().length != 0) {

						User newMember = msg.message().newChatMembers()[0];

						if (user.id.equals(newMember.id())) {

								startAuth(user,msg,gd,null);

								return;

						}

						msg.kick(newMember.id());

						if (newMember.isBot()) {

								if (gd.invite_bot_ban != null) {

										msg.kick(user.id,true);

								} else {

										msg.kick();

								}

						} else {

								if (gd.invite_user_ban != null) {

										msg.kick(user.id,true);

								} else {

										msg.kick();

								}

						}

						failed(user,msg,auth,gd,true);

						return;

				}

				if (auth.code.verify(msg.text())) {

						success(user,msg,auth,gd);

				} else {

						failed(user,msg,auth,gd);

				}

		}

		@Override
		public void onCallback(final UserData user,Callback callback,String point,String[] params) {

				long target = NumberUtil.parseInt(params[0]);

				if (POINT_AUTH.equals(point)) {

						if (!user.id.equals(target)) {

								callback.alert("这个验证不针对你。");

								return;

						}

						callback.delete();

						startAuth(user,callback,GroupData.get(callback.chat()),null);

						return;

				} 

				final HashMap<Long, AuthCache> group = cache.containsKey(callback.chatId()) ? cache.get(callback.chatId()) : new HashMap<Long, AuthCache>();
				final GroupData gd = GroupData.get(callback.chat());
				AuthCache auth = group.get(user.id);

				if (POINT_INTERFERE.equals(point)) {

						if (!user.id.equals(target)) {

								callback.alert("这个验证不针对你。");

								return;

						}

						failed(user,callback,auth,gd);

				} else if (POINT_ACC.equals(point) || POINT_REJ.equals(point)) {

						if (user.id.equals(target)) {

								failed(user,callback,auth,gd);

						} else if (NTT.checkGroupAdmin(callback)) {

								return;

						}

						success(UserData.get(target),callback,auth,gd);

				} else if (POINT_ANSWER.equals(point)) {

						if (!user.id.equals(target)) {

								callback.alert("这个验证不针对你。");

								return;

						}
						
						
						if (auth == null) {

								callback.delete();

								callback.restrict(user.id);

								callback.send(user.userName() + " , 验证丢失。 你可以重试 :)")
										.buttons(new ButtonMarkup() {{

														newButtonLine("重新验证",POINT_AUTH,user.id);

														newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

												}}).html().exec();

								return;

						}

						if (auth.code.verify(params[1])) {

								success(user,callback,auth,gd);

						} else {

								failed(user,callback,auth,gd);

						}

				}

		}

		void success(UserData user,Msg msg,AuthCache auth,GroupData gd) {

				if (cache.containsKey(msg.chatId())) {

						AuthCache rdc = cache.get(msg.chatId()).remove(user.id);

						if (auth == null) auth = rdc;

				}

				if (auth != null) {

						if (auth.serviceMsg != null) auth.serviceMsg.delete();

						auth.task.cancel();

				}

				msg.delete();

				gd.waitForCaptcha.remove(user.id);

				msg.unrestrict(user.id);

				if (!(msg instanceof Callback)) {

						clearGroupPoint(user);

				}

				if (gd.captcha_del == null && gd.last_join_msg != null) {

						execute(new DeleteMessage(msg.chatId(),gd.last_join_msg));

						gd.last_join_msg = null;

				}

				if (gd.captcha_del == null) {

						Msg lastMsg = msg.send(user.userName() + " 通过了验证 ~").html().send();

						if (lastMsg != null) {

								gd.last_join_msg = lastMsg.messageId();

						}

				} else if (gd.captcha_del == 0) {

						msg.send(user.userName() + " 通过了验证 ~").html().failed();

				} else {

						msg.send(user.userName() + " 通过了验证 ~").html().exec();

				}


		}

		void failed(UserData user,Msg msg,AuthCache auth,GroupData gd) {

				failed(user,msg,auth,gd,false);

		}

		void failed(UserData user,Msg msg,AuthCache auth,GroupData gd,boolean noRetey) {

				if (cache.containsKey(msg.chatId())) {

						AuthCache rdc = cache.get(msg.chatId()).remove(user.id);

						if (auth == null) auth = rdc;

				}

				msg.delete();

				if (auth != null) {

						if (auth.serviceMsg != null) auth.serviceMsg.delete();

						auth.task.cancel();

				}

				if (!noRetey && gd.ft_count != null && (gd.captchaFailed == null || !gd.captchaFailed.containsKey(user.id.toString()) || (gd.captchaFailed.get(user.id.toString()) <= gd.ft_count))) {

						if (gd.captchaFailed == null) {

								gd.captchaFailed = new HashMap<>();

								gd.captchaFailed.put(user.id.toString(),1);

						} else if (!gd.captchaFailed.containsKey(user.id.toString())) {

								gd.captchaFailed.put(user.id.toString(),1);

						} else {

								gd.captchaFailed.put(user.id.toString(),gd.captchaFailed.get(user.id.toString()) + 1);

						}

						startAuth(user,msg,gd,auth != null ? auth.code : null);

						return;

				}

				if (msg.message().leftChatMember() != null) {
				} else if (msg.message().newChatMembers() != null) {
				} else if (gd.fail_ban == null) {

						gd.waitForCaptcha.remove(user.id);

						msg.kick(user.id);

				} else {

						gd.waitForCaptcha.remove(user.id);

						msg.kick(user.id,true);

				}

				if (gd.captcha_del == null && gd.last_join_msg != null) {

						execute(new DeleteMessage(msg.chatId(),gd.last_join_msg));

						gd.last_join_msg = null;

				}

				if (gd.captcha_del == null) {

						Msg lastMsg = msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().send();

						if (lastMsg != null) {

								gd.last_join_msg = lastMsg.messageId();

						}

				} else if (gd.captcha_del == 0) {

						msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().failed();

				} else {

						msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().exec();

				}

				if (gd.passive_msg != null && gd.passive_msg.containsKey(user.id.toString())) {

						int id = gd.passive_msg.remove(user.id.toString());

						execute(new DeleteMessage(msg.chatId(),id));

						if (gd.passive_msg.isEmpty()) gd.passive_msg = null;

				}

				if (gd.captchaFailed != null) {

						gd.captchaFailed.remove(user.id.toString());

						if (gd.captchaFailed.isEmpty()) gd.captchaFailed = null;

				}

		}

}
