package com.zawww.e_simaksi.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.util.SessionManager;

import com.zawww.e_simaksi.util.ErrorHandler;

public class UbahPasswordFragment extends Fragment {

    private TextInputEditText etEmail, etOtp, etNewPassword, etConfirmNewPassword;
    private Button btnKirimOtp, btnUbahPassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ubah_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        initViews(view);
        setupListeners();

        etEmail.setText(sessionManager.getUserEmail());
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etOtp = view.findViewById(R.id.et_otp);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmNewPassword = view.findViewById(R.id.et_confirm_new_password);
        btnKirimOtp = view.findViewById(R.id.btn_kirim_otp);
        btnUbahPassword = view.findViewById(R.id.btn_ubah_password);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnKirimOtp.setOnClickListener(v -> sendOtp());
        btnUbahPassword.setOnClickListener(v -> changePassword());
    }

    private void sendOtp() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            ErrorHandler.showError(requireView(), "Email tidak boleh kosong");
            return;
        }

        showLoading(true);

        SupabaseAuth.sendPasswordResetOtp(email, new SupabaseAuth.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "OTP telah dikirim ke email Anda", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    ErrorHandler.showError(requireView(), "Gagal mengirim OTP: " + errorMessage);
                });
            }
        });
    }

    private void changePassword() {
        String email = etEmail.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        if (otp.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            ErrorHandler.showError(requireView(), "Semua field harus diisi");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            ErrorHandler.showError(requireView(), "Password baru tidak cocok");
            return;
        }

        showLoading(true);

        SupabaseAuth.resetPasswordWithOtp(email, otp, newPassword, new SupabaseAuth.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    ErrorHandler.showError(requireView(), "Gagal mengubah password: " + errorMessage);
                });
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Password Berhasil Diubah")
                .setMessage("Password Anda telah berhasil diubah. Silakan login kembali.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // Optionally, navigate back to profile or login screen
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnKirimOtp.setEnabled(!isLoading);
        btnUbahPassword.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
        etNewPassword.setEnabled(!isLoading);
        etConfirmNewPassword.setEnabled(!isLoading);
    }
}
