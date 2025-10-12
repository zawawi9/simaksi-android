package dashboard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.dashboard.HomeFragment; // PERBAIKAN: Import HomeFragment ditambahkan

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kode boilerplate EdgeToEdge Anda (sudah benar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Atur listener untuk merespon klik
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_beranda) {
                loadFragment(new HomeFragment());
                return true; // Berhasil, item dipilih
            } else if (itemId == R.id.nav_transaksi) {
                Toast.makeText(this, "Fitur Transaksi akan segera hadir!", Toast.LENGTH_SHORT).show();
                return false; // Gagal, item tidak akan dipilih (highlight)
            } else if (itemId == R.id.nav_profil) {
                Toast.makeText(this, "Fitur Profil akan segera hadir!", Toast.LENGTH_SHORT).show();
                return false; // Gagal, item tidak akan dipilih (highlight)
            }

            return false;
        });

        // PERBAIKAN PALING PENTING:
        // Muat HomeFragment secara default saat activity pertama kali dibuat.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }
    }
    // Fungsi ini sudah bagus, tidak perlu diubah
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Animasi transisi (opsional, tapi membuat UI lebih smooth)
        fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in, // animasi masuk
                android.R.anim.fade_out, // animasi keluar
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}