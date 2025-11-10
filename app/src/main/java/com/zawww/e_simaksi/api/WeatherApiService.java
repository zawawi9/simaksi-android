package com.zawww.e_simaksi.api;

import com.zawww.e_simaksi.model.ForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("data/2.5/forecast")
    Call<ForecastResponse> getForecast(
            @Query("q") String location,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}
