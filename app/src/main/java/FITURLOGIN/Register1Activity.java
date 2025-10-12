package FITURLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.util.Patterns;
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

public class Register1Activity extends AppCompatActivity {

    ImageView backArrow;
    TextInputLayout textFieldEmail, textFieldUsername, textFieldPassword, textFieldName;
    TextInputEditText editTextEmail, editTextUsername, editTextPassword, editTextName;
    MaterialButton buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register1);

        textFieldEmail = findViewById(R.id.textFieldEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        textFieldUsername = findViewById(R.id.textFieldUsername);
        editTextUsername = findViewById(R.id.editTextUsername);
        textFieldName = findViewById(R.id.textFieldFullname);
        editTextName = findViewById(R.id.editTextFullname);
        textFieldPassword = findViewById(R.id.textFieldPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        backArrow = findViewById(R.id.backArrowImageView);
        buttonNext = findViewById(R.id.buttonNext);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Menutup activity saat ini dan kembali ke halaman sebelumnya
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void validateAndProceed() {
        // 1. Ambil semua input dari user
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Reset error sebelumnya
        textFieldEmail.setError(null);
        textFieldUsername.setError(null);
        textFieldPassword.setError(null);

        // 2. Mulai Validasi
        if (email.isEmpty()) {
            textFieldEmail.setError("Email tidak boleh kosong");
            return; // Hentikan proses
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textFieldEmail.setError("Format email tidak valid");
            return;
        }

        if (username.isEmpty()) {
            textFieldUsername.setError("Username tidak boleh kosong");
            return;
        }

        if (name.isEmpty()) {
            textFieldName.setError("Nama tidak boleh kosong");
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

        // 3. Jika semua validasi lolos
        // Buat objek User baru dari data yang diinput
        User newUser = new User(email, username, password, name);

        // Di aplikasi nyata, objek "newUser" ini akan disimpan ke database.
        // Untuk sekarang, kita tampilkan pesan Toast sebagai simulasi.
        Toast.makeText(this, "Data valid! User: " + newUser.getUsername(), Toast.LENGTH_SHORT).show();

        // Lanjutkan ke langkah berikutnya
        Intent intent = new Intent(Register1Activity.this, Register2Activity.class);
        startActivity(intent);
    }
}