package io.kurumi.ntt.model.data;

import io.kurumi.ntt.Env;
import java.lang.reflect.Constructor;

public abstract class AIIdDataModel extends IdDataModel {
    
    public AIIdDataModel(String dirName) {

        super(dirName);
        
        id = -1L;

        init();

    }
    
    public AIIdDataModel(String dirName,long id) { super(dirName,id); }
    
    @Override
    public void save() {
        
        if (id == -1) {

            id = Long.parseLong(Env.getOrDefault("id." + dirName.replace("/","."),"0")) + 1L;

			idStr = id.toString();
			
            Env.set("id." + dirName.replace("/","."),id);

        }
       
        super.save();
        
    }
    
    public static class Factory<T extends AIIdDataModel> extends IdDataModel.Factory<T> {
        
        public Factory(Class<T> clazz, String dirName) { super(clazz,dirName);
        
            try {
                aiidc = clazz.getDeclaredConstructor(new Class[] {String.class});
            } catch (NoSuchMethodException e) {} catch (SecurityException e) {}

        }
        
        public Constructor<T> aiidc;
        
        public T newObj() {
            
            try {

                T obj = aiidc.newInstance(dirName);

                return obj;

            } catch (Exception e) {

                throw new RuntimeException(e);

            }
            
            
        }
        
    }
    
}
