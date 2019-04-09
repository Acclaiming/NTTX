package io.kurumi.ntt.model.data;

import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import io.kurumi.ntt.utils.BotLog;

public abstract class IdDataModel {

    protected abstract void init();
    protected abstract void load(JSONObject obj);
    protected abstract void save(JSONObject obj);

    protected String dirName;

    public Long id;

	public transient String idStr;

    protected IdDataModel(String dirName) {

        this.dirName = dirName;

    }

    public IdDataModel(String dirName,long id) {

        this(dirName);

        this.id = id;

		idStr = this.id.toString();

        init();
        
        File file = new File(Env.DATA_DIR,dirName + "/" + id + ".json");


        try {

			String json = FileUtil.readUtf8String(file);
            
            JSONObject data = new JSONObject(json);

            load(data);

        } catch (IORuntimeException e) {
        } catch (JSONException e) {
            
            BotLog.error("deleted " + file.toString(),e);
            
            FileUtil.del(file);
            
        }

    }

    void save() {

        FileUtil.writeUtf8String(new JSONObject() {{ save(this); }}.toStringPretty(),new File(Env.DATA_DIR,dirName + "/" + id + ".json"));

    }

    void delete() {

        FileUtil.del(new File(Env.DATA_DIR,dirName + "/" + id + ".json"));

    }

    public static class Factory<T extends IdDataModel> {

        public String dirName;
        public Constructor<T> constructor;

        public LinkedList<Long> idList = new LinkedList<>();
        public HashMap<Long,T> idIndex = new HashMap<>();

        public Factory(Class<T> clazz,String dirName) { this.dirName = dirName;

            try {

                constructor = clazz.getDeclaredConstructor(new Class[] {String.class,long.class});

            } catch (Exception e) {

                throw new RuntimeException(e);

            }

            File[] files = new File(Env.DATA_DIR,dirName).listFiles();

            if (files != null) {

                for (File dataFile : files) {
                    
                    if (!dataFile.isFile()) continue;

                    idList.add(Long.parseLong(StrUtil.subBefore(dataFile.getName(),".json",true)));

                }

            }

        }

        public Boolean exists(long id) {

            return idIndex.containsKey(id);

        }

        public T getNoCache(Long id) {

            if (idIndex.containsKey(id)) return idIndex.get(id);
            
            synchronized (idIndex) {

                if (idIndex.containsKey(id)) return idIndex.get(id);

                try {

                    T obj = constructor.newInstance(dirName,id);

                    return obj;

                } catch (Exception e) {

                    throw new RuntimeException(e);

                }

            }

        }


        public T get(Long id) {
            
            if (idIndex.containsKey(id)) return idIndex.get(id);
            
            synchronized (idIndex) {

                if (idIndex.containsKey(id)) return idIndex.get(id);
                else if (idList.contains(id)) {

                    try {

                        T obj = constructor.newInstance(dirName,id);

                        idIndex.put(obj.id,obj);

                        return obj;

                    } catch (Exception e) {

                        throw new RuntimeException(e);

                    }


                }

            }

            return null;

        }

        public T getOrNew(Long id) {

            if (idIndex.containsKey(id)) return idIndex.get(id);

            synchronized (idIndex) {

                if (idIndex.containsKey(id)) return idIndex.get(id);

                try {

                    T obj = constructor.newInstance(dirName,id);

                    return obj;

                } catch (Exception e) {

                    throw new RuntimeException(e);

                }

            }

        }

        public void saveObj(T obj) {

            obj.save();

            if (!idList.contains(obj)) idList.add(obj.id);

            idIndex.put(obj.id,obj);

        }

        public void delObj(T obj) {

            obj.delete();

            idList.remove(obj.id);

            idIndex.remove(obj.id);

        }

    }

}
