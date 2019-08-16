package io.kurumi.ntt.db;

import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;

public class PointStore {

    private static HashMap<BotFragment, PointStore> point = new HashMap<>();

    public final BotFragment bot;

    public final HashMap<Long, PointData> privatePoints = new HashMap<>();
    public final HashMap<Long, PointData> groupPoints = new HashMap<>();

    private PointStore(BotFragment bot) {
        this.bot = bot;
    }

    public static synchronized PointStore getInstance(BotFragment bot) {

        if (point.containsKey(bot)) return point.get(bot);

        PointStore instance = new PointStore(bot);

        point.put(bot,instance);

        return instance;

    }

    public boolean containsPrivate(Long userId) {

        return privatePoints.containsKey(userId);

    }

    public boolean containsGroup(Long userId) {

        return groupPoints.containsKey(userId);

    }

    public PointData getPrivate(Long userId) {

        if (containsPrivate(userId)) {

            return privatePoints.get(userId);

        }

        return null;

    }

    public PointData getGroup(long userId) {

        if (containsGroup(userId)) {

            return groupPoints.get(userId);

        }

        return null;

    }

    public PointData setPrivate(Long userId,final String pointTo,final PointData data) {

        data.point = pointTo;
        data.type = 1;

        privatePoints.put(userId,data);

        return data;

    }


    public PointData setPrivateData(Long userId,Msg command,final String pointTo,final Object content) {

        PointData pointData = new PointData(command);

		pointData.point = pointTo;
		pointData.data = content;

		pointData.type = 1;

        privatePoints.put(userId,pointData);

        return pointData;

    }

    public PointData setGroup(Long userId,final String pointTo,final PointData data) {

        data.type = 2;
        data.point = pointTo;

        groupPoints.put(userId,data);

        return data;

    }


    public PointData setGroupData(Long userId,Msg command,final String pointTo,final Object content) {

        PointData pointData = new PointData(command);

		pointData.point = pointTo;
		pointData.data = content;
		
		
        groupPoints.put(userId,pointData);

        return pointData;

    }


    public PointData clearPrivate(Long userId) {

        return privatePoints.remove(userId);

    }

    public PointData clearGroup(Long userId) {

        return groupPoints.remove(userId);

    }

}
