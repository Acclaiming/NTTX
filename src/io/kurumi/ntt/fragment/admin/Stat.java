package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import tool.sysinfo.linux.cpu.Handler4stat;
import tool.sysinfo.linux.memory.Handler4meminfo;
import cn.hutool.core.thread.ThreadUtil;
import java.lang.management.ManagementFactory;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RuntimeUtil;

public class Stat extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("stat");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        StringBuilder status = new StringBuilder("===== 系统状态 ====");

        status.append("\n运行线程 : " + Thread.activeCount());

        Handler4meminfo mem = new Handler4meminfo(true);

        status.append("\n内存大小 : ").append(mem.shot.getMemTotal() / 1024).append("MB");
        status.append("\n已用内存 : ").append((mem.shot.getMemTotal() - mem.shot.getMemFree() - mem.shot.getBuffers() - mem.shot.getCached()) / 1024).append("MB");
        status.append("\n缓存内存 : ").append((mem.shot.getCached() + mem.shot.getBuffers()) / 1024).append("MB");
        status.append("\n空闲内存 : ").append(mem.shot.getMemFree() / 1024).append("MB");

        Handler4stat handler4stat = new Handler4stat(true);
        long total = handler4stat.getTotalCPUTime();
        long idle = handler4stat.getIdleCPUTime();

		int processId = NumberUtil.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		
        status.append("\nCPU占用 : ").append((100 - ((float) (idle) / total) * 100)).append("%");

		status.append("\n进程ID : ").append(processId);
		
		status.append("\n\nDUMP :\n\n");
		
		status.append(RuntimeUtil.execForStr("jstack -l " + processId));
		
        msg.send(status.toString()).exec();

    }

}
