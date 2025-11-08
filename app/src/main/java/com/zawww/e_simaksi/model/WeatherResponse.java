package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("current")
    private CurrentWeather current;

    @SerializedName("daily")
    private DailyData daily;

    public CurrentWeather getCurrent() {
        return current;
    }

    public DailyData getDaily() {
        return daily;
    }

    public static class CurrentWeather {
        @SerializedName("temperature_2m")
        private double temperature;

        @SerializedName("relativehumidity_2m")
        private int humidity;

        @SerializedName("windspeed_10m")
        private double windspeed;

        @SerializedName("weathercode")
        private int weathercode;

        public double getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity;
        }

        public double getWindspeed() {
            return windspeed;
        }

        public int getWeathercode() {
            return weathercode;
        }
    }

    public static class DailyData {
        @SerializedName("time")
        private List<String> time;

        @SerializedName("weathercode")
        private List<Integer> weatherCode;

        @SerializedName("temperature_2m_max")
        private List<Double> temperatureMax;

        @SerializedName("temperature_2m_min")
        private List<Double> temperatureMin;

        public List<String> getTime() {
            return time;
        }

        public List<Integer> getWeatherCode() {
            return weatherCode;
        }

        public List<Double> getTemperatureMax() {
            return temperatureMax;
        }

        public List<Double> getTemperatureMin() {
            return temperatureMin;
        }
    }
}
