package dashboard; // Sesuaikan package Anda

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Reservasi;
import FITURLOGIN.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservasiStep3Fragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private ReservasiFragment parentReservasiFragment;
    private LayoutInflater inflater;
    private LinearLayout layoutListBarang;
    private Button btnTambahBarang;
    private TextView tvRingkasanDetail, tvRingkasanTotal;
    private final List<View> barangViews = new ArrayList<>();
    private SessionManager sessionManager;

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

        layoutListBarang = view.findViewById(R.id.layout_list_barang);
        btnTambahBarang = view.findViewById(R.id.btn_tambah_barang);
        tvRingkasanDetail = view.findViewById(R.id.tv_ringkasan_detail);
        tvRingkasanTotal = view.findViewById(R.id.tv_ringkasan_total);

        Button btnSubmitFinal = requireActivity().findViewById(R.id.btn_buat_reservasi);
        btnSubmitFinal.setOnClickListener(v -> {
            if (validateAndSaveBarang()) {
                startFullSubmissionProcess();
            }
        });

        setupBarangBawaanListeners();
        displayRingkasan();
    }

    private void setupBarangBawaanListeners() {
        btnTambahBarang.setOnClickListener(v -> {
            View barangView = inflater.inflate(R.layout.item_barang, layoutListBarang, false);
            AutoCompleteTextView spinner = barangView.findViewById(R.id.spinner_jenis_sampah);
            String[] jenisSampah = new String[] {"PLASTIK", "LOGAM", "KACA", "LAINNYA"};
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

    private void displayRingkasan() {
        String tanggal = viewModel.tanggalMasuk.getValue();
        int jumlah = viewModel.jumlahPendaki.getValue();
        int total = viewModel.totalHarga.getValue();
        String detail = "Tanggal Masuk: " + tanggal + "\n" +
                "Jumlah Pendaki: " + jumlah + " orang";
        tvRingkasanDetail.setText(detail);
        tvRingkasanTotal.setText("Total: Rp " + total);
    }

    private boolean validateAndSaveBarang() {
        viewModel.listBarang.clear();
        for (View barangView : barangViews) {
            TextInputEditText etNama = barangView.findViewById(R.id.et_nama_barang);
            TextInputEditText etJumlah = barangView.findViewById(R.id.et_jumlah_barang);
            AutoCompleteTextView spinner = barangView.findViewById(R.id.spinner_jenis_sampah);
            String nama = etNama.getText().toString();
            String jumlahStr = etJumlah.getText().toString();
            String jenis = spinner.getText().toString();
            if (nama.isEmpty() || jumlahStr.isEmpty() || jenis.isEmpty()) {
                Toast.makeText(getContext(), "Harap lengkapi semua data barang bawaan", Toast.LENGTH_SHORT).show();
                return false;
            }
            int jumlah = Integer.parseInt(jumlahStr);
            viewModel.listBarang.add(new BarangBawaanSampah(nama, jenis, jumlah));
        }
        Log.d("Step3", "Validasi barang sukses. Total barang: " + viewModel.listBarang.size());
        return true;
    }


    // --- PROSES SUBMIT FINAL ---

    private void startFullSubmissionProcess() {
        Log.d("Step3", "Memulai proses submit...");
        parentReservasiFragment.showLoading(true);

        AtomicInteger uploadCounter = new AtomicInteger(0);
        int totalFiles = viewModel.mapSuratSehat.size();

        if (totalFiles == 0 || totalFiles != viewModel.listPendaki.size()) {
            Log.e("Step3", "Jumlah file (" + totalFiles + ") tidak sama dengan jumlah pendaki (" + viewModel.listPendaki.size() + ").");
            Toast.makeText(getContext(), "Error: Harap upload surat sehat untuk SETIAP pendaki.", Toast.LENGTH_SHORT).show();
            parentReservasiFragment.showLoading(false);
            return;
        }

        // Mulai looping upload
        for (PendakiRombongan pendaki : viewModel.listPendaki) {
            String nik = pendaki.getNik();
            Uri fileUri = viewModel.mapSuratSehat.get(nik);

            String fileExtension = getFileExtension(fileUri);
            String fileName = nik + "_" + System.currentTimeMillis() + "." + fileExtension;
            Log.d("Step3", "Mengupload file untuk " + nik + ": " + fileName);

            // PANGGIL METODE BARU YANG MENANGANI REFRESH SECARA OTOMATIS
            SupabaseAuth.uploadSuratSehatWithRefresh(requireContext(), sessionManager, fileUri, fileName, new SupabaseAuth.UploadCallback() {
                @Override
                public void onSuccess(String publicUrl) {
                    Log.d("Step3", "Upload sukses untuk " + nik + ". URL: " + publicUrl);
                    pendaki.setUrlSuratSehat(publicUrl);

                    if (uploadCounter.incrementAndGet() == totalFiles) {
                        Log.d("Step3", "Semua file berhasil di-upload. Memulai Transaksi Database...");
                        // Ambil token dan user id yang sudah diperbarui dari session untuk langkah selanjutnya
                        String refreshedToken = sessionManager.getAccessToken();
                        String refreshedUserId = sessionManager.getUserId();
                        mulaiTransaksiDatabase(refreshedUserId, refreshedToken);
                    }
                }
                @Override
                public void onError(String errorMessage) {
                    Log.e("Step3", "Gagal upload untuk " + nik + ": " + errorMessage);
                    Toast.makeText(getContext(), "Gagal upload surat sehat untuk " + pendaki.getNamaLengkap() + ". Penyebab: " + errorMessage, Toast.LENGTH_LONG).show();
                    parentReservasiFragment.showLoading(false);
                }
            });
        }
    }

    private void mulaiTransaksiDatabase(String userId, String accessToken) {
        String bearerToken = "Bearer " + accessToken;

        String tanggal = viewModel.tanggalMasuk.getValue();
        int jumlahPendaki = viewModel.jumlahPendaki.getValue();
        int jumlahParkir = viewModel.jumlahParkir.getValue();

        // LANGKAH 1: Panggil RPC 'cek_dan_ambil_kuota'
        Log.d("Step3_Trans", "LANGKAH 1: Cek dan ambil kuota...");
        Map<String, Object> kuotaParams = new HashMap<>();
        kuotaParams.put("p_tanggal", tanggal);
        kuotaParams.put("p_jumlah", jumlahPendaki);

        SupabaseAuth.kuotaService.cekDanAmbilKuota(bearerToken, kuotaParams).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Kuota Habis";
                        Log.e("Step3_Trans", "Gagal ambil kuota: " + err);
                        Toast.makeText(getContext(), "Gagal: " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Gagal: Kuota tidak mencukupi.", Toast.LENGTH_LONG).show();
                    }
                    parentReservasiFragment.showLoading(false);
                    return;
                }

                long idKuotaBaru = response.body();
                Log.d("Step3_Trans", "LANGKAH 1 Sukses. ID Kuota: " + idKuotaBaru);

                // LANGKAH 2: INSERT ke tabel 'reservasi'
                String kodeReservasi = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Map<String, Object> reservasiBody = new HashMap<>();
                reservasiBody.put("id_pengguna", userId);
                reservasiBody.put("id_kuota", idKuotaBaru);
                reservasiBody.put("kode_reservasi", kodeReservasi);
                reservasiBody.put("tanggal_pendakian", tanggal);
                reservasiBody.put("tanggal_keluar", viewModel.tanggalKeluar.getValue());
                reservasiBody.put("jumlah_pendaki", jumlahPendaki);
                reservasiBody.put("jumlah_tiket_parkir", jumlahParkir);
                reservasiBody.put("total_harga", "rpc(hitung_total_harga, p_jumlah_pendaki:=" + jumlahPendaki + ", p_jumlah_parkir:=" + jumlahParkir + ")");
                reservasiBody.put("status", "menunggu_pembayaran");

                Log.d("Step3_Trans", "LANGKAH 2: Insert ke tabel reservasi...");
                SupabaseAuth.reservasiService.insertReservasi(bearerToken, reservasiBody).enqueue(new Callback<List<Reservasi>>() {
                    @Override
                    public void onResponse(Call<List<Reservasi>> call, Response<List<Reservasi>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            Log.e("Step3_Trans", "Gagal insert reservasi: " + response.message());
                            Toast.makeText(getContext(), "Gagal membuat reservasi.", Toast.LENGTH_LONG).show();
                            parentReservasiFragment.showLoading(false);
                            // TODO: Buat fungsi RPC 'kembalikan_kuota(idKuotaBaru, jumlahPendaki)'
                            return;
                        }

                        Reservasi reservasiBaru = response.body().get(0);
                        long idReservasiBaru = reservasiBaru.getIdReservasi();
                        Log.d("Step3_Trans", "LANGKAH 2 Sukses. ID Reservasi: " + idReservasiBaru);

                        // LANGKAH 3: Set ID reservasi ke semua objek pendaki & barang
                        for (PendakiRombongan pendaki : viewModel.listPendaki) {
                            pendaki.setIdReservasi(idReservasiBaru);
                        }
                        for (BarangBawaanSampah barang : viewModel.listBarang) {
                            barang.setIdReservasi(idReservasiBaru);
                        }

                        // LANGKAH 4: INSERT Rombongan & Barang
                        insertRombonganDanBarang(bearerToken);
                    }
                    @Override
                    public void onFailure(Call<List<Reservasi>> call, Throwable t) {
                        Log.e("Step3_Trans", "Koneksi Gagal (Reservasi): " + t.getMessage());
                        parentReservasiFragment.showLoading(false);
                    }
                });
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("Step3_Trans", "Koneksi Gagal (Kuota RPC): " + t.getMessage());
                parentReservasiFragment.showLoading(false);
            }
        });
    }

    // --- METHOD YANG HILANG (TAMBAHKAN INI) ---
    private void insertRombonganDanBarang(String bearerToken) {
        Log.d("Step3_Trans", "LANGKAH 3: Insert " + viewModel.listPendaki.size() + " pendaki...");

        SupabaseAuth.reservasiService.insertRombongan(bearerToken, viewModel.listPendaki).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("Step3_Trans", "Gagal insert rombongan: " + response.message());
                    parentReservasiFragment.showLoading(false);
                    return;
                }

                Log.d("Step3_Trans", "LANGKAH 3 Sukses.");

                if (viewModel.listBarang.isEmpty()) {
                    Log.d("Step3_Trans", "Tidak ada barang. Selesai.");
                    reservasiSukses();
                    return;
                }

                Log.d("Step3_Trans", "LANGKAH 4: Insert " + viewModel.listBarang.size() + " barang...");
                SupabaseAuth.reservasiService.insertBarang(bearerToken, viewModel.listBarang).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Log.e("Step3_Trans", "Gagal insert barang: " + response.message());
                            parentReservasiFragment.showLoading(false);
                            return;
                        }
                        Log.d("Step3_Trans", "LANGKAH 4 Sukses.");
                        reservasiSukses();
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Step3_Trans", "Koneksi Gagal (Barang): " + t.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Step3_Trans", "Koneksi Gagal (Rombongan): " + t.getMessage());
            }
        });
    }

    // --- METHOD YANG HILANG (TAMBAHKAN INI) ---
    private void reservasiSukses() {
        parentReservasiFragment.showLoading(false);
        Log.d("Step3_Trans", "RESERVASI BERHASIL DIBUAT!");
        Toast.makeText(getContext(), "Reservasi berhasil dibuat!", Toast.LENGTH_LONG).show();
        // Kembali ke Home
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private String getFileExtension(Uri uri) {
        if (uri == null) return "jpg"; // Default
        try {
            ContentResolver cR = requireContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));
            if (type == null || type.isEmpty()) {
                String path = uri.getPath();
                if (path != null) {
                    String[] parts = path.split("\\.");
                    if (parts.length > 1) {
                        return parts[parts.length - 1];
                    }
                }
                return "bin";
            }
            return type;
        } catch (Exception e) {
            Log.e("Step3", "Gagal dapat ekstensi file, pakai .jpg", e);
            return "jpg";
        }
    }
}