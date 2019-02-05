package io.kurumi.nttools.timer;

import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import java.util.LinkedList;
import cn.hutool.log.StaticLog;

public class TimerThread extends Thread {

    private MainFragment main;
    private long sleep;

    public TimerThread(MainFragment fragment,long sleep) {

        this.main = fragment;
        this.sleep = sleep;

    }

    public LinkedList<TimerTask> tasks = new LinkedList<>();

    @Override
    public void run() {

        try {

            do {

                for (TimerTask task : tasks) {

                    task.run(main);

                }

                sleep(sleep);

            } while(true);

        } catch (InterruptedException e) {
        } catch (Exception ex) {
            
            StaticLog.error(ex);
            
        }

    }

}
