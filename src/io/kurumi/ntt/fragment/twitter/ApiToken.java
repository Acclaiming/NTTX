package io.kurumi.ntt.fragment.twitter;

import cn.hutool.json.*;
import twitter4j.*;
import twitter4j.conf.*;

public class ApiToken {

		public static final ApiToken twidere = new ApiToken("i5XtSVfoWjLjKlnrvhiLPMZC0","sQncmZ2atQR6tKbqnnAtqjrECqN8k6FD4p4OoNS0XTDkUz3HGH");
		
		/*
		
    public static final ApiToken defaultToken = new ApiToken("u50PRCPUVatU7oN70a1kYdzey","qiPDJAhjbfeNV6TI6dMMPJu48RUpm72p0rfDmcWN2Sr8QyHslr");
		public static final ApiToken androidToken = new ApiToken("F2lOKbef4axdGXpytocR5mn2u","6lIQ3JhEykA1n0aFpT2Fz0Dx5n1yljdfSClk2s799sUlAedVkU");
		public static final ApiToken iPhoneToken = new ApiToken("s45tEGC9syyTx3kHtZLgAcGAY","J8AuGmdzORQfR3xqyFMKKecO4cRm0tCLarveg51uwkrfibSDwl");
		public static final ApiToken webToken = new ApiToken("pSsknvuoiZH7pv0eItLPFLSAc","TFntXmXcvgx3H4lkMH7HeQyB1CGxjSdOtebUETJt5fwJtgmzkJ");

		*/

    public String apiToken;
    public String apiSecToken;

    public ApiToken(String apiToken,String apiSecToken) {
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
