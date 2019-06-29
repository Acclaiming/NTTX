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
import java.io.PrintStream;

public class Shell extends Fragment {

	final String POINT_ADMIN_SHELL = "admin_shell";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("shell");
		registerPoint(POINT_ADMIN_SHELL);

	}

	class Exec implements Runnable {

		public UserData admin;
		public Process process;
		public PrintStream output;
		
		public void start() {
			
			origin.asyncPool.execute(this);
			
		}
		
		@Override
		public void run() {

			final ProcessBuilder processBuilder = new ProcessBuilder("bash");

			processBuilder.directory(new File("/root"));

			processBuilder.redirectErrorStream(true);

			int status = -1;

			try {

				process = processBuilder.start();

				output = new PrintStream(process.getOutputStream(),true);
				
				origin.asyncPool.execute(new Runnable() {

						@Override
						public void run() {

							BufferedReader reader = null;

							try {

								InputStream inputStream = process.getInputStream();

								if (inputStream == null) return;

								reader = new BufferedReader(new InputStreamReader(inputStream));

								StringBuilder line = new StringBuilder();

								int b;
								
								Msg last = null;

								while ((b = reader.read()) != -1) {

									if (last == null) {

										line.append(b);
										
										last = new Send(admin.id,line.toString()).send();

									} else {

									  line.append(b);
										
										last.edit(line.toString()).exec();

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

				new Send(admin.id,"命令行已停止").exec();

			}

			clearPrivatePoint(admin);
			
		}

	}

	@Override
	public void onFunction(final UserData user,Msg msg,String function,String[] params) {

		//if ("shell".equals(function)) {

		setPrivatePoint(user,POINT_ADMIN_SHELL,new Exec() {{

					this.admin = user;

					start();

				}});

		StringBuilder start = new StringBuilder("----------------------------------------\n");	

		File motd = new File("/etc/motd");

		if (motd.isFile()) {

			start.append(FileUtil.readUtf8String(motd));

		}

		msg.send(start.toString(),
				 "----------------------------------------",
				 "      WELCOME TO NTT ROOT SHELL",
				 "----------------------------------------").exec();

		//}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
		Exec exec = (Exec) data;
		
		exec.output.println(msg.text());
		
	}
	

}
