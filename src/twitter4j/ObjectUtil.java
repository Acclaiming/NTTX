package twitter4j;
import io.kurumi.nttools.twitter.TwiAccount;

public class ObjectUtil {
    
    public static Status parseStatus(String obj,TwiAccount account) {
        
        try {
            
            return new StatusJSONImpl(new JSONObject(obj),account.createConfig());
            
        } catch (TwitterException e) {
            
            throw new RuntimeException(e);
            
        }

    }
    
}
