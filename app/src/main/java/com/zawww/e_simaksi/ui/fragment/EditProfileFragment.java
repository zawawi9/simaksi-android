package com.zawww.e_simaksi.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.Profile;
import com.zawww.e_simaksi.util.SessionManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etNik, etNamaLengkap, etTanggalLahir, etAlamat, etNomorTelepon;
    private Button btnSimpan;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private Profile currentProfile;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        initViews(view);

        if (getArguments() != null) {
            currentProfile = (Profile) getArguments().getSerializable("PROFIL_SAAT_INI");
            if (currentProfile != null) {
                populateProfileData();
            }
        }

        setupListeners();
    }

    private void initViews(View view) {
        etNik = view.findViewById(R.id.et_nik);
        etNamaLengkap = view.findViewById(R.id.et_nama_lengkap);
        etTanggalLahir = view.findViewById(R.id.et_tanggal_lahir);
        etAlamat = view.findViewById(R.id.et_alamat);
        etNomorTelepon = view.findViewById(R.id.et_nomor_telepon);
        btnSimpan = view.findViewById(R.id.btn_simpan);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void populateProfileData() {
        etNik.setText(currentProfile.getNik());
        etNamaLengkap.setText(currentProfile.getNamaLengkap());
        etTanggalLahir.setText(currentProfile.getTanggalLahir());
        etAlamat.setText(currentProfile.getAlamat());
        etNomorTelepon.setText(currentProfile.getNomorTelepon());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        etTanggalLahir.setOnClickListener(v -> showDatePickerDialog());

        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                updateProfile();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            etTanggalLahir.setText(selectedDate);
        }, year, month, day).show();
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etNik.getText())) {
            etNik.setError("NIK tidak boleh kosong");
            return false;
        }
        if (TextUtils.isEmpty(etNamaLengkap.getText())) {
            etNamaLengkap.setError("Nama lengkap tidak boleh kosong");
            return false;
        }
        // Add more validation if needed
        return true;
    }

    private void updateProfile() {
        showLoading(true);

        String nik = etNik.getText().toString().trim();
        String namaLengkap = etNamaLengkap.getText().toString().trim();
        String tanggalLahir = etTanggalLahir.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();
        String nomorTelepon = etNomorTelepon.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nik", nik);
        updates.put("nama_lengkap", namaLengkap);
        updates.put("tanggal_lahir", tanggalLahir);
        updates.put("alamat", alamat);
        updates.put("nomor_telepon", nomorTelepon);

        String accessToken = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();

        // This method will be created in a later step
        SupabaseAuth.updateProfile(accessToken, userId, updates, new SupabaseAuth.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Gagal memperbarui profil: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSimpan.setEnabled(!isLoading);
    }
}
