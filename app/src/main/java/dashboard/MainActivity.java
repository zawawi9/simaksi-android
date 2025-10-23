package dashboard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dashboard.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zawww.e_simaksi.R;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment saat aplikasi dibuka
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Listener untuk navigasi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_beranda) {
                loadFragment(new HomeFragment());
                return true; // Berhasil, item dipilih
            } else if (itemId == R.id.nav_transaksi) {
                Toast.makeText(this, "Fitur Transaksi akan segera hadir!", Toast.LENGTH_SHORT).show();
                loadFragment(new TransaksiFragment());
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