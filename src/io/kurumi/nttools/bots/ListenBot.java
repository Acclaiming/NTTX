package io.kurumi.nttools.bots;

import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.utils.UserData;
import com.pengrad.telegrambot.model.Message;

public class ListenBot extends Fragment {
    
    public ListenBot(MainFragment main) { super(main); }

    @Override
    public String name() {
        
        return "ListenBot";
        
    }


}
