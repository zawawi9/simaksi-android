package com.zawww.e_simaksi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.ForecastResponse;
import com.zawww.e_simaksi.util.WeatherIconUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<ForecastResponse.ListItem> forecastList = new ArrayList<>();

    public void setForecastList(List<ForecastResponse.ListItem> fullList) {
        this.forecastList.clear();
        List<ForecastResponse.ListItem> dailyForecasts = new ArrayList<>();
        if (fullList != null) {
            // Filter the list to only include forecasts for 12:00 PM each day
            for (ForecastResponse.ListItem item : fullList) {
                if (item.getDt_txt().contains("12:00:00")) {
                    dailyForecasts.add(item);
                }
            }
        }
        // Remove "today" from the list, so it starts from "tomorrow"
        if (!dailyForecasts.isEmpty()) {
            dailyForecasts.remove(0);
        }
        this.forecastList = dailyForecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuaca, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        if (forecastList == null || forecastList.isEmpty()) {
            return;
        }

        try {
            ForecastResponse.ListItem item = forecastList.get(position);

            // Format the day from the timestamp (e.g., "Sen")
            long timestamp = item.getDt();
            Date date = new Date(timestamp * 1000L); // Convert to milliseconds
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("id", "ID"));
            dayFormat.setTimeZone(TimeZone.getDefault());
            holder.tvDay.setText(dayFormat.format(date));

            // Get weather code and set icon and description
            if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                ForecastResponse.Weather weather = item.getWeather().get(0);
                holder.ivWeatherIcon.setImageResource(WeatherIconUtil.getWeatherIcon(weather.getId()));
                // Use description directly from API as it's already in Indonesian
                holder.tvWeatherDescription.setText(weather.getDescription());
            }

            // Get and set temperature (single value)
            int temp = (int) item.getMain().getTemp();
            String tempText = String.format(Locale.getDefault(), "%d°", temp);
            holder.tvTemperature.setText(tempText);

        } catch (Exception e) {
            e.printStackTrace();
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return forecastList != null ? forecastList.size() : 0;
    }

    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTemperature, tvWeatherDescription;
        ImageView ivWeatherIcon;

        WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            tvWeatherDescription = itemView.findViewById(R.id.tv_weather_description);
        }
    }
}