package FITURLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

public class Register2Activity extends AppCompatActivity {

    ImageView backArrow;
    TextInputLayout textFieldCode;
    TextInputEditText editTextCode;
    MaterialButton buttonSignUp;

    final String KODE_BENAR = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register2);

        backArrow = findViewById(R.id.backArrowImageView);
        textFieldCode = findViewById(R.id.textFieldCode);
        editTextCode = findViewById(R.id.editTextCode);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        backArrow.setOnClickListener(v -> finish());

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Ambil teks dari input field
                String kodeYangDimasukkan = editTextCode.getText().toString().trim();

                // 2. Cek apakah input kosong
                if (kodeYangDimasukkan.isEmpty()) {
                    textFieldCode.setError("Kode tidak boleh kosong!");
                    return; // Hentikan proses
                }

                // 3. Bandingkan dengan kode yang benar
                if (kodeYangDimasukkan.equals(KODE_BENAR)) {
                    // JIKA KODE BENAR
                    Toast.makeText(Register2Activity.this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();

                    // Pindah ke LoginActivity
                    Intent intent = new Intent(Register2Activity.this, LoginActivity.class);

                    // Baris ini penting: menghapus semua activity sebelumnya (register 1 & 2)
                    // dari history, sehingga pengguna tidak bisa kembali ke halaman register
                    // setelah berhasil.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                } else {
                    // JIKA KODE SALAH
                    textFieldCode.setError("Kode yang Anda masukkan salah!");
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}