package io.kurumi.ntt.model.data;

import io.kurumi.ntt.BotConf;

public abstract class AIIdDataModel extends IdDataModel {
    
    public AIIdDataModel(String dirName) {

        super(dirName);
        
        id = -1L;

        init();

    }
    
    public AIIdDataModel(String dirName,Long id) { super(dirName,id); }
    
    @Override
    public void save() {
        
        if (id == -1) {

            id = Long.parseLong(BotConf.getOrDefault("id." + dirName.replace("/","."),"0")) + 1L;

			idStr = id.toString();
			
            BotConf.set("id." + dirName.replace("/","."),id);

        }
       
        super.save();
        
    }
    
    public static class Factory<T extends AIIdDataModel> extends IdDataModel.Factory<T> {
        
        public Factory(Class<T> clazz, String dirName) { super(clazz,dirName); }
        
        public T newObj() {
            
            try {

                T obj = clazz.getDeclaredConstructor(new Class[] {String.class}).newInstance(dirName);

                return obj;

            } catch (Exception e) {

                throw new RuntimeException(e);

            }
            
            
        }
        
    }
    
}
