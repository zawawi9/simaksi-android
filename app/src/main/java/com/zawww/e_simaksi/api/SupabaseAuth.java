package com.zawww.e_simaksi.api;

import android.util.Log;

import com.google.gson.JsonObject;
import com.zawww.e_simaksi.model.Promosi;
import com.zawww.e_simaksi.model.Reservasi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class SupabaseAuth {

    private static final String BASE_URL = "https://kitxtcpfnccblznbagzx.supabase.co/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpdHh0Y3BmbmNjYmx6bmJhZ3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1ODIxMzEsImV4cCI6MjA3NTE1ODEzMX0.OySigpw4AWI3G7JW_8r8yXu7re0Mr9CYv8u3d9Fr548";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final AuthService authService = retrofit.create(AuthService.class);
    private static final ProfileService profileService = retrofit.create(ProfileService.class);
    private static final PromosiService promosiService = retrofit.create(PromosiService.class);
    public static final ReservasiService reservasiService = retrofit.create(ReservasiService.class);
    public static void getPromosiPoster(PromosiCallback callback) {
        // Panggil service untuk mengambil data
        promosiService.getPromosiAktif().enqueue(new Callback<List<Promosi>>() {
            @Override
            public void onResponse(Call<List<Promosi>> call, Response<List<Promosi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SupabaseAuth", "✅ Berhasil mengambil data promosi: " + response.body().size() + " item.");
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal mengambil promosi: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Gagal mengambil promosi: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Promosi>> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi promosi: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // ========== REGISTER ==========
    public static void registerUser(String email, String password, String namaLengkap, RegisterCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("data", Map.of("nama_lengkap", namaLengkap));

        authService.register(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SupabaseAuth", "✅ Registrasi berhasil: " + response.body());

                    try {
                        JsonObject body = response.body();

                        // Ambil ID langsung dari root JSON
                        if (body.has("id")) {
                            String userId = body.get("id").getAsString();
                            insertProfile(userId, email, namaLengkap); // Simpan ke tabel profiles
                        } else {
                            Log.e("SupabaseAuth", "⚠️ Tidak dapat menemukan ID user di respons Supabase.");
                        }

                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Gagal parsing user id: " + e.getMessage());
                    }

                    callback.onSuccess();
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Registrasi gagal: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Registrasi gagal: " + response.message());
                }
            }


            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // ========== INSERT KE PROFILES ==========
    private static void insertProfile(String userId, String email, String namaLengkap) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userId);
        profile.put("email", email);
        profile.put("nama_lengkap", namaLengkap);
        profile.put("peran", "pendaki"); // default role

        profileService.insertProfile(profile).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Log.d("SupabaseAuth", "✅ Profil berhasil ditambahkan: " + response.body());
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "❌ Gagal menambah profil: " + response.code() + " | " + err);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Gagal membaca error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error saat menambah profil: " + t.getMessage());
            }
        });
    }

    // ========== LOGIN ==========
    public static void loginUser(String email, String password, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        authService.login(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    String accessToken = json.get("access_token").getAsString();
                    String userId = json.getAsJsonObject("user").get("id").getAsString();

                    getUserInfo(accessToken, new UserInfoCallback() {
                        @Override
                        public void onSuccess(JsonObject userData) {
                            if (userData != null && userData.has("email_confirmed_at") && !userData.get("email_confirmed_at").isJsonNull()) {
                                Log.d("SupabaseAuth", "✅ Email sudah diverifikasi, login sukses.");
                                callback.onSuccess(accessToken, userId);
                            } else {
                                Log.w("SupabaseAuth", "⚠️ Email belum diverifikasi.");
                                callback.onError("Silakan verifikasi email Anda sebelum login.");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError("Gagal mengambil data user: " + errorMessage);
                        }
                    });

                } else {
                    Log.e("SupabaseAuth", "❌ Login gagal: " + response.message());
                    callback.onError("Login gagal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi: " + t.getMessage());
                callback.onError("Error koneksi: " + t.getMessage());
            }
        });
    }

    private static void getUserInfo(String accessToken, UserInfoCallback callback) {
        Retrofit retrofitUser = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserService userService = retrofitUser.create(UserService.class);

        userService.getUser("Bearer " + accessToken, API_KEY).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal mengambil data user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void getJadwalAktif(String idPengguna, JadwalCallback callback) {
        String idQuery = "eq." + idPengguna;
        String statusQuery = "eq.terkonfirmasi"; // Hanya ambil yang sudah terkonfirmasi
        // Ambil data ringkas saja
        String querySelect = "id_reservasi,tanggal_pendakian,kode_reservasi,jumlah_pendaki";

        reservasiService.getJadwalAktif(idQuery, statusQuery, querySelect, "tanggal_pendakian.asc", 1)
                .enqueue(new Callback<List<Reservasi>>() {
                    @Override
                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Log.d("SupabaseAuth", "✅ Berhasil mengambil jadwal aktif.");
                            callback.onSuccess(response.body().get(0)); // Kirim data jadwal pertama
                        } else {
                            // Ini bukan error, tapi memang tidak ada jadwal
                            if (response.isSuccessful()) {
                                Log.d("SupabaseAuth", "Tidak ada jadwal aktif ditemukan.");
                                callback.onError("Tidak ada jadwal aktif.");
                            } else {
                                // Ini baru error server
                                callback.onError("Gagal mengambil jadwal: " + response.message());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                        Log.e("SupabaseAuth", "⚠️ Error koneksi jadwal: " + t.getMessage());
                        callback.onError("Koneksi gagal: " + t.getMessage());
                    }
                });
    }

    public static void getDetailReservasi(int idReservasi, DetailReservasiCallback callback) {
        String idQuery = "eq." + idReservasi;

        String querySelect = "*,pendaki_rombongan(*),barang_bawaan_sampah(*)";

        reservasiService.getDetailReservasi(idQuery, querySelect).enqueue(new Callback<List<Reservasi>>() {
            @Override
            public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Log.d("SupabaseAuth", "✅ Berhasil mengambil detail reservasi.");
                    callback.onSuccess(response.body().get(0)); // Kirim data reservasi pertama
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal mengambil detail reservasi: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Gagal mengambil data: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi detail reservasi: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // ========== SERVICE TAMBAHAN ==========
    interface UserService {
        @Headers({
                "Content-Type: application/json"
        })
        @GET("auth/v1/user")
        Call<JsonObject> getUser(@Header("Authorization") String bearerToken,
                                 @Header("apikey") String apiKey);
    }

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

    interface ProfileService {
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY, // Pakai Service Key (API_KEY)
                "Prefer: return=representation"
        })
        @POST("rest/v1/profiles")
        Call<JsonObject> insertProfile(@Body Map<String, Object> body);
    }

    interface PromosiService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        // Ambil data dari 'promosi_poster', filter yg is_aktif=true, urutkan berdasarkan 'urutan'
        @GET("rest/v1/promosi_poster?is_aktif=eq.true&order=urutan.asc")
        Call<List<Promosi>> getPromosiAktif();
    }

    public interface ReservasiService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getDetailReservasi(
                @Query("id_reservasi") String idQuery, // eg: "eq.1"
                @Query("select") String select
        );
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getJadwalAktif(
                @Query("id_pengguna") String idPenggunaQuery,
                @Query("status") String statusQuery,     // eq.terkonfirmasi
                @Query("select") String select,          // id_reservasi,tanggal_pendakian,dll
                @Query("order") String order,
                @Query("limit") int limit
        );
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getReservasiByUser(
                @Query("id_pengguna") String idPenggunaQuery, // eg: "eq.user-id-here"
                @Query("select") String select,               // specify fields to return
                @Query("order") String order                  // order by field, default ascending
        );
    }

    // ========== CALLBACKS ==========
    public interface AuthCallback {
        void onSuccess(String accessToken, String userId);
        void onError(String errorMessage);
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface UserInfoCallback {
        void onSuccess(JsonObject userData);
        void onError(String errorMessage);
    }

    public interface PromosiCallback {
        void onSuccess(List<Promosi> promosiList);
        void onError(String errorMessage);
    }

    public interface JadwalCallback {
        void onSuccess(Reservasi jadwal);
        void onError(String errorMessage);
    }

    public interface DetailReservasiCallback {
        void onSuccess(Reservasi reservasi);
        void onError(String errorMessage);
    }

    public interface ReservasiCallback {
        void onSuccess(List<Reservasi> reservasiList);
        void onError(String errorMessage);
    }

    public static void getReservasiHistory(String idPengguna, ReservasiCallback callback) {
        String idQuery = "eq." + idPengguna;
        String selectQuery = "*"; // Ambil semua data
        String orderQuery = "dipesan_pada.desc"; // Urutkan dari yang terbaru

        reservasiService.getReservasiByUser(idQuery, selectQuery, orderQuery)
                .enqueue(new Callback<List<Reservasi>>() {
                    @Override
                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("SupabaseAuth", "✅ Berhasil mengambil riwayat reservasi: " + response.body().size() + " item.");
                            callback.onSuccess(response.body());
                        } else {
                            try {
                                String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                                Log.e("SupabaseAuth", "Gagal mengambil riwayat reservasi: " + errBody);
                            } catch (java.io.IOException e) {
                                Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                            }
                            callback.onError("Gagal mengambil riwayat: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                        Log.e("SupabaseAuth", "⚠️ Error koneksi riwayat reservasi: " + t.getMessage());
                        callback.onError("Koneksi gagal: " + t.getMessage());
                    }
                });
    }
}
