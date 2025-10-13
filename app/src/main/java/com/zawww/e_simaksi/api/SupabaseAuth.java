package com.zawww.e_simaksi.api;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class SupabaseAuth {

    private static final String BASE_URL = "https://kitxtcpfnccblznbagzx.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpdHh0Y3BmbmNjYmx6bmJhZ3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1ODIxMzEsImV4cCI6MjA3NTE1ODEzMX0.OySigpw4AWI3G7JW_8r8yXu7re0Mr9CYv8u3d9Fr548";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static AuthService service = retrofit.create(AuthService.class);

    // ===== REGISTER =====
    public static void registerUser(String email, String password, String namaLengkap) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("data", Map.of("nama_lengkap", namaLengkap));

        service.register(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Log.d("SupabaseAuth", "Registrasi berhasil: " + response.body());
                } else {
                    Log.e("SupabaseAuth", "Registrasi gagal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "Error koneksi: " + t.getMessage());
            }
        });
    }

    // ===== LOGIN =====
    public static void loginUser(String email, String password, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        service.login(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    String accessToken = json.get("access_token").getAsString();
                    String userId = json.getAsJsonObject("user").get("id").getAsString();

                    Log.d("SupabaseAuth", "Login berhasil, ID: " + userId);
                    callback.onSuccess(accessToken, userId);
                } else {
                    Log.e("SupabaseAuth", "Login gagal: " + response.message());
                    callback.onError("Login gagal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "Error koneksi: " + t.getMessage());
                callback.onError("Error koneksi: " + t.getMessage());
            }
        });
    }

    // ===== INTERFACE RETROFIT =====
    interface AuthService {
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY
        })
        @POST("auth/v1/signup")
        Call<JsonObject> register(@Body Map<String, Object> body);

        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY
        })
        @POST("auth/v1/token?grant_type=password")
        Call<JsonObject> login(@Body Map<String, Object> body);
    }

    // ===== CALLBACK UNTUK LOGIN =====
    public interface AuthCallback {
        void onSuccess(String accessToken, String userId);
        void onError(String errorMessage);
    }
}