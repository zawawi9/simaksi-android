package dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.view.WindowCompat;

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
    private int previousPosition = 0; // Untuk melacak posisi navigasi sebelumnya

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
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
            // Update previous position to match the default fragment (Beranda = position 0)
            previousPosition = 0;
        }

        // Setup swipe to refresh with custom animation
        setupSwipeRefresh();

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Ambil layout params (aturan tata letak) dari view
            ViewGroup.LayoutParams params = v.getLayoutParams();

            // Pastikan itu adalah MarginLayoutParams (biar kita bisa set margin)
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;

                // Set MARGIN BAWAH seukuran tinggi tombol navigasi
                marginParams.bottomMargin = systemBars.bottom;

                // Terapkan kembali layout params yang sudah diubah
                v.setLayoutParams(marginParams);
            }

            // Kembalikan insets aslinya
            return insets;
        });

        // Listener untuk navigasi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentPosition = getPositionById(itemId);

            // Tentukan arah animasi berdasarkan posisi sebelumnya dan sekarang
            int enterAnim, exitAnim, popEnterAnim, popExitAnim;

            if (currentPosition > previousPosition) {
                // Berpindah ke kanan (misal: Beranda -> Transaksi)
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
                popEnterAnim = R.anim.slide_in_left;
                popExitAnim = R.anim.slide_out_right;
            } else if (currentPosition < previousPosition) {
                // Berpindah ke kiri (misal: Transaksi -> Beranda)
                enterAnim = R.anim.slide_in_left;
                exitAnim = R.anim.slide_out_right;
                popEnterAnim = R.anim.slide_in_right;
                popExitAnim = R.anim.slide_out_left;
            } else {
                // Jika sama atau tidak ada perubahan posisi, gunakan default
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
                popEnterAnim = R.anim.slide_in_left;
                popExitAnim = R.anim.slide_out_right;
            }

            // Update posisi sebelumnya
            previousPosition = currentPosition;

            Fragment selectedFragment = null;
            if (itemId == R.id.nav_beranda) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_transaksi) {
                selectedFragment = new TransaksiFragment();
            } else if (itemId == R.id.nav_hubungi) {
                selectedFragment = new HubungiKamiFragment();
            } else if (itemId == R.id.nav_profil) {
                // Tambahkan fungsi logout di sini atau navigasi ke profil
                showLogoutDialog();
                return true; // Berhasil, item dipilih
            }

            if (selectedFragment != null) {
                loadFragmentWithDirection(selectedFragment, enterAnim, exitAnim, popEnterAnim, popExitAnim);
            }

            return true; // Berhasil, item dipilih
        });

        // PERBAIKAN PALING PENTING:
        // Muat HomeFragment secara default saat activity pertama kali dibuka.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }

        // Setup back pressed callback for double tap to exit
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish(); // Keluar dari aplikasi
                } else {
                    Toast.makeText(getBaseContext(), "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
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

    // Metode untuk memuat fragment dengan animasi arah yang sesuai
    private void loadFragmentWithDirection(Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Set animasi berdasarkan arah navigasi
        fragmentTransaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
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

    // Metode untuk mendapatkan posisi navigasi berdasarkan ID
    private int getPositionById(int itemId) {
        if (itemId == R.id.nav_beranda) {
            return 0; // Beranda adalah posisi pertama (paling kiri)
        } else if (itemId == R.id.nav_transaksi) {
            return 1; // Transaksi adalah posisi kedua
        } else if (itemId == R.id.nav_hubungi) {
            return 2; // Hubungi adalah posisi ketiga
        } else if (itemId == R.id.nav_profil) {
            return 3; // Profil adalah posisi keempat (paling kanan)
        } else {
            return 0;
        }
    }
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
}