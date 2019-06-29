package io.kurumi.ntt.db;

import java.util.*;

import io.kurumi.ntt.fragment.*;

public class PointStore {

    private static HashMap<BotFragment, PointStore> point = new HashMap<>();

    public final BotFragment bot;
	
    public final HashMap<Long, Point> privatePoints = new HashMap<>();
	public final HashMap<Long, Point> groupPoints = new HashMap<>();
	
    private PointStore(BotFragment bot) {
        this.bot = bot;
    }

    public static synchronized PointStore getInstance(BotFragment bot) {

        if (point.containsKey(bot)) return point.get(bot);

        PointStore instance = new PointStore(bot);

        point.put(bot, instance);

        return instance;

    }

    public boolean containsPrivate(UserData user) {

        return privatePoints.containsKey(user.id);

    }
	
	public boolean containsGroup(UserData user) {

        return groupPoints.containsKey(user.id);

    }

    public Point getPrivate(UserData user) {

        if (containsPrivate(user)) {

            return  privatePoints.get(user.id);

        }

        return null;

    }

	public Point getGroup(UserData user) {

        if (containsGroup(user)) {

            return  groupPoints.get(user.id);

        }

        return null;

    }
	
	
    public void setPrivate(UserData user, final String pointTo, final Object content) {

        privatePoints.put(user.id, new Point() {{

            point = pointTo;
            data = content;

        }});

    }
	
	public void setGroup(UserData user, final String pointTo, final Object content) {

        groupPoints.put(user.id, new Point() {{

					point = pointTo;
					data = content;

				}});

    }
	

    public Point clearPrivate(UserData user) {

        return  privatePoints.remove(user.id);

    }

	public Point clearGroup(UserData user) {

        return  groupPoints.remove(user.id);

    }
	
	
    public static abstract class Point {

        public String point;

        public Object data;
		
		public void onCancel() {}

    }

}
