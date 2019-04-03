package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Chat;

public class History extends Fragment {

    @Override
    public boolean onUpdate(UserData user,Update update) {
        
        
        
        if (update.message() != null) {
            
            if (update.message().chat().type() == Chat.Type.Private) {
                
               
                
            }
            
        }
        
        return false;
        
    }
    
}
