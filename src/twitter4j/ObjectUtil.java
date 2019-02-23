package twitter4j;

import io.kurumi.ntt.disc.TAuth;

public class ObjectUtil {

    public static Status parseStatus(String obj, TAuth account) {

        try {

            return new StatusJSONImpl(new JSONObject(obj), account.createConfig());

        } catch (TwitterException e) {

            throw new RuntimeException(e);

        }

    }

}
