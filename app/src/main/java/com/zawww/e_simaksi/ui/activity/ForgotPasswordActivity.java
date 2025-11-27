package com.zawww.e_simaksi.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Komponen UI
    private TextInputEditText etEmail, etOtp, etNewPass, etConfirmPass;
    private TextInputLayout layoutEmail, layoutOtp, layoutNewPass, layoutConfirmPass;
    private MaterialButton btnReset;
    private ProgressBar progressBar;

    // Status: false = Mode Minta Kode, true = Mode Ganti Password
    private boolean isCodeSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupInitialState();

        btnReset.setOnClickListener(v -> {
            if (!isCodeSent) {
                // TAHAP 1: Minta Kode
                requestResetCode();
            } else {
                // TAHAP 2: Verifikasi & Ganti Password
                finalizeReset();
            }
        });
    }

    private void initViews() {
        // EditTexts
        etEmail = findViewById(R.id.editTextEmail);
        etOtp = findViewById(R.id.editTextOtp);
        etNewPass = findViewById(R.id.editTextNewPassword);
        etConfirmPass = findViewById(R.id.editTextConfirmNewPassword);

        // Layouts (Wrapper)
        layoutEmail = findViewById(R.id.textFieldEmail);
        layoutOtp = findViewById(R.id.textFieldOtp);
        layoutNewPass = findViewById(R.id.textFieldNewPassword);
        layoutConfirmPass = findViewById(R.id.textFieldConfirmNewPassword);

        btnReset = findViewById(R.id.buttonResetPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    // Mengatur tampilan awal (Sembunyikan form OTP & Password)
    private void setupInitialState() {
        layoutOtp.setVisibility(View.GONE);
        layoutNewPass.setVisibility(View.GONE);
        layoutConfirmPass.setVisibility(View.GONE);

        btnReset.setText("KIRIM KODE OTP");
    }

    // --- LOGIC TAHAP 1: MINTA KODE ---
    private void requestResetCode() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Masukkan email terdaftar");
            return;
        }

        setLoading(true);

        SupabaseAuth.sendPasswordReset(email, new SupabaseAuth.UpdateCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Kode OTP terkirim ke email!", Toast.LENGTH_LONG).show();

                // Pindah ke Mode Input Token
                switchToResetMode();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- LOGIC TAHAP 2: VERIFIKASI & UPDATE ---
    private void finalizeReset() {
        String email = etEmail.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // Validasi
        if (TextUtils.isEmpty(otp)) {
            layoutOtp.setError("Masukkan kode OTP");
            return;
        }
        if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
            layoutNewPass.setError("Password minimal 6 karakter");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            layoutConfirmPass.setError("Password tidak sama");
            return;
        }

        setLoading(true);

        // 1. Verifikasi OTP dulu
        SupabaseAuth.verifyRecoveryOtp(email, otp, new SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String accessToken, String userId, String refreshToken) {

                // 2. Jika OTP Benar -> Update Password
                SupabaseAuth.updateUserPassword(accessToken, newPass, new SupabaseAuth.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        Toast.makeText(ForgotPasswordActivity.this, "Password Berhasil Diubah! Silakan Login.", Toast.LENGTH_LONG).show();
                        finish(); // Tutup activity, balik ke login
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        Toast.makeText(ForgotPasswordActivity.this, "Gagal Update Password: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                layoutOtp.setError("Kode OTP Salah / Kadaluarsa");
                Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper: Ubah Tampilan jadi Form Lengkap
    private void switchToResetMode() {
        isCodeSent = true;

        // Munculkan field lainnya
        layoutOtp.setVisibility(View.VISIBLE);
        layoutNewPass.setVisibility(View.VISIBLE);
        layoutConfirmPass.setVisibility(View.VISIBLE);

        // Matikan edit email
        layoutEmail.setEnabled(false);

        // Ubah tombol
        btnReset.setText("SIMPAN PASSWORD BARU");

        // Fokus ke OTP
        etOtp.requestFocus();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnReset.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnReset.setEnabled(true);
        }
    }
}