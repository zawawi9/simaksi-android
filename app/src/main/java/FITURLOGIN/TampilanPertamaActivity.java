package FITURLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.zawww.e_simaksi.R;
import dashboard.MainActivity;

public class TampilanPertamaActivity extends AppCompatActivity {

    MaterialButton buttonLogin, buttonRegister;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tampilan_pertama);

        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // Jika sudah login, langsung ke MainActivity
            Intent intent = new Intent(TampilanPertamaActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return; // Keluar dari onCreate agar tidak melanjutkan inisialisasi
        }

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TampilanPertamaActivity.this, LoginActivity.class);
                // Mulai perpindahan halaman
                startActivity(intent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buat Intent untuk pergi ke RegisterActivity
                Intent intent = new Intent(TampilanPertamaActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}