package com.zawww.e_simaksi.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.zawww.e_simaksi.viewmodel.ReservasiSharedViewModel;

import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.PendakiRombongan;
// Impor SupabaseAuth jika Anda perlu mengambil data user
// import com.zawww.e_simaksi.api.SupabaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReservasiStep2Fragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private LinearLayout layoutListPendaki;
    private LayoutInflater inflater;

    // Data Ketua
    private TextInputEditText etKetuaNama, etKetuaNik, etKetuaTelepon, etKetuaDarurat, etKetuaAlamat;
    private Button btnUploadSuratKetua;
    private TextView tvNamaFileSuratKetua;

    // List untuk menyimpan referensi ke view anggota (agar datanya bisa diambil)
    private final List<View> anggotaViews = new ArrayList<>();

    // Launcher untuk file. Ini triknya: kita butuh tahu tombol mana yang diklik
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Button currentUploadButton; // Tombol yang sedang aktif
    private TextView currentFileTextView; // TextView yang sedang aktif
    private String currentPendakiIdentifier; // NIK atau "KETUA"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater; // Simpan inflater
        return inflater.inflate(R.layout.layout_step2_rombongan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hubungkan ke ViewModel Induk
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservasiSharedViewModel.class);

        // Binding Views Ketua
        layoutListPendaki = view.findViewById(R.id.layout_list_pendaki);
        etKetuaNama = view.findViewById(R.id.et_ketua_nama);
        etKetuaNik = view.findViewById(R.id.et_ketua_nik);
        etKetuaTelepon = view.findViewById(R.id.et_ketua_telepon);
        etKetuaDarurat = view.findViewById(R.id.et_ketua_darurat);
        etKetuaAlamat = view.findViewById(R.id.et_ketua_alamat);
        btnUploadSuratKetua = view.findViewById(R.id.btn_upload_surat_ketua);
        tvNamaFileSuratKetua = view.findViewById(R.id.tv_nama_file_surat_ketua);

        // Setup File Picker
        setupFilePickerLauncher();

        // Muat data user yang login (Ketua)
        loadDataKetua();

        // Buat form dinamis untuk anggota
        generateAnggotaForms();
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            // Tampilkan nama file di TextView yang benar
                            currentFileTextView.setText("File: " + fileUri.getLastPathSegment());
                            currentFileTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                            // Simpan URI ke ViewModel
                            viewModel.mapSuratSehat.put(currentPendakiIdentifier, fileUri);
                            Log.d("Step2", "File URI disimpan untuk " + currentPendakiIdentifier);
                        }
                    }
                });
    }

    private void loadDataKetua() {
        // Ambil data user dari SupabaseAuth Anda
        // String nama = SupabaseAuth.getCurrentUser().getName(); // (Contoh)
        // String email = SupabaseAuth.getCurrentUser().getEmail(); // (Contoh)

        // Simulasi:
        etKetuaNama.setText("Zawawi"); // Isi dengan data asli
        etKetuaNama.setEnabled(false); // Kunci nama ketua

        // Atur listener upload untuk KETUA
        btnUploadSuratKetua.setOnClickListener(v -> {
            String nikKetua = etKetuaNik.getText().toString();
            if (nikKetua.isEmpty()) {
                etKetuaNik.setError("NIK Ketua harus diisi dulu");
                return;
            }
            // Set variabel global sebelum meluncurkan picker
            currentUploadButton = btnUploadSuratKetua;
            currentFileTextView = tvNamaFileSuratKetua;
            currentPendakiIdentifier = nikKetua; // Gunakan NIK sebagai key unik
            launchFilePicker();
        });
    }

    private void generateAnggotaForms() {
        int jumlahPendaki = viewModel.jumlahPendaki.getValue();
        if (jumlahPendaki <= 1) {
            // Tidak ada anggota, hanya ketua
            return;
        }

        // Hapus form lama jika ada (untuk jaga-jaga)
        layoutListPendaki.removeAllViews();
        anggotaViews.clear();

        // Buat form untuk anggota (jumlah - 1)
        for (int i = 1; i < jumlahPendaki; i++) {
            View anggotaView = inflater.inflate(R.layout.item_pendaki, layoutListPendaki, false);

            // Ambil komponen di dalam card anggota
            TextView tvJudulAnggota = anggotaView.findViewById(R.id.tv_judul_anggota);
            TextInputEditText etNikAnggota = anggotaView.findViewById(R.id.et_nik_pendaki);
            Button btnUploadAnggota = anggotaView.findViewById(R.id.btn_upload_surat_anggota);
            TextView tvFileAnggota = anggotaView.findViewById(R.id.tv_nama_file_surat_anggota);

            tvJudulAnggota.setText("Data Anggota " + (i + 1)); // Set judul (Anggota 2, Anggota 3, dst)

            // Atur listener upload untuk ANGGOTA
            btnUploadAnggota.setOnClickListener(v -> {
                String nikAnggota = etNikAnggota.getText().toString();
                if (nikAnggota.isEmpty()) {
                    etNikAnggota.setError("NIK Anggota harus diisi dulu");
                    return;
                }
                // Set variabel global
                currentUploadButton = btnUploadAnggota;
                currentFileTextView = tvFileAnggota;
                currentPendakiIdentifier = nikAnggota; // NIK anggota
                launchFilePicker();
            });

            // Tambahkan view ini ke list & layout
            anggotaViews.add(anggotaView);
            layoutListPendaki.addView(anggotaView);
        }
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Semua jenis file
        String[] mimeTypes = {"image/jpeg", "image/png", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    // Fungsi ini akan dipanggil oleh Fragment INDUK sebelum pindah ke Langkah 3
    // Ini adalah tempat kita memvalidasi dan menyimpan data ke ViewModel
    public boolean validateAndSaveData() {
        viewModel.listPendaki.clear(); // Kosongkan list lama

        // 1. Validasi & Ambil Data Ketua
        String namaKetua = etKetuaNama.getText().toString();
        String nikKetua = etKetuaNik.getText().toString();
        String telpKetua = etKetuaTelepon.getText().toString();
        String daruratKetua = etKetuaDarurat.getText().toString();
        String alamatKetua = etKetuaAlamat.getText().toString();

        if (nikKetua.isEmpty() || telpKetua.isEmpty() || daruratKetua.isEmpty() || alamatKetua.isEmpty()) {
            Toast.makeText(getContext(), "Harap lengkapi semua data Ketua", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!viewModel.mapSuratSehat.containsKey(nikKetua)) {
            Toast.makeText(getContext(), "Harap upload surat sehat Ketua", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Tambahkan Ketua ke list
        PendakiRombongan ketua = new PendakiRombongan(namaKetua, nikKetua, alamatKetua, telpKetua, daruratKetua);
        viewModel.listPendaki.add(ketua);

        // 2. Validasi & Ambil Data Anggota
        for (View anggotaView : anggotaViews) {
            TextInputEditText etNama = anggotaView.findViewById(R.id.et_nama_pendaki);
            TextInputEditText etNik = anggotaView.findViewById(R.id.et_nik_pendaki);

            // PERBAIKAN: Gunakan getEditText() dari TextInputLayout
            com.google.android.material.textfield.TextInputLayout tilTelp = anggotaView.findViewById(R.id.til_telepon_pendaki);
            com.google.android.material.textfield.TextInputLayout tilDarurat = anggotaView.findViewById(R.id.til_darurat_pendaki);
            com.google.android.material.textfield.TextInputLayout tilAlamat = anggotaView.findViewById(R.id.til_alamat_pendaki);

            TextInputEditText etTelp = (TextInputEditText) tilTelp.getEditText();
            TextInputEditText etDarurat = (TextInputEditText) tilDarurat.getEditText();
            TextInputEditText etAlamat = (TextInputEditText) tilAlamat.getEditText();

            // Pastikan EditText tidak null sebelum digunakan
            if (etNama == null || etNik == null || etTelp == null || etDarurat == null || etAlamat == null) {
                Log.e("Step2", "Salah satu EditText di item_pendaki.xml null!");
                Toast.makeText(getContext(), "Terjadi error internal pada form.", Toast.LENGTH_SHORT).show();
                return false; // Hentikan proses jika ada error
            }

            String nama = etNama.getText().toString();
            String nik = etNik.getText().toString();
            String telp = etTelp.getText().toString();
            String darurat = etDarurat.getText().toString();
            String alamat = etAlamat.getText().toString();

            if (nama.isEmpty() || nik.isEmpty() || telp.isEmpty() || darurat.isEmpty() || alamat.isEmpty()) {
                Toast.makeText(getContext(), "Harap lengkapi data untuk anggota " + (viewModel.listPendaki.size() + 1), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!viewModel.mapSuratSehat.containsKey(nik)) {
                Toast.makeText(getContext(), "Harap upload surat sehat untuk " + nama, Toast.LENGTH_SHORT).show();
                return false;
            }

            // Tambahkan Anggota ke list
            PendakiRombongan anggota = new PendakiRombongan(nama, nik, alamat, telp, darurat);
            viewModel.listPendaki.add(anggota);
        }

        Log.d("Step2", "Validasi sukses. Total pendaki disimpan: " + viewModel.listPendaki.size());
        return true; // Semua data valid
    }

    // Kita harus sedikit memodifikasi ReservasiFragment
    // (Akan saya jelaskan setelah ini)
}