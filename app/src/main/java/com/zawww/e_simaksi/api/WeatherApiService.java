package com.zawww.e_simaksi.api;

import com.zawww.e_simaksi.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("v1/forecast")
    Call<WeatherResponse> getDailyForecast(
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("daily") String daily,
        @Query("current") String current,
        @Query("timezone") String timezone
    );
}
