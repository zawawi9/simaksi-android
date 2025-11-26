package com.zawww.e_simaksi.ui.fragment; // Pastikan package ini sesuai

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import untuk dialog
import androidx.fragment.app.Fragment;

// Import semua yang kita perlukan
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.Profile;
import com.zawww.e_simaksi.util.SessionManager; // Ganti ini jika path SessionManager Anda beda
import com.zawww.e_simaksi.ui.activity.LoginActivity; // Import untuk Logout

public class ProfileFragment extends Fragment {

    // [BARU] Deklarasi semua View (gaya findViewById)
    private TextView tvNamaLengkapHeader, tvStatusPeran, tvNik, tvNamaLengkapDetail, tvTanggalLahir, tvEmail, tvNomorTelepon, tvAlamat;
    private ImageButton btnEditProfil;
    private TextView btnUbahPassword;
    private Button btnLogout;

    private SessionManager sessionManager;
    private Profile currentProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // [DIUBAH] Kita inflate layout-nya secara normal
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            Log.e("ProfileFragment", "Context is null in onViewCreated");
            return;
        }

        initViews(view); // Panggil metode initViews
        sessionManager = new SessionManager(requireContext());
        setupButtonListeners(); // Atur klik tombol
    }

    /**
     * [BARU] Metode untuk mendaftarkan semua View menggunakan findViewById
     */
    private void initViews(View view) {
        // Header Card
        tvNamaLengkapHeader = view.findViewById(R.id.tv_nama_lengkap_header);
        tvStatusPeran = view.findViewById(R.id.tv_status_peran);

        // Detail Fields
        tvNik = view.findViewById(R.id.tv_nik);
        tvNamaLengkapDetail = view.findViewById(R.id.tv_nama_lengkap_detail);
        tvTanggalLahir = view.findViewById(R.id.tv_tanggal_lahir); // [FIX] ID sudah benar
        tvEmail = view.findViewById(R.id.tv_email);
        tvNomorTelepon = view.findViewById(R.id.tv_nomor_telepon);
        tvAlamat = view.findViewById(R.id.tv_alamat);

        // Tombol
        btnEditProfil = view.findViewById(R.id.btn_edit_profil);
        btnUbahPassword = view.findViewById(R.id.btn_ubah_password);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void fetchUserProfile(String accessToken, String userId) {
        if (accessToken == null || userId == null) {
            Toast.makeText(getContext(), "Sesi tidak valid, silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        tvNamaLengkapHeader.setText("Memuat...");
        tvStatusPeran.setText("Memuat...");

        SupabaseAuth.getProfile(accessToken, userId, new SupabaseAuth.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                if (getActivity() == null) return;
                currentProfile = profile;
                updateUI(profile);
            }
            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(), "Gagal memuat profil: " + errorMessage, Toast.LENGTH_LONG).show();
                tvNamaLengkapHeader.setText("Gagal Memuat");
                tvStatusPeran.setText("Error");
            }
        });
    }

    private void fetchUserProfileWithRefresh() {
        String refreshToken = sessionManager.getRefreshToken();

        if (refreshToken == null) {
            Toast.makeText(getContext(), "Sesi tidak valid, silakan login ulang.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        tvNamaLengkapHeader.setText("Menyegarkan sesi...");
        tvStatusPeran.setText("...");

        // 1. Panggil refreshAccessToken
        SupabaseAuth.refreshAccessToken(refreshToken, new SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String newAccessToken, String userId, String newRefreshToken) {
                // 2. Simpan token baru ke session
                // (Menggunakan 4 parameter, sesuai logika Anda di uploadSuratSehat)
                sessionManager.createLoginSession(newAccessToken, userId, sessionManager.getUserEmail(), newRefreshToken);

                // 3. Panggil metode fetch yang asli dengan token BARU
                fetchUserProfile(newAccessToken, userId);
            }

            @Override
            public void onError(String errorMessage) {
                // Gagal refresh, kemungkinan refresh token sudah kedaluwarsa
                Toast.makeText(getContext(), "Sesi Anda telah berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show();
                // TODO: Arahkan ke Login
            }
        });
    }

    /**
     * Mengisi data ke View (tidak pakai 'binding' lagi)
     */
    private void updateUI(Profile profile) {
        String belumDiisi = "Belum diisi";

        // Update header card
        tvNamaLengkapHeader.setText(profile.getNamaLengkap());

        String peran = profile.getPeran();
        if (peran != null && !peran.isEmpty()) {
            peran = peran.substring(0, 1).toUpperCase() + peran.substring(1);
        } else {
            peran = "Pendaki";
        }
        tvStatusPeran.setText(peran);

        // Update detail fields
        tvNik.setText((profile.getNik() != null && !profile.getNik().isEmpty()) ? profile.getNik() : belumDiisi);
        tvNamaLengkapDetail.setText(profile.getNamaLengkap());
        tvTanggalLahir.setText((profile.getTanggalLahir() != null && !profile.getTanggalLahir().isEmpty()) ? profile.getTanggalLahir() : belumDiisi);
        tvEmail.setText(profile.getEmail());
        tvNomorTelepon.setText((profile.getNomorTelepon() != null && !profile.getNomorTelepon().isEmpty()) ? profile.getNomorTelepon() : belumDiisi);
        tvAlamat.setText((profile.getAlamat() != null && !profile.getAlamat().isEmpty()) ? profile.getAlamat() : belumDiisi);
    }

    /**
     * Mengatur listener (tidak pakai 'binding' lagi)
     */
    private void setupButtonListeners() {
        btnEditProfil.setOnClickListener(v -> {
            if (currentProfile != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("PROFIL_SAAT_INI", currentProfile);
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                editProfileFragment.setArguments(bundle);

                if (getActivity() instanceof com.zawww.e_simaksi.ui.activity.MainActivity) {
                    ((com.zawww.e_simaksi.ui.activity.MainActivity) getActivity()).navigateToFragment(editProfileFragment);
                }
            } else {
                Toast.makeText(getContext(), "Data profil belum dimuat", Toast.LENGTH_SHORT).show();
            }
        });

        btnUbahPassword.setOnClickListener(v -> {
            if (getActivity() instanceof com.zawww.e_simaksi.ui.activity.MainActivity) {
                ((com.zawww.e_simaksi.ui.activity.MainActivity) getActivity()).navigateToFragment(new UbahPasswordFragment());
            }
        });

        // [PERBAIKAN] Panggil dialog konfirmasi saat tombol logout diklik
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    /**
     * [BARU] Memindahkan metode dialog logout dari MainActivity ke sini.
     */
    private void showLogoutDialog() {
        if (getContext() == null) return; // Pastikan context ada

        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Logout user
                    sessionManager.logoutUser();
                    Toast.makeText(getContext(), "Berhasil logout", Toast.LENGTH_SHORT).show();

                    // Kembali ke halaman login
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Selalu muat ulang profil saat fragment ini ditampilkan
        // untuk memastikan data selalu yang terbaru setelah login ulang atau edit profil.
        Log.d("ProfileFragment", "onResume: Memuat ulang profil...");
        fetchUserProfileWithRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Tidak perlu apa-apa di sini karena kita tidak pakai binding
    }
}