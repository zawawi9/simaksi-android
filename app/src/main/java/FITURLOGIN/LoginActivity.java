package FITURLOGIN;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zawww.e_simaksi.R;

import dashboard.MainActivity;

public class LoginActivity extends AppCompatActivity {

    ImageView backArrow;
    TextInputLayout textFieldUsername, textFieldPassword;
    TextInputEditText editTextUsername, editTextPassword;
    MaterialButton buttonLogin;
    final String USERNAME_TERDAFTAR = "zawawi";
    final String PASSWORD_TERDAFTAR = "123456";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        backArrow = findViewById(R.id.backArrowImageView);
        textFieldUsername = findViewById(R.id.textFieldUsername);
        editTextUsername = findViewById(R.id.editTextUsername);
        textFieldPassword = findViewById(R.id.textFieldPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ambil input
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                // Reset error
                textFieldUsername.setError(null);
                textFieldPassword.setError(null);

                // Validasi input kosong
                if (username.isEmpty()) {
                    textFieldUsername.setError("Username tidak boleh kosong");
                    return;
                }
                if (password.isEmpty()) {
                    textFieldPassword.setError("Password tidak boleh kosong");
                    return;
                }

                // Cek kredensial
                if (username.equals(USERNAME_TERDAFTAR) && password.equals(PASSWORD_TERDAFTAR)) {
                    // JIKA LOGIN BERHASIL
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                    // Di sini Anda akan pindah ke halaman utama aplikasi
                    // Contoh: Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    // startActivity(intent);
                     // Tutup halaman login agar tidak bisa kembali
                    // Simpan status login
                    SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

// Pindah ke halaman utama
// ...
                } else {
                    // JIKA GAGAL
                    Toast.makeText(LoginActivity.this, "Username atau Password salah!", Toast.LENGTH_LONG).show();
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}