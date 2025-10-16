package FITURLOGIN;

import android.os.Bundle;
import android.util.Patterns;
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
import com.zawww.e_simaksi.api.SupabaseAuth;

public class RegisterActivity extends AppCompatActivity {

    ImageView backArrow;
    TextInputLayout textFieldEmail, textFieldUsername, textFieldPassword;
    TextInputEditText editTextEmail, editTextUsername, editTextPassword;
    MaterialButton buttonNext;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inisialisasi komponen UI
        textFieldEmail = findViewById(R.id.textFieldEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        textFieldUsername = findViewById(R.id.textFieldUsername);
        editTextUsername = findViewById(R.id.editTextUsername);
        textFieldPassword = findViewById(R.id.textFieldPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        backArrow = findViewById(R.id.backArrowImageView);
        buttonNext = findViewById(R.id.buttonNext);
        progressBar = findViewById(R.id.progressBar);

        backArrow.setOnClickListener(v -> finish());

        buttonNext.setOnClickListener(v -> validateAndProceed());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validateAndProceed() {
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim(); // → akan dikirim ke kolom nama_lengkap
        String password = editTextPassword.getText().toString().trim();

        // Reset error
        textFieldEmail.setError(null);
        textFieldUsername.setError(null);
        textFieldPassword.setError(null);

        // Validasi input
        if (email.isEmpty()) {
            textFieldEmail.setError("Email tidak boleh kosong");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textFieldEmail.setError("Format email tidak valid");
            return;
        }

        if (username.isEmpty()) {
            textFieldUsername.setError("Nama lengkap tidak boleh kosong");
            return;
        }

        if (password.isEmpty()) {
            textFieldPassword.setError("Password tidak boleh kosong");
            return;
        }

        if (password.length() < 6) {
            textFieldPassword.setError("Password minimal harus 6 karakter");
            return;
        }

        // Jika valid → tampilkan loading
        progressBar.setVisibility(View.VISIBLE);
        buttonNext.setEnabled(false);

        // Panggil fungsi register Supabase
        SupabaseAuth.registerUser(email, password, username, new SupabaseAuth.RegisterCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                buttonNext.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registrasi berhasil! Cek email untuk verifikasi.", Toast.LENGTH_LONG).show();
                finish(); // Tutup activity, balik ke login
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                buttonNext.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Gagal: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
