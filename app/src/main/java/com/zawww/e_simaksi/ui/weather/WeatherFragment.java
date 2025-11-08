package com.zawww.e_simaksi.ui.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.adapter.WeatherAdapter;
import com.zawww.e_simaksi.api.WeatherApiClient;
import com.zawww.e_simaksi.api.WeatherApiService;
import com.zawww.e_simaksi.model.WeatherResponse;
import com.zawww.e_simaksi.util.WeatherIconUtil;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter weatherAdapter;
    private ProgressBar progressBar;
    private TextView tvCurrentTemp, tvCurrentHumidity, tvCurrentWind, tvCurrentWeatherDesc;
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

        // Setup RecyclerView for horizontal scrolling
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        weatherAdapter = new WeatherAdapter();
        weatherRecyclerView.setAdapter(weatherAdapter);

        fetchWeatherData();
    }



    private void fetchWeatherData() {
        progressBar.setVisibility(View.VISIBLE);

        WeatherApiService apiService = WeatherApiClient.getClient().create(WeatherApiService.class);

        // Coordinates for Gunung Buthak
        double latitude = -7.92;
        double longitude = 112.55;
        String dailyParams = "weathercode,temperature_2m_max,temperature_2m_min";
        String currentParams = "temperature_2m,relativehumidity_2m,windspeed_10m,weathercode";

        Call<WeatherResponse> call = apiService.getDailyForecast(latitude, longitude, dailyParams, currentParams, "auto");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherData = response.body();
                    if (weatherData.getCurrent() != null) {
                        updateCurrentWeatherUI(weatherData.getCurrent());
                    }
                    if (weatherData.getDaily() != null && weatherData.getDaily().getTime().size() > 1) {
                        // Exclude today's forecast
                        WeatherResponse.DailyData daily = weatherData.getDaily();
                        daily.getTime().remove(0);
                        daily.getWeatherCode().remove(0);
                        daily.getTemperatureMax().remove(0);
                        daily.getTemperatureMin().remove(0);
                        weatherAdapter.setForecastList(daily);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get weather data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentWeatherUI(WeatherResponse.CurrentWeather current) {
        if (getContext() == null) return;

        tvCurrentTemp.setText(String.format(Locale.getDefault(), "%d°C", (int) current.getTemperature()));
        tvCurrentWeatherDesc.setText(WeatherIconUtil.getWeatherDescription(current.getWeathercode()));
        ivCurrentWeatherIcon.setImageResource(WeatherIconUtil.getWeatherIcon(current.getWeathercode()));

        String humidityText = String.format(Locale.getDefault(), "%d%%\nKelembapan", current.getHumidity());
        tvCurrentHumidity.setText(humidityText);

        String windText = String.format(Locale.getDefault(), "%.1f km/h\nAngin", current.getWindspeed());
        tvCurrentWind.setText(windText);
    }
}
