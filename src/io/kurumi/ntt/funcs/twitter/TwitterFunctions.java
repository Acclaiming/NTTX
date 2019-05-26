package io.kurumi.ntt.funcs.twitter;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.funcs.twitter.ext.*;
import io.kurumi.ntt.funcs.twitter.delete.TwitterDelete;

public class TwitterFunctions {
    
    public static void init(BotFragment fragment) {
        
        fragment.addFragment(TrackUI.INSTANCE);
        
        fragment.addFragment(BioSearch.INSTANCE);
        fragment.addFragment(BlockList.INSTANCE);
        fragment.addFragment(StatusGetter.INSTANCE);
        
        fragment.addFragment(TwitterDelete.INSTANCE);
        
    }
    
}
