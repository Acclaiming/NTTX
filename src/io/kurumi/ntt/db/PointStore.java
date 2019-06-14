package io.kurumi.ntt.db;

import java.util.*;

import io.kurumi.ntt.fragment.*;

public class PointStore {

    private static HashMap<BotFragment, PointStore> point = new HashMap<>();
    public final BotFragment bot;
    public final HashMap<Long, Point> points = new HashMap<>();
    private PointStore(BotFragment bot) {
        this.bot = bot;
    }

    public static synchronized PointStore getInstance(BotFragment bot) {

        if (point.containsKey(bot)) return point.get(bot);

        PointStore instance = new PointStore(bot);

        point.put(bot, instance);

        return instance;

    }

    public boolean contains(UserData user) {

        return points.containsKey(user.id);

    }

    public <T> Point<T> get(UserData user) {

        if (contains(user)) {

            return (Point<T>) points.get(user.id);

        }

        return null;

    }

    public <T> void set(UserData user, final Type context, final String pointTo, final T content) {

        points.put(user.id, new Point<T>() {{

            type = context;
            point = pointTo;
            data = content;

        }});

    }

    public <T> void set(UserData user, final String pointTo, final T content) {

        points.put(user.id, new Point<T>() {{

            point = pointTo;
            data = content;

        }});

    }

    public <T> Point<T> clear(UserData user) {

        return (Point<T>) points.remove(user.id);

    }

    public enum Type {

        Global, Private, Group;

    }

    public abstract class Point<T> {

        public Type type = Type.Private;

        public String point;

        public T data;

    }

}
