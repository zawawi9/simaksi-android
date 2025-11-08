package com.zawww.e_simaksi.util;

import com.zawww.e_simaksi.R;

public class WeatherIconUtil {

    public static int getWeatherIcon(int weatherCode) {
        if (weatherCode == 0) {
            return R.drawable.ic_cerah;
        } else if (weatherCode >= 1 && weatherCode <= 3) {
            return R.drawable.ic_mendung;
        } else if (weatherCode == 45 || weatherCode == 48) {
            return R.drawable.ic_mendung; // Menggunakan ikon mendung untuk kabut
        } else if ((weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82) || (weatherCode >= 95 && weatherCode <= 99)) {
            return R.drawable.ic_hujan; // Hujan, Gerimis, Badai
        } else {
            return R.drawable.ic_weather_cloudy; // Ikon default jika tidak ada yang cocok
        }
    }

    public static String getWeatherDescription(int weatherCode) {
        if (weatherCode == 0) {
            return "Cerah";
        } else if (weatherCode == 1) {
            return "Cerah Berawan";
        } else if (weatherCode == 2) {
            return "Berawan";
        } else if (weatherCode == 3) {
            return "Mendung";
        } else if (weatherCode == 45 || weatherCode == 48) {
            return "Kabut";
        } else if (weatherCode == 51 || weatherCode == 53 || weatherCode == 55) {
            return "Gerimis";
        } else if (weatherCode == 56 || weatherCode == 57) {
            return "Gerimis Beku";
        } else if (weatherCode == 61 || weatherCode == 63 || weatherCode == 65) {
            return "Hujan";
        } else if (weatherCode == 66 || weatherCode == 67) {
            return "Hujan Beku";
        } else if (weatherCode == 80 || weatherCode == 81 || weatherCode == 82) {
            return "Hujan Deras";
        } else if (weatherCode == 95) {
            return "Badai";
        } else if (weatherCode == 96 || weatherCode == 99) {
            return "Badai Petir";
        } else {
            return "Tidak Diketahui";
        }
    }
}