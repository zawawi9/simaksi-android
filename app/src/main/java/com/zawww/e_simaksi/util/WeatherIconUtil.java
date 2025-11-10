package com.zawww.e_simaksi.util;

import com.zawww.e_simaksi.R;

public class WeatherIconUtil {

    public static int getWeatherIcon(int weatherCode) {
        if (weatherCode >= 200 && weatherCode < 300) {
            return R.drawable.ic_hujan; // Thunderstorm -> Hujan
        } else if (weatherCode >= 300 && weatherCode < 400) {
            return R.drawable.ic_hujan; // Drizzle -> Hujan
        } else if (weatherCode >= 500 && weatherCode < 600) {
            return R.drawable.ic_hujan; // Rain -> Hujan
        } else if (weatherCode >= 600 && weatherCode < 700) {
            return R.drawable.ic_weather_snowy; // Snow
        } else if (weatherCode >= 700 && weatherCode < 800) {
            return R.drawable.ic_mendung; // Atmosphere (Mist, Fog, etc) -> Mendung
        } else if (weatherCode == 800) {
            return R.drawable.ic_cerah; // Clear
        } else if (weatherCode > 800 && weatherCode < 900) {
            return R.drawable.ic_mendung; // Clouds -> Mendung
        } else {
            return R.drawable.ic_weather_cloudy; // Default
        }
    }

    public static String getWeatherDescription(int weatherCode) {
        if (weatherCode >= 200 && weatherCode < 300) {
            return "Badai Petir";
        } else if (weatherCode >= 300 && weatherCode < 400) {
            return "Gerimis";
        } else if (weatherCode >= 500 && weatherCode < 600) {
            return "Hujan";
        } else if (weatherCode >= 600 && weatherCode < 700) {
            return "Salju";
        } else if (weatherCode == 701) {
            return "Berkabut";
        } else if (weatherCode == 721) {
            return "Kabut Asap";
        } else if (weatherCode == 741) {
            return "Kabut";
        } else if (weatherCode == 800) {
            return "Cerah";
        } else if (weatherCode == 801) {
            return "Sedikit Berawan";
        } else if (weatherCode == 802) {
            return "Berawan";
        } else if (weatherCode == 803) {
            return "Sangat Berawan";
        } else if (weatherCode == 804) {
            return "Mendung";
        } else {
            return "Tidak Diketahui";
        }
    }
}
