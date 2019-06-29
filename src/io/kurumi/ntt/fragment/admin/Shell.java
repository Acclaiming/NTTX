package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import io.kurumi.ntt.utils.BotLog;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.io.FileUtil;

public class Shell extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("shell");

	}

	class Exec extends PointStore.Point {

		public UserData admin;
		public Process process;

		public void start() {

			final ProcessBuilder processBuilder = new ProcessBuilder("bash");

			processBuilder.directory(new File("/root"));

			processBuilder.redirectErrorStream(true);

			int status = -1;

			try {

				process = processBuilder.start();

				origin.asyncPool.execute(new Runnable() {

						@Override
						public void run() {

							BufferedReader reader = null;

							try {

								InputStream inputStream = process.getInputStream();

								if (inputStream == null) return;

								reader = new BufferedReader(new InputStreamReader(inputStream));

								String line;

								int count = 0;
								Msg last = null;

								while ((line = reader.readLine()) != null) {

									count ++;

									if (last == null || count > 20) {

										last = new Send(admin.id,line).send();

									} else {

										last.edit(last.text(),line).exec();

									}

								}

							} catch (Exception e) {

								new Send(admin.id,"命令行出错 :",BotLog.parseError(e)).exec();

							} finally {

								if (reader != null) {

									try {

										reader.close();

									} catch (IOException e) {
									}

								}

							}

						}

					});

				status = process.waitFor();

				new Send(admin.id,"命令行结束 : " + status).exec();

			} catch (IOException e) {

				new Send(admin.id,"命令行出错 :",BotLog.parseError(e)).exec();

			} catch (InterruptedException e) {

				new Send(admin.id,"命令行已停止").send();

			}

			clearPrivatePoint(admin);

			RuntimeUtil.exec("history -c");

		}

	}

	@Override
	public void onFunction(final UserData user,Msg msg,String function,String[] params) {

		if ("shell".equals(function)) {

			setPrivatePoint(user,"admin_shell",new Exec() {{

						this.admin = user;

						start();

					}});

			StringBuilder start = new StringBuilder("----------------------------------------\n");	

			File motd = new File("/etc/motd");

			if (motd.isFile()) {

				start.append(FileUtil.readUtf8String(motd));

			}

			msg.send(
				"----------------------------------------",
				"      WELCOME TO NTT ROOT SHELL",
				"----------------------------------------").exec();

		}

	}

}
