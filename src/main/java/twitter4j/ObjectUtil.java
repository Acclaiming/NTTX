package twitter4j;


import io.kurumi.ntt.twitter.*;

public class ObjectUtil {

	public static String toString(Status status) {
		
		return ((StatusJSONImpl)status).getJSON();
		
	}
	
    public static Status parseStatus(String obj) {

        try {

            return new StatusJSONImpl(new JSONObject(obj));

        } catch (TwitterException e) {

            throw new RuntimeException(e);

        }

    }

}
