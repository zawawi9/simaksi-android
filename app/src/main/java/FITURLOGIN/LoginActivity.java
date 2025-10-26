package FITURLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;

import dashboard.MainActivity;

public class LoginActivity extends AppCompatActivity {

    ImageView backArrow;
    TextInputLayout textFieldUsername, textFieldPassword;
    TextInputEditText editTextUsername, editTextPassword;
    MaterialButton buttonLogin;
    ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        backArrow = findViewById(R.id.backArrowImageView);
        textFieldUsername = findViewById(R.id.textFieldUsername);
        editTextUsername = findViewById(R.id.editTextUsername);
        textFieldPassword = findViewById(R.id.textFieldPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Reset error
            textFieldUsername.setError(null);
            textFieldPassword.setError(null);

            // Validasi input
            if (email.isEmpty()) {
                textFieldUsername.setError("Email tidak boleh kosong");
                return;
            }
            if (password.isEmpty()) {
                textFieldPassword.setError("Password tidak boleh kosong");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);

            SupabaseAuth.loginUser(email, password, new SupabaseAuth.AuthCallback() {
                @Override
                public void onSuccess(String accessToken, String userId) {
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                    // Simpan informasi login menggunakan SessionManager
                    sessionManager.createLoginSession(accessToken, userId, email);

                    // Pindah ke dashboard
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    showErrorDialog(errorMessage);
                }
            });
        });

        backArrow.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Gagal Login")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
