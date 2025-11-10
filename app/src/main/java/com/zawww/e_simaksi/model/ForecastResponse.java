package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {

    @SerializedName("list")
    private List<ListItem> list;

    @SerializedName("city")
    private City city;

    public List<ListItem> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }

    public static class ListItem {
        @SerializedName("dt")
        private long dt;

        @SerializedName("main")
        private Main main;

        @SerializedName("weather")
        private List<Weather> weather;

        @SerializedName("wind")
        private Wind wind;

        @SerializedName("dt_txt")
        private String dt_txt;

        public long getDt() {
            return dt;
        }

        public Main getMain() {
            return main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public Wind getWind() {
            return wind;
        }

        public String getDt_txt() {
            return dt_txt;
        }
    }

    public static class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        @SerializedName("id")
        private int id;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() {
            return speed;
        }
    }

    public static class City {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }
}
