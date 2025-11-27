package com.zawww.e_simaksi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.util.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    // Komponen UI
    private TextInputEditText etName, etEmail, etPassword, etConfirmPass, etToken;
    private TextInputLayout layoutName, layoutEmail, layoutPass, layoutConfirmPass, layoutToken;
    private MaterialButton btnAction;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    // Status: false = Mode Daftar, true = Mode Verifikasi Token
    private boolean isTokenMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inisialisasi View (Sambungkan dengan ID di XML)
        initViews();

        // 2. Atur Listener Tombol Utama
        btnAction.setOnClickListener(v -> {
            if (!isTokenMode) {
                // Jika masih mode daftar -> Kirim Data ke Supabase
                processRegistration();
            } else {
                // Jika sudah mode token -> Verifikasi Token
                processVerification();
            }
        });

        // 3. Link ke Login
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        // EditText (Inputan)
        etName = findViewById(R.id.editTextFullName);
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        etConfirmPass = findViewById(R.id.editTextConfirmPassword);
        etToken = findViewById(R.id.editTextToken);

        // Layout (Pembungkus Inputan - buat nampilin error)
        layoutName = findViewById(R.id.textFieldFullName);
        layoutEmail = findViewById(R.id.textFieldEmail);
        layoutPass = findViewById(R.id.textFieldPassword);
        layoutConfirmPass = findViewById(R.id.textFieldConfirmPassword);
        layoutToken = findViewById(R.id.textFieldToken);

        // Tombol & Loading
        btnAction = findViewById(R.id.buttonRegister);
        tvLoginLink = findViewById(R.id.tv_login_sekarang);
        progressBar = findViewById(R.id.progressBar);

        // Set Tampilan Awal: Sembunyikan Kolom Token
        layoutToken.setVisibility(View.GONE);
        btnAction.setText("DAFTAR SEKARANG");
    }

    // --- TAHAP 1: PROSES DAFTAR ---
    private void processRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // Validasi Input
        if (TextUtils.isEmpty(name)) {
            layoutName.setError("Nama wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            layoutPass.setError("Password minimal 6 karakter");
            return;
        }
        if (!password.equals(confirmPass)) {
            layoutConfirmPass.setError("Password tidak sama");
            return;
        }

        // Bersihkan Error sebelumnya
        clearErrors();
        setLoading(true);

        // Panggil Supabase
        SupabaseAuth.registerUser(email, password, name, new SupabaseAuth.RegisterCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Kode verifikasi terkirim ke email!", Toast.LENGTH_LONG).show();

                // BERHASIL -> Ubah tampilan ke Mode Input Token
                switchToTokenMode();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Gagal: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- TAHAP 2: PROSES VERIFIKASI TOKEN ---
    private void processVerification() {
        String email = etEmail.getText().toString().trim();
        String token = etToken.getText().toString().trim();

        if (TextUtils.isEmpty(token)) {
            layoutToken.setError("Masukkan kode token dari email");
            return;
        }

        setLoading(true);

        // Panggil Verifikasi OTP
        SupabaseAuth.verifyEmailOtp(email, token, new SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String accessToken, String userId, String refreshToken) {
                setLoading(false);

                // Simpan Data Login ke Session
                SessionManager session = new SessionManager(RegisterActivity.this);
                session.createLoginSession(accessToken, userId, email, refreshToken);

                Toast.makeText(RegisterActivity.this, "Pendaftaran Berhasil!", Toast.LENGTH_LONG).show();

                // Pindah ke Home (Main Activity)
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Hapus history
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                layoutToken.setError(errorMessage); // Tampilkan error di kolom token
                Toast.makeText(RegisterActivity.this, "Gagal Verifikasi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper: Mengubah UI menjadi Mode Input Token
    private void switchToTokenMode() {
        isTokenMode = true; // Ubah status

        // UI Changes:
        layoutToken.setVisibility(View.VISIBLE); // Munculkan kolom Token

        // Sembunyikan yang gak perlu biar rapi
        layoutName.setVisibility(View.GONE);
        layoutConfirmPass.setVisibility(View.GONE);

        // Matikan email & password biar gak diubah user
        layoutEmail.setEnabled(false);
        layoutPass.setEnabled(false);

        // Ubah teks tombol
        btnAction.setText("VERIFIKASI AKUN");

        // Fokus kursor ke kolom token
        etToken.requestFocus();
    }

    private void clearErrors() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPass.setError(null);
        layoutConfirmPass.setError(null);
        layoutToken.setError(null);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnAction.setEnabled(false);
            btnAction.setText("Loading...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnAction.setEnabled(true);
            // Kembalikan teks tombol sesuai mode
            btnAction.setText(isTokenMode ? "VERIFIKASI AKUN" : "DAFTAR SEKARANG");
        }
    }
}