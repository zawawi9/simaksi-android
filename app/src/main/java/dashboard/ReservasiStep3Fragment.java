package dashboard;

import android.content.ContentResolver;
import android.content.Context;
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
import com.zawww.e_simaksi.api.SupabaseAuth; // PENTING
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.BuatReservasiRequest;
import com.zawww.e_simaksi.model.PendakiRombongan;
import FITURLOGIN.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReservasiStep3Fragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private ReservasiFragment parentReservasiFragment;
    private LayoutInflater inflater;

    // Views
    private LinearLayout layoutListBarang;
    private Button btnTambahBarang;
    private TextView tvRingkasanDetail, tvRingkasanTotal;

    private final List<View> barangViews = new ArrayList<>();
    private SessionManager sessionManager; // Variabel untuk SessionManager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.layout_step3_sampah, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hubungkan ke ViewModel Induk
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservasiSharedViewModel.class);
        parentReservasiFragment = (ReservasiFragment) requireParentFragment();

        // Inisialisasi SessionManager
        sessionManager = new SessionManager(requireContext());

        // Binding
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
        parentReservasiFragment.showLoading(true); // Tampilkan loading

        // PERBAIKAN: Ambil User ID (untuk RPC) dan Access Token (untuk Upload)
        String currentUserId = sessionManager.getUserId();
        String accessToken = sessionManager.getAccessToken();

        if (currentUserId == null || accessToken == null) {
            Log.e("Step3", "Submit GAGAL: User ID atau Access Token tidak ditemukan di session.");
            Toast.makeText(getContext(), "Sesi Anda habis. Silakan login kembali.", Toast.LENGTH_LONG).show();
            parentReservasiFragment.showLoading(false);
            // TODO: Navigasi paksa ke LoginActivity
            return;
        }

        AtomicInteger uploadCounter = new AtomicInteger(0);
        int totalFiles = viewModel.mapSuratSehat.size();

        if (totalFiles == 0) {
            Log.e("Step3", "Tidak ada file surat sehat untuk di-upload.");
            parentReservasiFragment.showLoading(false);
            Toast.makeText(getContext(), "Error: Tidak ada data surat sehat.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mulai looping upload
        for (PendakiRombongan pendaki : viewModel.listPendaki) {
            String nik = pendaki.getNik();
            Uri fileUri = viewModel.mapSuratSehat.get(nik);

            String fileExtension = getFileExtension(fileUri);
            String fileName = nik + "_" + System.currentTimeMillis() + "." + fileExtension;

            Log.d("Step3", "Mengupload file untuk " + nik + ": " + fileName);

            // PERBAIKAN: Kirim 'accessToken' yang valid ke method upload
            SupabaseAuth.uploadSuratSehat(requireContext(), accessToken, fileUri, fileName, new SupabaseAuth.UploadCallback() {
                @Override
                public void onSuccess(String publicUrl) {
                    Log.d("Step3", "Upload sukses untuk " + nik + ". URL: " + publicUrl);
                    pendaki.setUrlSuratSehat(publicUrl);

                    if (uploadCounter.incrementAndGet() == totalFiles) {
                        Log.d("Step3", "Semua file berhasil di-upload. Membuat reservasi...");
                        // Kirim 'currentUserId' yang valid
                        createReservasiObject(currentUserId);
                        // Hapus baris di bawah ini jika masih ada
                        // createReservasiObject(currentUserId);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("Step3", "Gagal upload untuk " + nik + ": " + errorMessage);
                    Toast.makeText(getContext(), "Gagal upload surat sehat untuk " + pendaki.getNamaLengkap(), Toast.LENGTH_LONG).show();
                    parentReservasiFragment.showLoading(false);
                }
            });
        }
    }

    private void createReservasiObject(String userId) {
        String tanggalKeluar = viewModel.tanggalKeluar.getValue();
        if (tanggalKeluar == null || tanggalKeluar.isEmpty()) {
            tanggalKeluar = viewModel.tanggalMasuk.getValue();
        }

        BuatReservasiRequest request = new BuatReservasiRequest(
                userId,
                viewModel.tanggalMasuk.getValue(),
                tanggalKeluar,
                viewModel.jumlahPendaki.getValue(),
                0, // Jumlah Parkir (hardcode 0)
                null, // Kode Promo (hardcode null)
                viewModel.listPendaki,
                viewModel.listBarang
        );

        SupabaseAuth.kirimReservasi(request, new SupabaseAuth.GeneralCallback() {
            @Override
            public void onSuccess() {
                parentReservasiFragment.showLoading(false);
                Log.d("Step3", "RESERVASI BERHASIL DIBUAT!");
                Toast.makeText(getContext(), "Reservasi berhasil dibuat!", Toast.LENGTH_LONG).show();

                // Navigasi kembali ke Home (bersihkan stack)
                // Kita gunakan getParentFragmentManager() karena ini adalah child fragment
                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            @Override
            public void onError(String errorMessage) {
                parentReservasiFragment.showLoading(false);
                Log.e("Step3", "Gagal RPC: " + errorMessage);
                Toast.makeText(getContext(), "Gagal membuat reservasi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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