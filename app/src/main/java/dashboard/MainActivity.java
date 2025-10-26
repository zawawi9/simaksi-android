package dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zawww.e_simaksi.R;

import FITURLOGIN.SessionManager;
import FITURLOGIN.TampilanPertamaActivity;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private long backPressedTime;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LottieAnimationView lottieRefreshAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Set default fragment saat aplikasi dibuka
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Setup swipe to refresh with custom animation
        setupSwipeRefresh();

        // Listener untuk navigasi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_beranda) {
                loadFragment(new HomeFragment());
                return true; // Berhasil, item dipilih
            } else if (itemId == R.id.nav_transaksi) {
                loadFragment(new TransaksiFragment());
                return true; // Berhasil, item dipilih
            } else if (itemId == R.id.nav_hubungi) {
                Toast.makeText(this, "Fitur Hubungi Kami akan segera hadir!", Toast.LENGTH_SHORT).show();
                return false; // Gagal, item tidak akan dipilih (highlight)
            } else if (itemId == R.id.nav_profil) {
                // Tambahkan fungsi logout di sini atau navigasi ke profil
                showLogoutDialog();
                return true; // Berhasil, item dipilih
            }

            return false;
        });

        // PERBAIKAN PALING PENTING:
        // Muat HomeFragment secara default saat activity pertama kali dibuka.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }
    }
    
    private void setupSwipeRefresh() {
        // Setup custom refresh animation
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Show custom refresh animation
            showCustomRefreshAnimation();
            
            // Simulate data refresh (replace with actual data refresh logic)
            new Handler().postDelayed(() -> {
                // Stop the refresh animation
                swipeRefreshLayout.setRefreshing(false);
                
                // Optional: Show success message
                Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
            }, 2000); // Simulate 2 seconds refresh time
        });
        
        // Customize the default SwipeRefreshLayout colors
        swipeRefreshLayout.setColorSchemeColors(
            getResources().getColor(R.color.hijau_tua_brand)
        );
    }
    
    private void showCustomRefreshAnimation() {
        // The Lottie animation will automatically play when setRefreshing(true) is called
        // The default animation will be replaced with our custom one
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Improved animations for smoother transitions
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    
    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Logout user
                    sessionManager.logoutUser();
                    Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();
                    
                    // Kembali ke halaman login
                    Intent intent = new Intent(MainActivity.this, TampilanPertamaActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    // Handle back button press to exit app or return to home
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}