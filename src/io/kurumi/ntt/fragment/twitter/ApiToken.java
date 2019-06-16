package io.kurumi.ntt.fragment.twitter;

import cn.hutool.json.*;
import twitter4j.*;
import twitter4j.conf.*;

public class ApiToken {

    public static final ApiToken defaultToken = new ApiToken("u50PRCPUVatU7oN70a1kYdzey", "qiPDJAhjbfeNV6TI6dMMPJu48RUpm72p0rfDmcWN2Sr8QyHslr");

    public String apiToken;
    public String apiSecToken;

    public ApiToken(String apiToken, String apiSecToken) {
        this.apiToken = apiToken;
        this.apiSecToken = apiSecToken;
    }

    public Configuration createAppOnlyConfig() {

        return new ConfigurationBuilder()
                .setOAuthConsumerKey(apiToken)
                .setOAuthConsumerSecret(apiSecToken)
                .setApplicationOnlyAuthEnabled(true)
                .build();

    }

    public Configuration createConfig() {

        return new ConfigurationBuilder()
                .setOAuthConsumerKey(apiToken)
                .setOAuthConsumerSecret(apiSecToken)
                .build();

    }

    public Twitter createAppOnlyApi() {

        return new TwitterFactory(createAppOnlyConfig()).getInstance();

    }

    public Twitter createApi() {

        return new TwitterFactory(createConfig()).getInstance();

    }

}
