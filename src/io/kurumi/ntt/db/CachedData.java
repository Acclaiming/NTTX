package io.kurumi.ntt.db;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CachedData<T> extends Data<T> {

    public HashMap<Long, T> idIndex = new HashMap<>();

    public CachedData(Class<T> clazz) {

        super(clazz);

    }

    public CachedData(String collectionName, Class<T> clazz) {

        super(collectionName, clazz);

    }

    public void saveAll() {

        for (Map.Entry<Long, T> data : idIndex.entrySet()) {

            super.setById(data.getKey(), data.getValue());

        }

    }

    @Override
    public T setById(Long id, T object) {

        synchronized (idIndex) {

            idIndex.put(id,object);

        }

        return super.setById(id, object);

    }

    public T getNoCache(Long id) {

        return super.getById(id);

    }

    @Override
    public T getById(Long id) {

        if (idIndex.size() > 233) {

			saveAll();
			
            idIndex.clear();

        } else if (idIndex.containsKey(id)) return idIndex.get(id);

        synchronized (idIndex) {

            if (idIndex.containsKey(id)) {

                return idIndex.get(id);

            }

            T data = super.getById(id);

            if (data != null) {

                idIndex.put(id, data);
            }

            return data;

        }

    }

}
