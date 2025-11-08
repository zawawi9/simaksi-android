package com.zawww.e_simaksi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.WeatherResponse;
import com.zawww.e_simaksi.util.WeatherIconUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {



    private WeatherResponse.DailyData forecastList = null;





    public void setForecastList(WeatherResponse.DailyData forecastList) {

        this.forecastList = forecastList;

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

        if (forecastList == null || forecastList.getTime() == null || forecastList.getTime().isEmpty()) {

            return;

        }



        try {

            // Get data for the specific day

            String dateString = forecastList.getTime().get(position);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date date = inputFormat.parse(dateString);



            // Format the day name (e.g., "Sen", "Sel")

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

            holder.tvDay.setText(dayFormat.format(date));



            // Get weather code and set icon and description

            int weatherCode = forecastList.getWeatherCode().get(position);

            holder.ivWeatherIcon.setImageResource(WeatherIconUtil.getWeatherIcon(weatherCode));

            holder.tvWeatherDescription.setText(WeatherIconUtil.getWeatherDescription(weatherCode));





            // Get and set temperature range

            int maxTemp = forecastList.getTemperatureMax().get(position).intValue();

            int minTemp = forecastList.getTemperatureMin().get(position).intValue();

            String tempText = String.format(Locale.getDefault(), "%d°/%d°", maxTemp, minTemp);

            holder.tvTemperature.setText(tempText);



        } catch (Exception e) {

            e.printStackTrace();

            // Optionally, hide the view or show an error state

            holder.itemView.setVisibility(View.GONE);

        }

    }



    @Override

    public int getItemCount() {

        if (forecastList == null || forecastList.getTime() == null) {

            return 0;

        }

        return forecastList.getTime().size();

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
