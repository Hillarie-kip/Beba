package com.techkip.bebarider.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * ilitengenezwa na hillarie on 3/15/18.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}
