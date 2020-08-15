package com.techkip.bebadriver.remote;



import com.techkip.bebadriver.model.DataMessage;
import com.techkip.bebadriver.model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * ilitengenezwa na hillarie on 3/17/18.
 */

public interface IFCMService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAALYCUNU8:APA91bEGHEelBeSNXNt7YbvrmZk6eqfsyWQ71MX5yR4gkCpjUehBgJWexoOIEm6SuELZgJlESJInmzL0VZ6WtAjp6cnGgmCHUNXl4-hxtFHwrhQaDeQcra16mEScRtMExWsbLuzo80Em" //key from project setting >cloud messaging >server key.firebase

    })
    @POST("fcm/send")
        Call<FCMResponse> sendMessage(@Body DataMessage body);
}
