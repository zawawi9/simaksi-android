package com.zawww.e_simaksi.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent; // Penting buat buka browser
import android.graphics.Color;
import android.net.Uri; // Penting buat parsing URL
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.HitungReservasiResponse;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Reservasi;
import com.zawww.e_simaksi.util.SessionManager;
import com.zawww.e_simaksi.viewmodel.ReservasiSharedViewModel;

// HAPUS IMPORT MIDTRANS SDK (KITA GAK PAKE LAGI)
// import com.midtrans.sdk... (DIBUANG)

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.zawww.e_simaksi.util.ErrorHandler;



public class ReservasiStep3Fragment extends Fragment {



    private ReservasiSharedViewModel viewModel;

    private ReservasiFragment parentReservasiFragment;

    private LayoutInflater inflater;

    private SessionManager sessionManager;



    // UI Components

    private LinearLayout layoutListBarang;

    private Button btnTambahBarang;

    private TextView tvRingkasanDetail, tvRingkasanTotal;



    // UI Components PROMO

    private EditText etKodePromo;

    private Button btnCekPromo;

    private TextView tvPotonganHarga;



    private final List<View> barangViews = new ArrayList<>();



    // Variables Data Transaksi

    private Long idPromosiTerpilih = null;

    private long finalTotalBayar = 0;

    private long finalHargaAwal = 0;

    private long finalNominalDiskon = 0;



    @Nullable

    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.inflater = inflater;

        return inflater.inflate(R.layout.layout_step3_sampah, container, false);

    }



    @Override

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);



        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservasiSharedViewModel.class);

        parentReservasiFragment = (ReservasiFragment) requireParentFragment();

        sessionManager = new SessionManager(requireContext());



        // Binding View

        layoutListBarang = view.findViewById(R.id.layout_list_barang);

        btnTambahBarang = view.findViewById(R.id.btn_tambah_barang);

        tvRingkasanDetail = view.findViewById(R.id.tv_ringkasan_detail);

        tvRingkasanTotal = view.findViewById(R.id.tv_ringkasan_total);

        etKodePromo = view.findViewById(R.id.et_kode_promo);

        btnCekPromo = view.findViewById(R.id.btn_cek_promo);

        tvPotonganHarga = view.findViewById(R.id.tv_potongan_harga);



        Button btnSubmitFinal = requireActivity().findViewById(R.id.btn_buat_reservasi);

        btnSubmitFinal.setOnClickListener(v -> {

            if (validateAndSaveBarang()) {

                startFullSubmissionProcess();

            }

        });



        setupBarangBawaanListeners();



        // HAPUS KODINGAN SdkUIFlowBuilder.init() DISINI

        // KITA GAK PERLU INIT APA-APA KARENA CUMA MAU BUKA LINK



        // Listener Tombol Cek Promo

        btnCekPromo.setOnClickListener(v -> cekPromo());



        // Load awal

        cekPromoInternal("");

    }



    private void setupBarangBawaanListeners() {

        // ... (Kode sama persis seperti sebelumnya) ...

        btnTambahBarang.setOnClickListener(v -> {

            View barangView = inflater.inflate(R.layout.item_barang, layoutListBarang, false);

            AutoCompleteTextView spinner = barangView.findViewById(R.id.spinner_jenis_sampah);

            String[] jenisSampah = new String[] {"Organik", "Anorganik"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(

                    requireContext(),

                    android.R.layout.simple_dropdown_item_1line,

                    jenisSampah

            );

            spinner.setAdapter(adapter);

            ImageButton btnHapus = barangView.findViewById(R.id.btn_hapus_barang);

            btnHapus.setOnClickListener(vHapus -> {

                layoutListBarang.removeView(barangView);

                barangViews.remove(barangView);

            });

            barangViews.add(barangView);

            layoutListBarang.addView(barangView);

        });

    }



    private void cekPromo() {

        String kode = etKodePromo.getText().toString().trim();

        if (kode.isEmpty()) {

            etKodePromo.setError("Masukkan kode promo");

            return;

        }

        cekPromoInternal(kode);

    }



    private void cekPromoInternal(String kodePromo) {

        // ... (Kode sama persis seperti sebelumnya) ...

        int jumlahPendaki = viewModel.jumlahPendaki.getValue() != null ? viewModel.jumlahPendaki.getValue() : 1;

        int jumlahParkir = viewModel.jumlahParkir.getValue() != null ? viewModel.jumlahParkir.getValue() : 0;



        SupabaseAuth.hitungTotalReservasi(jumlahPendaki, jumlahParkir, kodePromo, new SupabaseAuth.HitungReservasiCallback() {

            @Override

            public void onSuccess(HitungReservasiResponse response) {

                if (getActivity() == null) return;



                Locale localeID = new Locale("in", "ID");

                NumberFormat formatRp = NumberFormat.getCurrencyInstance(localeID);

                formatRp.setMaximumFractionDigits(0);



                finalTotalBayar = response.getTotalAkhir();

                finalHargaAwal = response.getHargaAwal();

                finalNominalDiskon = response.getNominalDiskon();

                idPromosiTerpilih = response.getIdPromosiApplied();



                String detail = "Subtotal: " + formatRp.format(finalHargaAwal);

                tvRingkasanDetail.setText(detail);

                tvRingkasanTotal.setText("Total Bayar: " + formatRp.format(finalTotalBayar));



                if (finalNominalDiskon > 0) {

                    tvPotonganHarga.setVisibility(View.VISIBLE);

                    tvPotonganHarga.setText("Diskon: - " + formatRp.format(finalNominalDiskon));

                    tvPotonganHarga.setTextColor(Color.parseColor("#69F0AE"));

                    if (!kodePromo.isEmpty()) {

                        Toast.makeText(getContext(), "Promo Berhasil Digunakan!", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    tvPotonganHarga.setVisibility(View.GONE);

                    if (!kodePromo.isEmpty()) {

                        etKodePromo.setError("Kode Promo tidak valid");

                        ErrorHandler.showError(requireView(), "Kode promo tidak valid");

                    }

                }

            }



            @Override

            public void onError(String errorMessage) {

                if (getActivity() != null) {

                    ErrorHandler.showError(requireView(), errorMessage);

                }

            }

        });

    }



    private boolean validateAndSaveBarang() {

        // ... (Kode sama persis seperti sebelumnya) ...

        viewModel.listBarang.clear();

        for (View barangView : barangViews) {

            TextInputEditText etNama = barangView.findViewById(R.id.et_nama_barang);

            TextInputEditText etJumlah = barangView.findViewById(R.id.et_jumlah_barang);

            AutoCompleteTextView spinner = barangView.findViewById(R.id.spinner_jenis_sampah);

            String nama = etNama.getText().toString();

            String jumlahStr = etJumlah.getText().toString();

            String jenis = spinner.getText().toString();

            if (nama.isEmpty() || jumlahStr.isEmpty() || jenis.isEmpty()) {

                ErrorHandler.showError(requireView(), "Harap lengkapi data barang");

                return false;

            }

            int jumlah = Integer.parseInt(jumlahStr);

            viewModel.listBarang.add(new BarangBawaanSampah(nama, jenis, jumlah));

        }

        return true;

    }



    private void startFullSubmissionProcess() {

        // ... (Kode sama persis sampai pemanggilan database) ...

        Log.d("Step3", "Memulai proses submit...");

        parentReservasiFragment.showLoading(true);



        AtomicInteger uploadCounter = new AtomicInteger(0);

        int totalFiles = viewModel.mapSuratSehat.size();



        if (totalFiles == 0 || totalFiles != viewModel.listPendaki.size()) {

            ErrorHandler.showError(requireView(), "Harap upload surat sehat untuk SETIAP pendaki.");

            parentReservasiFragment.showLoading(false);

            return;

        }



        for (PendakiRombongan pendaki : viewModel.listPendaki) {

            String nik = pendaki.getNik();

            Uri fileUri = viewModel.mapSuratSehat.get(nik);

            String fileName = nik + "_" + System.currentTimeMillis() + ".jpg"; // Simplifikasi ekstensi



            SupabaseAuth.uploadSuratSehatWithRefresh(requireContext(), sessionManager, fileUri, fileName, new SupabaseAuth.UploadCallback() {

                @Override

                public void onSuccess(String publicUrl) {

                    pendaki.setUrlSuratSehat(publicUrl);

                    if (uploadCounter.incrementAndGet() == totalFiles) {

                        String refreshedToken = sessionManager.getAccessToken();

                        String refreshedUserId = sessionManager.getUserId();

                        mulaiTransaksiDatabase(refreshedUserId, refreshedToken);

                    }

                }

                @Override

                public void onError(String errorMessage) {

                    parentReservasiFragment.showLoading(false);

                    ErrorHandler.showError(requireView(), "Gagal upload surat: " + errorMessage);

                }

            });

        }

    }



    private void mulaiTransaksiDatabase(String userId, String accessToken) {

        // ... (Kode sama persis) ...

        String bearerToken = "Bearer " + accessToken;

        String tanggal = viewModel.tanggalMasuk.getValue();

        int jumlahPendaki = viewModel.jumlahPendaki.getValue() != null ? viewModel.jumlahPendaki.getValue() : 1;



        Map<String, Object> kuotaParams = new HashMap<>();

        kuotaParams.put("p_tanggal", tanggal);

        kuotaParams.put("p_jumlah", jumlahPendaki);



        SupabaseAuth.kuotaService.cekDanAmbilKuota(bearerToken, kuotaParams).enqueue(new Callback<Long>() {

            @Override

            public void onResponse(Call<Long> call, Response<Long> response) {

                if (!response.isSuccessful() || response.body() == null) {

                    parentReservasiFragment.showLoading(false);

                    ErrorHandler.showError(requireView(), "Kuota Habis/Gagal");

                    return;

                }

                long idKuotaBaru = response.body();



                String kodeReservasi = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Map<String, Object> reservasiBody = new HashMap<>();

                reservasiBody.put("id_pengguna", userId);

                reservasiBody.put("id_kuota", idKuotaBaru);

                reservasiBody.put("kode_reservasi", kodeReservasi);

                reservasiBody.put("tanggal_pendakian", tanggal);

                reservasiBody.put("tanggal_keluar", viewModel.tanggalKeluar.getValue());

                reservasiBody.put("jumlah_pendaki", jumlahPendaki);

                reservasiBody.put("jumlah_parkir", viewModel.jumlahParkir.getValue() != null ? viewModel.jumlahParkir.getValue() : 0);

                reservasiBody.put("status", "menunggu_pembayaran");

                reservasiBody.put("total_harga", finalTotalBayar); // Harga Akhir

                // reservasiBody.put("id_promosi", idPromosiTerpilih.intValue()); // Kolom ini tidak ada di tabel 'reservasi' di database, jadi tidak perlu dikirim.



                SupabaseAuth.reservasiService.insertReservasi(bearerToken, reservasiBody).enqueue(new Callback<List<Reservasi>>() {

                    @Override

                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {

                        if (!response.isSuccessful() || response.body() == null) {

                            parentReservasiFragment.showLoading(false);

                            ErrorHandler.showError(requireView(), "Gagal simpan reservasi");

                            return;

                        }

                        Reservasi reservasiBaru = response.body().get(0);

                        long idReservasiBaru = reservasiBaru.getIdReservasi();



                        for (PendakiRombongan pendaki : viewModel.listPendaki) pendaki.setIdReservasi(idReservasiBaru);

                        for (BarangBawaanSampah barang : viewModel.listBarang) barang.setIdReservasi(idReservasiBaru);



                        insertRombonganDanBarang(bearerToken, reservasiBaru);

                    }

                    @Override

                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {

                        parentReservasiFragment.showLoading(false);

                        ErrorHandler.showError(requireView(), "Gagal simpan reservasi: " + t.getMessage());

                    }

                });

            }

            @Override

            public void onFailure(Call<Long> call, Throwable t) {

                parentReservasiFragment.showLoading(false);

                ErrorHandler.showError(requireView(), "Gagal cek kuota: " + t.getMessage());

            }

        });

    }



    private void insertRombonganDanBarang(String bearerToken, Reservasi dataReservasi) {
        long idReservasiBaru = dataReservasi.getIdReservasi();

        Runnable triggerPayment = () -> {
            Log.d("Step3", "Data pendukung berhasil disimpan. Mengambil ulang data reservasi untuk pembayaran...");
            SupabaseAuth.getDetailReservasi((int) idReservasiBaru, new SupabaseAuth.DetailReservasiCallback() {
                @Override
                public void onSuccess(Reservasi reservasiFromDb) {
                    Log.d("Step3", "Reservasi terkonfirmasi dari DB. Memulai proses pembayaran.");
                    reservasiSukses(reservasiFromDb.getKodeReservasi(), reservasiFromDb.getTotalHarga());
                }

                @Override
                public void onError(String errorMessage) {
                    parentReservasiFragment.showLoading(false);
                    ErrorHandler.showError(requireView(), "Gagal memproses pembayaran: " + errorMessage);
                    Log.e("Step3", "Gagal mengambil ulang detail reservasi: " + errorMessage);
                }
            });
        };

        // Error handler
        java.util.function.Consumer<String> onError = errorMessage -> {
            parentReservasiFragment.showLoading(false);
            ErrorHandler.showError(requireView(), errorMessage);
            Log.e("Step3", "Error selama batch insert: " + errorMessage);
            // TODO: Pertimbangkan untuk menghapus reservasi yang sudah terbuat sebagian
        };

        // 1. Insert Rombongan in batches
        insertDataInBatches(
                bearerToken,
                viewModel.listPendaki,
                SupabaseAuth.reservasiService::insertRombongan,
                () -> {
                    // 2. On successful rombongan insertion, insert Barang in batches
                    insertDataInBatches(
                            bearerToken,
                            viewModel.listBarang,
                            SupabaseAuth.reservasiService::insertBarang,
                            triggerPayment, // 3. On successful barang insertion, trigger payment
                            onError
                    );
                },
                onError
        );
    }

    private <T> void insertDataInBatches(
            String bearerToken,
            List<T> list,
            java.util.function.BiFunction<String, List<T>, Call<Void>> apiCallFunction,
            Runnable onComplete,
            java.util.function.Consumer<String> onError
    ) {
        if (list == null || list.isEmpty()) {
            onComplete.run();
            return;
        }

        final int BATCH_SIZE = 20;
        int totalBatches = (int) Math.ceil((double) list.size() / BATCH_SIZE);
        AtomicInteger successfulBatches = new AtomicInteger(0);
        AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);


        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, list.size());
            List<T> batch = new ArrayList<>(list.subList(i, end));

            apiCallFunction.apply(bearerToken, batch).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (hasErrorOccurred.get()) return;

                    if (response.isSuccessful()) {
                        if (successfulBatches.incrementAndGet() == totalBatches) {
                            Log.d("Step3_Batch", "Semua batch berhasil disimpan.");
                            onComplete.run();
                        }
                    } else {
                        if (hasErrorOccurred.compareAndSet(false, true)) {
                            String errorMsg = "Gagal menyimpan batch. Kode: " + response.code() + ", Pesan: " + response.message();
                             try {
                                if (response.errorBody() != null) {
                                    errorMsg += ", Detail: " + response.errorBody().string();
                                }
                            } catch (java.io.IOException e) {
                                Log.e("Step3_Batch", "Error parsing error body", e);
                            }
                            onError.accept(errorMsg);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    if (hasErrorOccurred.compareAndSet(false, true)) {
                        onError.accept("Koneksi gagal saat menyimpan batch: " + t.getMessage());
                    }
                }
            });
        }
    }



    // === INI METODE KUNCI UNTUK WEB REDIRECT ===

    private void reservasiSukses(String kodeReservasi, long totalBayar) {

        Log.d("Step3", "Minta URL Payment untuk: " + kodeReservasi);



        // Panggil SupabaseAuth (yang sudah kita update tadi)

        SupabaseAuth.getSnapToken(kodeReservasi, totalBayar, new SupabaseAuth.TokenCallback() {

            @Override

            public void onSuccess(String snapToken, String redirectUrl) {

                parentReservasiFragment.showLoading(false);



                // Cek URL ada atau tidak

                if (redirectUrl != null && !redirectUrl.isEmpty()) {



                    String urlIndo = redirectUrl;



                    // Cek apakah URL sudah punya parameter lain (?)

                    if (urlIndo.contains("?")) {

                        urlIndo += "&language=id";

                    } else {

                        urlIndo += "?language=id";

                    }

                    Log.d("Step3", "Membuka Browser: " + redirectUrl);



                    // BUKA BROWSER / CHROME TAB

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));

                    startActivity(intent);



                } else {

                    ErrorHandler.showError(requireView(), "Gagal mendapatkan Link Pembayaran");

                }

            }



            @Override

            public void onError(String error) {

                parentReservasiFragment.showLoading(false);

                ErrorHandler.showError(requireView(), "Error Payment: " + error);

            }

        });

    }

}
