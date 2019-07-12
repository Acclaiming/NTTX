package io.kurumi.ntt.db;

import java.util.HashMap;
import java.util.Map;

public class CachedData<T> extends Data<T> {

	public HashMap<Long, T> idIndex = new HashMap<>();

	public CachedData(Class<T> clazz) {

        super(clazz);

    }

    public CachedData(String collectionName,Class<T> clazz) {

        super(collectionName,clazz);

    }
	
	public void saveAll() {
		
		for (Map.Entry<Long,T> data : idIndex.entrySet()) {
			
			super.setById(data.getKey(),data.getValue());
			
		}
		
	}

	@Override
	public T setById(Long id,T object) {
		
		synchronized (idIndex) {
			
			idIndex.remove(id);
			
		}
		
		return super.setById(id,object);
		
	}
	
	@Override
	public T getById(Long id) {

		if (idIndex.size() > 1000) {

			idIndex.clear();

		} else if (idIndex.containsKey(id)) return idIndex.get(id);

		synchronized (idIndex) {

			if (idIndex.containsKey(id)) {

				return idIndex.get(id);

			}

			T data = super.getById(id);

			if (data != null) {

				idIndex.put(id,data);
			}

			return data;

		}

	}

}
