package io.kurumi.ntt.model.data;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.BotConf;
import java.io.File;
import java.util.LinkedList;
import cn.hutool.core.util.StrUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import io.kurumi.ntt.spam.SpamTag;
import java.util.HashMap;

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

    public IdDataModel(String dirName, long id) {

        this(dirName);

        this.id = id;
		
		idStr = this.id.toString();

        init();

        try {

            JSONObject data = new JSONObject(FileUtil.readUtf8String(new File(BotConf.DATA_DIR, dirName + "/" + id + ".json")));

            load(data);

        } catch (IORuntimeException e) {
        }

    }

    public void save() {

        FileUtil.writeUtf8String(new JSONObject() {{ save(this); }}.toStringPretty(), new File(BotConf.DATA_DIR, dirName + "/" + id + ".json"));

    }

    public void delete() {

        FileUtil.del(new File(BotConf.DATA_DIR, dirName + "/" + id + ".json"));

    }

    public static class Factory<T extends IdDataModel> {

        protected Class<T> clazz;
        protected String dirName;

        public HashMap<Long,T> idIndex = new HashMap<>();

        public Factory(Class<T> clazz, String dirName) { this.clazz = clazz;this.dirName = dirName;

            File[] files = new File(BotConf.DATA_DIR, dirName).listFiles();

            if (files != null) {

                for (File dataFile : files) {

                    try {

                        T obj = clazz.getDeclaredConstructor(new Class[] {String.class,long.class}).newInstance(dirName,Long.parseLong(StrUtil.subBefore(dataFile.getName(), ".json", true)));

                        saveObj(obj);

                    } catch (Exception e) {
                        
                        throw new RuntimeException(e);
                        
                    }

                }

            }

        }
        
        public LinkedList<T> all() {
            
            return new LinkedList<T>() {{ addAll(idIndex.values()); }};
            
        }
        
        public T get(Long id) {

            if (idIndex.containsKey(id)) return idIndex.get(id);
            
            return null;
        
        }
        
        public T getOrNew(Long id) {
            
            if (idIndex.containsKey(id)) return idIndex.get(id);
            
            try {
                
                T obj = clazz.getDeclaredConstructor(new Class[] {String.class,long.class}).newInstance(dirName, id);

                saveObj(obj);
                
                return obj;

            } catch (Exception e) {
                
                throw new RuntimeException(e);
                
            }

        }

        public void saveObj(T obj) {

            obj.save();
            
            idIndex.put(obj.id, obj);

        }
        
        public void delObj(T obj) {
            
            obj.delete();
            
            idIndex.remove(obj.id);
            
        }

    }

}
