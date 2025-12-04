package com.zawww.e_simaksi.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonObject;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.KuotaHarian;
import com.zawww.e_simaksi.model.PengaturanBiaya;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Profile;
import com.zawww.e_simaksi.model.Promosi;
import com.zawww.e_simaksi.model.Reservasi;
import com.zawww.e_simaksi.model.Pengumuman;


import java.io.IOException;
import java.io.InputStream;
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
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class SupabaseAuth {

    private static final String BASE_URL = "https://kitxtcpfnccblznbagzx.supabase.co/";
    // Pastikan API KEY ini aman dan benar
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpdHh0Y3BmbmNjYmx6bmJhZ3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1ODIxMzEsImV4cCI6MjA3NTE1ODEzMX0.OySigpw4AWI3G7JW_8r8yXu7re0Mr9CYv8u3d9Fr548";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // Init Services
    private static final AuthService authService = retrofit.create(AuthService.class);
    private static final ProfileService profileService = retrofit.create(ProfileService.class);
    private static final PromosiService promosiService = retrofit.create(PromosiService.class);
    public static final ReservasiService reservasiService = retrofit.create(ReservasiService.class);
    public static final KuotaService kuotaService = retrofit.create(KuotaService.class);
    private static final StorageService storageService = retrofit.create(StorageService.class);
    private static final PengaturanBiayaService pengaturanBiayaService = retrofit.create(PengaturanBiayaService.class);
    public static final FunctionService functionService = retrofit.create(FunctionService.class);
    private static final PengumumanService pengumumanService = retrofit.create(PengumumanService.class);


    // =============================================================================================
    // BAGIAN 1: AUTHENTICATION (Login, Register, Reset Password, Verify)
    // =============================================================================================

    // 1. REGISTER
    public static void checkUserExists(String email, UserExistsCallback callback) {
        profileService.getProfileByEmail("eq." + email, "email").enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(!response.body().isEmpty());
                } else {
                    // Terjadi error, anggap user tidak ada agar tidak memblokir registrasi
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                // Terjadi error, anggap user tidak ada
                callback.onResult(false);
            }
        });
    }

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
                        if (body.has("id")) {
                            String userId = body.get("id").getAsString();
                            insertProfile(userId, email, namaLengkap);
                        }
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Gagal parsing user id: " + e.getMessage());
                    }
                    callback.onSuccess();
                } else {
                    handleErrorResponse(response, callback::onError);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // 2. VERIFIKASI OTP EMAIL (Signup)
    public static void verifyEmailOtp(String email, String token, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "signup");
        body.put("email", email);
        body.put("token", token);

        authService.verifyOtp(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginResponse(response.body(), callback);
                } else {
                    callback.onError("Kode salah atau kadaluarsa");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // 3. LOGIN
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
                    String refreshToken = json.get("refresh_token").getAsString();
                    String userId = json.getAsJsonObject("user").get("id").getAsString();

                    // Cek apakah email verified
                    getUserInfo(accessToken, new UserInfoCallback() {
                        @Override
                        public void onSuccess(JsonObject userData) {
                            if (userData != null && userData.has("email_confirmed_at") && !userData.get("email_confirmed_at").isJsonNull()) {
                                callback.onSuccess(accessToken, userId, refreshToken);
                            } else {
                                callback.onError("Silakan verifikasi email Anda sebelum login.");
                            }
                        }
                        @Override
                        public void onError(String errorMessage) {
                            callback.onError("Gagal mengambil data user: " + errorMessage);
                        }
                    });
                } else {
                    handleErrorResponse(response, callback::onError);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Error koneksi: " + t.getMessage());
            }
        });
    }

    // 4.1 LUPA PASSWORD: Kirim OTP Reset
    public static void sendPasswordResetOtp(String email, UpdateCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        authService.sendRecovery(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    handleErrorResponse(response, callback::onError);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // 5. LUPA PASSWORD: Verifikasi Token Recovery
    public static void verifyRecoveryOtp(String email, String token, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "recovery"); // Tipe recovery
        body.put("email", email);
        body.put("token", token);

        authService.verifyOtp(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginResponse(response.body(), callback);
                } else {
                    callback.onError("Token salah atau kadaluarsa.");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // 5.1 LUPA PASSWORD: Reset via OTP
    public static void resetPasswordWithOtp(String email, String token, String newPassword, UpdateCallback callback) {
        // Step 1: Verify the OTP to get a valid session (access token)
        verifyRecoveryOtp(email, token, new AuthCallback() {
            @Override
            public void onSuccess(String accessToken, String userId, String refreshToken) {
                // Step 2: With the access token, update the user's password
                updateUserPassword(accessToken, newPassword, new UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError("Gagal mengubah password setelah verifikasi OTP: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Verifikasi OTP gagal: " + errorMessage);
            }
        });
    }

    // 6. LUPA PASSWORD: Update Password Baru
    public static void updateUserPassword(String accessToken, String newPassword, UpdateCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("password", newPassword);

        authService.updateUser("Bearer " + accessToken, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Gagal update password.");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // 7. REFRESH TOKEN
    public static void refreshAccessToken(String refreshToken, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        authService.refreshToken(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginResponse(response.body(), callback);
                } else {
                    callback.onError("Sesi habis, silakan login ulang.");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // --- Helpers untuk Auth ---
    private static void handleLoginResponse(JsonObject json, AuthCallback callback) {
        try {
            String accessToken = json.get("access_token").getAsString();
            String refreshToken = json.get("refresh_token").getAsString();
            String userId = json.getAsJsonObject("user").get("id").getAsString();
            callback.onSuccess(accessToken, userId, refreshToken);
        } catch (Exception e) {
            callback.onError("Gagal parsing data session: " + e.getMessage());
        }
    }

    private static void handleErrorResponse(Response<?> response, ErrorHandler handler) {
        String genericError = "Terjadi kesalahan. Silakan coba lagi.";
        try {
            if (response.errorBody() != null) {
                String errBody = response.errorBody().string();
                try {
                    JsonObject jsonObject = com.google.gson.JsonParser.parseString(errBody).getAsJsonObject();
                    if (jsonObject.has("error_description")) {
                        String errorDesc = jsonObject.get("error_description").getAsString();
                        if (errorDesc.equalsIgnoreCase("Invalid login credentials") || errorDesc.equalsIgnoreCase("invalid credential")) {
                            handler.onError("Email atau Password Salah");
                        } else {
                            handler.onError(errorDesc);
                        }
                    } else if (jsonObject.has("msg")) {
                        handler.onError(jsonObject.get("msg").getAsString());
                    } else {
                        handler.onError(genericError);
                    }
                } catch (Exception jsonException) {
                    // Gagal parse JSON, tampilkan pesan generik
                    handler.onError(genericError);
                }
            } else {
                handler.onError("Terjadi error: " + response.code());
            }
        } catch (IOException e) {
            handler.onError(genericError);
        }
    }
    interface ErrorHandler { void onError(String msg); }


    // =============================================================================================
    // BAGIAN 2: FITUR UTAMA (Profile, Reservasi, Promosi, Upload, dll)
    // =============================================================================================

    public static void getAktifPengumuman(PengumumanCallback callback) {
        pengumumanService.getAktifPengumuman().enqueue(new Callback<List<com.zawww.e_simaksi.model.Pengumuman>>() {
            @Override
            public void onResponse(Call<List<com.zawww.e_simaksi.model.Pengumuman>> call, Response<List<com.zawww.e_simaksi.model.Pengumuman>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal mengambil pengumuman.");
                }
            }

            @Override
            public void onFailure(Call<List<com.zawww.e_simaksi.model.Pengumuman>> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void getPromosiPoster(PromosiCallback callback) {
        promosiService.getPromosiAktif().enqueue(new Callback<List<Promosi>>() {
            @Override
            public void onResponse(Call<List<Promosi>> call, Response<List<Promosi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal mengambil promosi.");
                }
            }
            @Override
            public void onFailure(Call<List<Promosi>> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private static void insertProfile(String userId, String email, String namaLengkap) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userId);
        profile.put("email", email);
        profile.put("nama_lengkap", namaLengkap);
        profile.put("peran", "pendaki");

        profileService.insertProfile(profile).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) Log.e("SupabaseAuth", "Gagal insert profil");
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "Error profil: " + t.getMessage());
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
                    callback.onError("Gagal ambil user info");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getJadwalAktif(String idPengguna, JadwalCallback callback) {
        java.time.LocalDate today = java.time.LocalDate.now();
        String todayString = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String idQuery = "eq." + idPengguna;
        String statusQuery = "in.(menunggu_pembayaran,terkonfirmasi)";
        String tanggalQuery = "gte." + todayString;

        reservasiService.getJadwalAktif(idQuery, statusQuery, tanggalQuery, "id_reservasi,tanggal_pendakian,kode_reservasi,jumlah_pendaki", "tanggal_pendakian.asc", 1)
                .enqueue(new Callback<List<Reservasi>>() {
                    @Override
                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            callback.onSuccess(response.body().get(0));
                        } else {
                            if (response.isSuccessful()) callback.onError("Tidak ada jadwal aktif.");
                            else callback.onError("Error server jadwal.");
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                        callback.onError(t.getMessage());
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
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Gagal mengambil detail reservasi.");
                }
            }
            @Override
            public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void cekKuota(String tanggal, KuotaCallback callback) {
        String tanggalQuery = "eq." + tanggal;
        kuotaService.getKuota(tanggalQuery).enqueue(new Callback<List<KuotaHarian>>() {
            @Override
            public void onResponse(Call<List<KuotaHarian>> call, Response<List<KuotaHarian>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) callback.onError("Kuota belum dibuka.");
                    else callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Gagal cek kuota.");
                }
            }
            @Override
            public void onFailure(Call<List<KuotaHarian>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getKuotaMingguan(KuotaListCallback callback) {
        java.time.LocalDate today = java.time.LocalDate.now();
        String todayString = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        kuotaService.getKuotaMingguan("gte." + todayString).enqueue(new Callback<List<KuotaHarian>>() {
            @Override
            public void onResponse(Call<List<KuotaHarian>> call, Response<List<KuotaHarian>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal mengambil data kuota.");
                }
            }

            @Override
            public void onFailure(Call<List<KuotaHarian>> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void uploadSuratSehatWithRefresh(Context context, com.zawww.e_simaksi.util.SessionManager sessionManager, Uri fileUri, String fileName, UploadCallback callback) {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            callback.onError("Sesi tidak valid.");
            return;
        }

        refreshAccessToken(refreshToken, new AuthCallback() {
            @Override
            public void onSuccess(String newAccessToken, String userId, String newRefreshToken) {
                String email = sessionManager.getUserEmail();
                sessionManager.createLoginSession(newAccessToken, userId, email, newRefreshToken);
                uploadSuratSehat(context, newAccessToken, fileUri, fileName, callback);
            }
            @Override
            public void onError(String errorMessage) {
                callback.onError("Sesi berakhir, login ulang.");
            }
        });
    }

    public static void uploadSuratSehat(Context context, String accessToken, Uri fileUri, String fileName, UploadCallback callback) {
        if (accessToken == null) { callback.onError("Token null."); return; }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] fileBytes = byteBuffer.toByteArray();
            inputStream.close();

            if (fileBytes.length == 0) { callback.onError("File kosong."); return; }

            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse(mimeType), fileBytes);
            String bearerToken = "Bearer " + accessToken;

            storageService.uploadFile(bearerToken, fileName, requestFile).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String publicUrl = BASE_URL + "storage/v1/object/public/surat-sehat/" + fileName;
                        callback.onSuccess(publicUrl);
                    } else {
                        callback.onError("Gagal upload: " + response.message());
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("Error file: " + e.getMessage());
        }
    }

    public static void getPengaturanBiaya(BiayaCallback callback) {
        pengaturanBiayaService.getSemuaBiaya().enqueue(new Callback<List<PengaturanBiaya>>() {
            @Override
            public void onResponse(Call<List<PengaturanBiaya>> call, Response<List<PengaturanBiaya>> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Gagal ambil biaya.");
            }
            @Override
            public void onFailure(Call<List<PengaturanBiaya>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getProfile(String accessToken, String userId, ProfileCallback callback) {
        profileService.getProfile("Bearer " + accessToken, "eq." + userId, "*").enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Gagal ambil profil.");
                }
            }
            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void updateProfile(String accessToken, String userId, Map<String, Object> updates, UpdateCallback callback) {
        profileService.updateProfileFields("Bearer " + accessToken, "eq." + userId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Gagal update profil.");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void hitungTotalReservasi(int jumlahPendaki, int jumlahParkir, String kodePromo, HitungReservasiCallback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_jumlah_pendaki", jumlahPendaki);
        params.put("p_jumlah_parkir", jumlahParkir);
        params.put("p_kode_promo", kodePromo == null ? "" : kodePromo);

        reservasiService.hitungTotalReservasi(params).enqueue(new Callback<com.zawww.e_simaksi.model.HitungReservasiResponse>() {
            @Override
            public void onResponse(Call<com.zawww.e_simaksi.model.HitungReservasiResponse> call, Response<com.zawww.e_simaksi.model.HitungReservasiResponse> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Gagal hitung biaya.");
            }
            @Override
            public void onFailure(Call<com.zawww.e_simaksi.model.HitungReservasiResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getReservasiHistory(String idPengguna, ReservasiCallback callback) {
        reservasiService.getReservasiByUser("eq." + idPengguna, "*", "dipesan_pada.desc")
                .enqueue(new Callback<List<Reservasi>>() {
                    @Override
                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                        if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                        else callback.onError("Gagal ambil history.");
                    }
                    @Override
                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public static void getSnapToken(String orderId, long grossAmount, TokenCallback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", orderId);
        params.put("gross_amount", grossAmount);

        functionService.getPaymentToken(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().has("token")) {
                        String token = response.body().get("token").getAsString();
                        String redirectUrl = response.body().has("redirect_url") ? response.body().get("redirect_url").getAsString() : "";
                        callback.onSuccess(token, redirectUrl);
                    } else callback.onError("Token tidak ditemukan.");
                } else callback.onError("Gagal get token payment.");
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void ajukanRefund(int idReservasi, String alasan, String bank, String noRek,
                                    String atasNama, int persentase, long nominal, GeneralCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "pengajuan_refund");
        updates.put("alasan_batal", alasan);
        updates.put("bank_refund", bank);
        updates.put("no_rek_refund", noRek);
        updates.put("atas_nama_refund", atasNama);
        updates.put("persentase_refund", persentase);
        updates.put("nominal_refund", nominal);

        String idQuery = "eq." + idReservasi;

        reservasiService.ajukanRefund(idQuery, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Gagal mengajukan refund.");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // =============================================================================================
    // BAGIAN 3: RETROFIT INTERFACES
    // =============================================================================================

    interface UserService {
        @Headers({"Content-Type: application/json"})
        @GET("auth/v1/user")
        Call<JsonObject> getUser(@Header("Authorization") String bearerToken, @Header("apikey") String apiKey);
    }

    interface AuthService {
        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/signup")
        Call<JsonObject> register(@Body Map<String, Object> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/otp")
        Call<JsonObject> sendOtp(@Body Map<String, Object> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/recover")
        Call<JsonObject> sendRecovery(@Body Map<String, Object> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/token?grant_type=password")
        Call<JsonObject> login(@Body Map<String, Object> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/token?grant_type=refresh_token")
        Call<JsonObject> refreshToken(@Body Map<String, Object> body);

        // Digabungkan: Endpoint Verify (Bisa untuk Signup & Recovery)
        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @POST("auth/v1/verify")
        Call<JsonObject> verifyOtp(@Body Map<String, Object> body);

        // Endpoint Update User (Ganti Password)
        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY})
        @PUT("auth/v1/user")
        Call<JsonObject> updateUser(@Header("Authorization") String token, @Body Map<String, Object> body);
    }

    interface ProfileService {
        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: return=representation"})
        @POST("rest/v1/profiles")
        Call<JsonObject> insertProfile(@Body Map<String, Object> body);

        @Headers({"apikey: " + API_KEY})
        @GET("rest/v1/profiles")
        Call<List<Profile>> getProfile(@Header("Authorization") String bearerToken, @Query("id") String userId, @Query("select") String select);

        @Headers({"apikey: " + API_KEY})
        @GET("rest/v1/profiles")
        Call<List<Profile>> getProfileByEmail(@Query("email") String email, @Query("select") String select);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: return=minimal"})
        @PATCH("rest/v1/profiles")
        Call<Void> updateProfileFields(@Header("Authorization") String bearerToken, @Query("id") String userId, @Body Map<String, Object> profileData);
    }

    interface PromosiService {
        @Headers({"apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/promosi_poster?is_aktif=eq.true&order=urutan.asc")
        Call<List<Promosi>> getPromosiAktif();
    }
    public interface ReservasiService {
        @Headers({"apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getDetailReservasi(@Query("id_reservasi") String idQuery, @Query("select") String select);

        @Headers({"apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getJadwalAktif(@Query("id_pengguna") String idPengguna, @Query("status") String status, @Query("tanggal_pendakian") String tgl, @Query("select") String sel, @Query("order") String ord, @Query("limit") int limit);

        @Headers({"apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getReservasiByUser(@Query("id_pengguna") String idPengguna, @Query("select") String select, @Query("order") String order);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: return=representation,resolution=merge-duplicates"})
        @POST("rest/v1/reservasi")
        Call<List<Reservasi>> insertReservasi(@Header("Authorization") String authToken, @Body Map<String, Object> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: resolution=merge-duplicates"})
        @POST("rest/v1/pendaki_rombongan")
        Call<Void> insertRombongan(@Header("Authorization") String authToken, @Body List<PendakiRombongan> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: resolution=merge-duplicates"})
        @POST("rest/v1/barang_bawaan_sampah")
        Call<Void> insertBarang(@Header("Authorization") String authToken, @Body List<BarangBawaanSampah> body);

        @Headers({"Content-Type: application/json",
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY})
        @POST("rest/v1/rpc/hitung_total_reservasi")
        Call<com.zawww.e_simaksi.model.HitungReservasiResponse> hitungTotalReservasi(@Body Map<String, Object> params);

        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @PATCH("rest/v1/reservasi")
        Call<Void> ajukanRefund(
                @Query("id_reservasi") String idReservasiQuery,
                @Body Map<String, Object> body
        );
    }

    public interface KuotaService {
        @Headers({"apikey: " + API_KEY, "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/kuota_harian?select=kuota_maksimal,kuota_terpesan,tanggal_kuota&order=tanggal_kuota.asc")
        Call<List<KuotaHarian>> getKuota(@Query("tanggal_kuota") String tanggalQuery);

        @Headers({"apikey: " + API_KEY, "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/kuota_harian?select=kuota_maksimal,kuota_terpesan,tanggal_kuota&order=tanggal_kuota.asc")
        Call<List<KuotaHarian>> getKuotaMingguan(@Query("tanggal_kuota") String tanggalQuery);


        @Headers({"Content-Type: application/json", "apikey: " + API_KEY})
        @POST("rest/v1/rpc/cek_dan_ambil_kuota")
        Call<Long> cekDanAmbilKuota(@Header("Authorization") String authToken, @Body Map<String, Object> body);
    }

    public interface StorageService {
        @Headers({"apikey: " + API_KEY})
        @POST("storage/v1/object/surat-sehat/{fileName}")
        Call<JsonObject> uploadFile(@Header("Authorization") String authToken, @Path("fileName") String fileName, @Body okhttp3.RequestBody fileBody);
    }

    public interface PengaturanBiayaService {
        @Headers({"apikey: " + API_KEY, "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/pengaturan_biaya?select=nama_item,harga")
        Call<List<PengaturanBiaya>> getSemuaBiaya();
    }

    public interface FunctionService {
        @Headers({"Content-Type: application/json", "Authorization: Bearer " + API_KEY})
        @POST("functions/v1/payment-token")
        Call<JsonObject> getPaymentToken(@Body Map<String, Object> body);
    }

    public interface PengumumanService {
        @Headers({"apikey: " + API_KEY, "Authorization: Bearer " + API_KEY})
        @GET("rest/v1/pengumuman?telah_terbit=eq.true&end_date=gte.now()&order=dibuat_pada.desc")
        Call<List<Pengumuman>> getAktifPengumuman();
    }


    // =============================================================================================
    // BAGIAN 4: CALLBACKS
    // =============================================================================================
    public interface AuthCallback { void onSuccess(String accessToken, String userId, String refreshToken); void onError(String errorMessage); }
    public interface RegisterCallback { void onSuccess(); void onError(String errorMessage); }
    public interface UserExistsCallback { void onResult(boolean exists); }
    public interface UserInfoCallback { void onSuccess(JsonObject userData); void onError(String errorMessage); }
    public interface PromosiCallback { void onSuccess(List<Promosi> promosiList); void onError(String errorMessage); }
    public interface JadwalCallback { void onSuccess(Reservasi jadwal); void onError(String errorMessage); }
    public interface DetailReservasiCallback { void onSuccess(Reservasi reservasi); void onError(String errorMessage); }
    public interface ReservasiCallback { void onSuccess(List<Reservasi> reservasiList); void onError(String errorMessage); }
    public interface KuotaCallback { void onSuccess(KuotaHarian kuota); void onError(String errorMessage); }
    public interface KuotaListCallback { void onSuccess(List<KuotaHarian> kuota); void onError(String errorMessage); }
    public interface UploadCallback { void onSuccess(String publicUrl); void onError(String errorMessage); }
    public interface UpdateCallback { void onSuccess(); void onError(String errorMessage); }
    public interface GeneralCallback { void onSuccess(); void onError(String errorMessage); }
    public interface BiayaCallback { void onSuccess(List<PengaturanBiaya> biayaList); void onError(String errorMessage); }
    public interface ProfileCallback { void onSuccess(Profile profile); void onError(String errorMessage); }
    public interface HitungReservasiCallback { void onSuccess(com.zawww.e_simaksi.model.HitungReservasiResponse response); void onError(String errorMessage); }
    public interface TokenCallback { void onSuccess(String token, String redirectUrl); void onError(String error); }
    public interface PengumumanCallback {
        void onSuccess(List<Pengumuman> pengumumanList);
        void onError(String errorMessage);
    }
}