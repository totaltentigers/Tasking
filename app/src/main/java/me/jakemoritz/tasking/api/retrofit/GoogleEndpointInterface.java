package me.jakemoritz.tasking.api.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleEndpointInterface {
    @GET("{id}")
    Call<String> getCoverImageURL(@Path("id") String id, @Query("key") String apiKey);
}
