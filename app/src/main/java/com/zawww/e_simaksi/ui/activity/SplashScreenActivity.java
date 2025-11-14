package com.zawww.e_simaksi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.ui.activity.MainActivity;
import com.zawww.e_simaksi.util.SessionManager;
import com.zawww.e_simaksi.ui.activity.TampilanPertamaActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Set delay untuk splash screen (misalnya 3 detik)
        new Handler().postDelayed(() -> {
            // Check if user is already logged in
            if (sessionManager.isLoggedIn()) {
                // Jika sudah login, langsung ke MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // Jika belum login, ke TampilanPertamaActivity
                Intent intent = new Intent(SplashScreenActivity.this, TampilanPertamaActivity.class);
                startActivity(intent);
            }
            finish(); // Tutup splash screen agar tidak bisa kembali ke sini
        }, 3000); // 3 detik delay
    }
}