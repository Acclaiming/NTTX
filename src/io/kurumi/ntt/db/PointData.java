package io.kurumi.ntt.db;

import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;

import java.util.Collections;
import java.util.LinkedList;

public class PointData {

    public int type;

    public int step = 0;

    private LinkedList<Msg> context = new LinkedList<>();

    public String point;
    public Object data;
    public boolean cancelable = true;

    public Msg command;

    public PointData() {
    }

    public PointData(Msg command) {
        this.command = command;
    }

    public PointData with(Msg msg) {

        context.add(msg);

        return this;

    }

    public <T> T data() {

        return (T) data;

    }

    public void onFinish() {

        BotFragment.waitPool.execute(new Runnable() {

            @Override
            public void run() {

                Collections.reverse(context);

                for (Msg toDelete : context) toDelete.delete();

            }
        });

    }

    public void onCancel(UserData user, Msg msg) {

        if (command != null) command.delete();

    }

}
