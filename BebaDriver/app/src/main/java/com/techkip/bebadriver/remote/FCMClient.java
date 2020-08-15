package com.techkip.bebadriver.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * ilitengenezwa na hillarie on 3/17/18.
 */

public class FCMClient {
    private static Retrofit retrofit  =null;


    public static Retrofit getClient (String baseURL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        return retrofit;
    }
}


