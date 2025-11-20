package com.zawww.e_simaksi.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.BuildConfig;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.adapter.WeatherAdapter;
import com.zawww.e_simaksi.api.WeatherApiClient;
import com.zawww.e_simaksi.api.WeatherApiService;
import com.zawww.e_simaksi.model.ForecastResponse;
import com.zawww.e_simaksi.util.WeatherIconUtil;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter weatherAdapter;
    private ProgressBar progressBar;
    private TextView tvCurrentTemp, tvCurrentHumidity, tvCurrentWind, tvCurrentWeatherDesc, tvCityName;
    private ImageView ivCurrentWeatherIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all views
        progressBar = view.findViewById(R.id.weather_progress_bar);
        weatherRecyclerView = view.findViewById(R.id.rv_weather_forecast);
        tvCurrentTemp = view.findViewById(R.id.tv_current_temp);
        tvCurrentHumidity = view.findViewById(R.id.tv_current_humidity);
        tvCurrentWind = view.findViewById(R.id.tv_current_wind);
        tvCurrentWeatherDesc = view.findViewById(R.id.tv_current_weather_desc);
        ivCurrentWeatherIcon = view.findViewById(R.id.iv_current_weather_icon);
        tvCityName = view.findViewById(R.id.tv_city_name);

        // Setup RecyclerView for horizontal scrolling
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        weatherAdapter = new WeatherAdapter();
        weatherRecyclerView.setAdapter(weatherAdapter);

        fetchWeatherData();
    }

    private void fetchWeatherData() {
        progressBar.setVisibility(View.VISIBLE);

        WeatherApiService apiService = WeatherApiClient.getClient().create(WeatherApiService.class);

        String location = "Malang,ID";
        String apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY;
        Log.d("WeatherFragment", "Using API Key: " + apiKey);
        String units = "metric";
        String lang = "id";

        if (apiKey.equals("YOUR_API_KEY") || apiKey.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Please add your OpenWeatherMap API key in gradle.properties", Toast.LENGTH_LONG).show();
            return;
        }

        Call<ForecastResponse> call = apiService.getForecast(location, apiKey, units, lang);

        call.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecastData = response.body();

                    if (forecastData.getCity() != null) {
                        tvCityName.setText(forecastData.getCity().getName());
                    }

                    if (forecastData.getList() != null && !forecastData.getList().isEmpty()) {
                        // Use the first item in the forecast list as the "current" weather
                        updateCurrentWeatherUI(forecastData.getList().get(0));

                        // Pass the full list to the adapter, which will filter it
                        weatherAdapter.setForecastList(forecastData.getList());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get weather data. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentWeatherUI(ForecastResponse.ListItem current) {
        if (getContext() == null || current == null) return;

        tvCurrentTemp.setText(String.format(Locale.getDefault(), "%d°C", (int) current.getMain().getTemp()));

        if (current.getWeather() != null && !current.getWeather().isEmpty()) {
            ForecastResponse.Weather weather = current.getWeather().get(0);
            tvCurrentWeatherDesc.setText(weather.getDescription());
            ivCurrentWeatherIcon.setImageResource(WeatherIconUtil.getWeatherIcon(weather.getId()));
        }

        String humidityText = String.format(Locale.getDefault(), "%d%%\nKelembapan", current.getMain().getHumidity());
        tvCurrentHumidity.setText(humidityText);

        // Convert wind speed from m/s to km/h
        double windSpeedKmh = current.getWind().getSpeed() * 3.6;
        String windText = String.format(Locale.getDefault(), "%.1f km/h\nAngin", windSpeedKmh);
        tvCurrentWind.setText(windText);
    }
}