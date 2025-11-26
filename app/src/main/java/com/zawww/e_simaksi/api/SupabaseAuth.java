package com.zawww.e_simaksi.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonObject;
import com.zawww.e_simaksi.model.BuatReservasiRequest;
import com.zawww.e_simaksi.model.KuotaHarian;
import com.zawww.e_simaksi.model.PengaturanBiaya;
import com.zawww.e_simaksi.model.Promosi;
import com.zawww.e_simaksi.model.Reservasi;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Profile;
import com.zawww.e_simaksi.util.SessionManager;

import java.io.ByteArrayOutputStream;
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
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PATCH;

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
    public static final KuotaService kuotaService = retrofit.create(KuotaService.class);
    private static final StorageService storageService = retrofit.create(StorageService.class);
    private static final PengaturanBiayaService pengaturanBiayaService = retrofit.create(PengaturanBiayaService.class);
    public static final FunctionService functionService = retrofit.create(FunctionService.class);
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
                    String refreshToken = json.get("refresh_token").getAsString(); // Ambil refresh token
                    String userId = json.getAsJsonObject("user").get("id").getAsString();

                    getUserInfo(accessToken, new UserInfoCallback() {
                        @Override
                        public void onSuccess(JsonObject userData) {
                            if (userData != null && userData.has("email_confirmed_at") && !userData.get("email_confirmed_at").isJsonNull()) {
                                Log.d("SupabaseAuth", "✅ Email sudah diverifikasi, login sukses.");
                                callback.onSuccess(accessToken, userId, refreshToken);
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
                    String errorMessage = "Login gagal: " + response.message();
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Log.e("SupabaseAuth", "❌ Login gagal: " + errorJson);
                            // Cek jika error karena email belum dikonfirmasi
                            if (errorJson.contains("Email not confirmed")) {
                                errorMessage = "Email belum terverifikasi. Silakan cek email Anda.";
                            }
                        } else {
                             Log.e("SupabaseAuth", "❌ Login gagal: " + response.message());
                        }
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi: " + t.getMessage());
                callback.onError("Error koneksi: " + t.getMessage());
            }
        });
    }

    public static void refreshAccessToken(String refreshToken, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        authService.refreshToken(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    String newAccessToken = json.get("access_token").getAsString();
                    String newRefreshToken = json.get("refresh_token").getAsString();
                    String userId = json.getAsJsonObject("user").get("id").getAsString();
                    Log.d("SupabaseAuth", "✅ Token berhasil diperbarui.");
                    callback.onSuccess(newAccessToken, userId, newRefreshToken);
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal memperbarui token: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Gagal memperbarui token: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi refresh token: " + t.getMessage());
                callback.onError("Koneksi refresh token gagal: " + t.getMessage());
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
        // 1. Dapatkan tanggal hari ini
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        // 2. Buat query yang benar
        String idQuery = "eq." + idPengguna;
        // Ambil jadwal yang statusnya menunggu pembayaran ATAU sudah terkonfirmasi
        String statusQuery = "in.(menunggu_pembayaran,terkonfirmasi)";
        // Ambil jadwal yang tanggalnya hari ini ATAU di masa depan
        String tanggalQuery = "gte." + todayString;

        String querySelect = "id_reservasi,tanggal_pendakian,kode_reservasi,jumlah_pendaki";

        // Panggil service dengan query yang sudah diperbaiki
        reservasiService.getJadwalAktif(idQuery, statusQuery, tanggalQuery, querySelect, "tanggal_pendakian.asc", 1)
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
                                try {
                                    String errBody = response.errorBody() != null ? response.errorBody().string() : response.message();
                                    Log.e("SupabaseAuth", "Gagal mengambil jadwal: " + errBody);
                                    callback.onError("Gagal mengambil jadwal: " + errBody);
                                } catch (java.io.IOException e) {
                                    callback.onError("Gagal mengambil jadwal: " + e.getMessage());
                                }
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
    public static void cekKuota(String tanggal, KuotaCallback callback) {
        String tanggalQuery = "eq." + tanggal;
        kuotaService.getKuota(tanggalQuery).enqueue(new Callback<List<KuotaHarian>>() {
            @Override
            public void onResponse(Call<List<KuotaHarian>> call, Response<List<KuotaHarian>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        // Tidak ada kuota = kuota 0
                        callback.onError("Kuota untuk tanggal ini belum dibuka.");
                    } else {
                        // Kuota ditemukan
                        callback.onSuccess(response.body().get(0));
                    }
                } else {
                    callback.onError("Gagal cek kuota: " + response.message());
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
            callback.onError("Gagal upload: Sesi tidak valid (refresh token tidak ada).");
            return;
        }

        Log.d("SupabaseAuth", "Sesi token sedang diperbarui sebelum upload...");
        refreshAccessToken(refreshToken, new AuthCallback() {
            @Override
            public void onSuccess(String newAccessToken, String userId, String newRefreshToken) {
                Log.d("SupabaseAuth", "Token berhasil diperbarui. Memulai upload...");
                // Ambil email yang ada dari session manager
                String email = sessionManager.getUserEmail();
                // Panggil createLoginSession dengan 4 parameter
                sessionManager.createLoginSession(newAccessToken, userId, email, newRefreshToken);
                // Panggil metode upload yang asli dengan token baru
                uploadSuratSehat(context, newAccessToken, fileUri, fileName, callback);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("SupabaseAuth", "Gagal memperbarui token saat akan upload: " + errorMessage);
                callback.onError("Gagal upload: Sesi Anda berakhir. Silakan login kembali.");
            }
        });
    }

    public static void uploadSuratSehat(Context context, String accessToken, Uri fileUri, String fileName, UploadCallback callback) {

        // Pengecekan keamanan, pastikan token ada
        if (accessToken == null) {
            callback.onError("Gagal upload: Access token tidak valid (null).");
            return;
        }

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
            // --- AKHIR PERBAIKAN ---

            // Pengecekan keamanan jika file benar-benar kosong
            if (fileBytes.length == 0) {
                Log.e("SupabaseAuth", "Gagal membaca file, ukuran file 0 byte.");
                callback.onError("Gagal membaca file (0 bytes).");
                return;
            }

            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // Tipe default
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(mimeType),
                    fileBytes
            );

            String bearerToken = "Bearer " + accessToken;

            // Panggil service storage
            storageService.uploadFile(bearerToken, fileName, requestFile).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        // Berhasil upload, sekarang buat URL publiknya
                        String publicUrl = BASE_URL + "storage/v1/object/public/surat-sehat/" + fileName;
                        Log.d("SupabaseAuth", "✅ File terupload: " + publicUrl);
                        callback.onSuccess(publicUrl);
                    } else {
                        try {
                            String err = response.errorBody() != null ? response.errorBody().string() : response.message();
                            Log.e("SupabaseAuth", "❌ Gagal upload: " + err);
                            callback.onError("Gagal upload file: " + err);
                        } catch (IOException e) {
                            callback.onError("Gagal upload file: " + e.getMessage());
                        }
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("SupabaseAuth", "⚠️ Gagal koneksi upload: " + t.getMessage());
                    callback.onError("Koneksi upload gagal: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("SupabaseAuth", "Error membaca file: " + e.getMessage());
            callback.onError("Gagal membaca file: " + e.getMessage());
        }
    }

    public static void getPengaturanBiaya(BiayaCallback callback) {
        pengaturanBiayaService.getSemuaBiaya().enqueue(new Callback<List<PengaturanBiaya>>() {
            @Override
            public void onResponse(Call<List<PengaturanBiaya>> call, Response<List<PengaturanBiaya>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal mengambil data harga");
                }
            }
            @Override
            public void onFailure(Call<List<PengaturanBiaya>> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void sendPasswordReset(String email, UpdateCallback callback) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        authService.sendPasswordReset(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                // Supabase returns 200 OK even if the email doesn't exist, for security reasons.
                if (response.isSuccessful()) {
                    Log.d("SupabaseAuth", "✅ Permintaan reset password terkirim untuk email: " + email);
                    callback.onSuccess();
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal mengirim reset password: " + errBody);
                        callback.onError("Gagal mengirim email: " + response.message());
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                        callback.onError("Gagal mengirim email: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi reset password: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void getProfile(String accessToken, String userId, ProfileCallback callback) {
        String bearerToken = "Bearer " + accessToken;
        String userIdFilter = "eq." + userId;

        Log.d("SupabaseAuth", "Mencoba mengambil profil untuk userId: " + userId); // Logging untuk debug

        // Panggil service yang akan kita definisikan di bawah
        profileService.getProfile(bearerToken, userIdFilter, "*").enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Log.d("SupabaseAuth", "✅ Berhasil mengambil profil pengguna.");
                    callback.onSuccess(response.body().get(0));
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal mengambil profil: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Gagal mengambil data profil: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi getProfile: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public static void updateProfile(String accessToken, String userId, Map<String, Object> updates, UpdateCallback callback) {
        String bearerToken = "Bearer " + accessToken;
        String userIdFilter = "eq." + userId;

        profileService.updateProfileFields(bearerToken, userIdFilter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("SupabaseAuth", "✅ Profil berhasil diperbarui.");
                    callback.onSuccess();
                } else {
                    try {
                        String errBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("SupabaseAuth", "Gagal update profil: " + errBody);
                    } catch (Exception e) {
                        Log.e("SupabaseAuth", "Error parsing errorBody: " + e.getMessage());
                    }
                    callback.onError("Gagal memperbarui profil: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("SupabaseAuth", "⚠️ Error koneksi updateProfile: " + t.getMessage());
                callback.onError("Koneksi gagal: " + t.getMessage());
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
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gagal menghitung biaya.");
                }
            }
            @Override
            public void onFailure(Call<com.zawww.e_simaksi.model.HitungReservasiResponse> call, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
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
    public static void getSnapToken(String orderId, long grossAmount, TokenCallback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", orderId);
        params.put("gross_amount", grossAmount);

        functionService.getPaymentToken(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Ambil token dari JSON: { "token": "xxxx", "redirect_url": "..." }
                    if (response.body().has("token")) {
                        String token = response.body().get("token").getAsString();
                        String redirectUrl = "";
                        if (response.body().has("redirect_url")) {
                            redirectUrl = response.body().get("redirect_url").getAsString();
                        }
                        callback.onSuccess(token, redirectUrl);
                    } else {
                        callback.onError("Token tidak ditemukan dalam respons server.");
                    }
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : response.message();
                        Log.e("Midtrans", "Gagal get token: " + err);
                        callback.onError("Gagal inisialisasi pembayaran: " + err);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Midtrans", "Koneksi function gagal: " + t.getMessage());
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

        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY
        })
        @POST("auth/v1/token?grant_type=refresh_token")
        Call<JsonObject> refreshToken(@Body Map<String, Object> body);

        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY
        })
        @POST("auth/v1/recover")
        Call<JsonObject> sendPasswordReset(@Body Map<String, String> body);
    }

    interface ProfileService {
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                // "Authorization: Bearer " + API_KEY, // INI SALAH: Harusnya pakai token user, bukan anon key. Dihapus agar menjadi anon request.
                "Prefer: return=representation"
        })
        @POST("rest/v1/profiles")
        Call<JsonObject> insertProfile(@Body Map<String, Object> body);
        @Headers({
                "apikey: " + API_KEY
        })
        @GET("rest/v1/profiles")
        Call<List<Profile>> getProfile(
                @Header("Authorization") String bearerToken, // Token pengguna
                @Query("id") String userId, // Filter "eq.USER_UUID"
                @Query("select") String select
        );

        @PATCH("rest/v1/profiles")
        Call<Void> updateProfile(
                @Header("Authorization") String bearerToken, // Token pengguna
                @Query("id") String userId, // Filter "eq.USER_UUID"
                @Body Profile profileData // Kirim POJO Profile
        );

        // --- METODE BARU ---
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: return=minimal" // Tidak perlu data kembali
        })
        @PATCH("rest/v1/profiles")
        Call<Void> updateProfileFields(
                @Header("Authorization") String bearerToken, // Token pengguna
                @Query("id") String userId, // Filter "eq.USER_UUID"
                @Body Map<String, Object> profileData // Kirim Map untuk update
        );
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
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: tx=commit;tz=Asia/Jakarta" // Atur zona waktu ke WIB
        })
        @POST("rest/v1/reservasi") // Endpoint bisa apa saja, header yang penting
        Call<Void> setTimeZone(@Header("Authorization") String authToken, @Body Map<String, Object> body);

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getDetailReservasi(
                @Query("id_reservasi") String idQuery,
                @Query("select") String select
        );

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getJadwalAktif(
                @Query("id_pengguna") String idPenggunaQuery,
                @Query("status") String statusQuery,
                @Query("tanggal_pendakian") String tanggalPendakianQuery,
                @Query("select") String select,
                @Query("order") String order,
                @Query("limit") int limit
        );

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/reservasi")
        Call<List<Reservasi>> getReservasiByUser(
                @Query("id_pengguna") String idPenggunaQuery,
                @Query("select") String select,
                @Query("order") String order
        );

        // --- INI ADALAH 3 METHOD BARU UNTUK PLAN B ---

        // 1. Insert ke tabel 'reservasi'
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                // Kita butuh 'service_role' key untuk memanggil rpc() di dalam insert
                // "Authorization: Bearer " + API_KEY, // Ini akan ditimpa oleh @Header
                "Prefer: return=representation,resolution=merge-duplicates"
        })
        @POST("rest/v1/reservasi")
        Call<List<Reservasi>> insertReservasi(
                @Header("Authorization") String authToken, // Ini mengirim token pengguna
                @Body Map<String, Object> body
        );

        // 2. Insert ke 'pendaki_rombongan' (bisa banyak)
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: resolution=merge-duplicates"
        })
        @POST("rest/v1/pendaki_rombongan")
        Call<Void> insertRombongan(
                @Header("Authorization") String authToken,
                @Body List<PendakiRombongan> body
        );

        // 3. Insert ke 'barang_bawaan_sampah' (bisa banyak)
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Prefer: resolution=merge-duplicates"
        })
        @POST("rest/v1/barang_bawaan_sampah")
        Call<Void> insertBarang(
                @Header("Authorization") String authToken,
                @Body List<BarangBawaanSampah> body
        );

        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @POST("rest/v1/rpc/hitung_total_reservasi")
        Call<com.zawww.e_simaksi.model.HitungReservasiResponse> hitungTotalReservasi(
                @Body Map<String, Object> params
        );
    }
    public interface KuotaService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/kuota_harian?select=kuota_maksimal,kuota_terpesan")
        Call<List<KuotaHarian>> getKuota(
                @Query("tanggal_kuota") String tanggalQuery // eg: "eq.2025-10-31"
        );
        @Headers({
                "Content-Type: application/json",
                "apikey: " + API_KEY
        })
        @POST("rest/v1/rpc/cek_dan_ambil_kuota")
        Call<Long> cekDanAmbilKuota(
                @Header("Authorization") String authToken,
                @Body Map<String, Object> body
        );
    }
    public interface StorageService { // Pastikan ini 'public'
        @Headers({
                "apikey: " + API_KEY
                // Hapus "Authorization" yang hardcode dari sini
        })
        @POST("storage/v1/object/surat-sehat/{fileName}")
        Call<JsonObject> uploadFile(
                @Header("Authorization") String authToken, // <-- Ini mengirim token pengguna
                @Path("fileName") String fileName,
                @Body okhttp3.RequestBody fileBody
        );
    }
    public interface PengaturanBiayaService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("rest/v1/pengaturan_biaya?select=nama_item,harga")
        Call<List<PengaturanBiaya>> getSemuaBiaya();
    }
    public interface FunctionService {
        @Headers({
                "Content-Type: application/json",
                "Authorization: Bearer " + API_KEY
        })
        @POST("functions/v1/payment-token")
        Call<JsonObject> getPaymentToken(@Body Map<String, Object> body);
    }
    // ========== CALLBACKS ==========
    public interface AuthCallback {
        void onSuccess(String accessToken, String userId, String refreshToken);
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

    public interface KuotaCallback {
        void onSuccess(KuotaHarian kuota);
        void onError(String errorMessage);
    }

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface GeneralCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface BiayaCallback {
        void onSuccess(List<PengaturanBiaya> biayaList);
        void onError(String errorMessage);
    }

    public interface ProfileCallback {
        void onSuccess(Profile profile);
        void onError(String errorMessage);
    }

    public interface HitungReservasiCallback {
        void onSuccess(com.zawww.e_simaksi.model.HitungReservasiResponse response);
        void onError(String errorMessage);
    }
    public interface TokenCallback {
        void onSuccess(String token, String redirectUrl);
        void onError(String error);
    }
}