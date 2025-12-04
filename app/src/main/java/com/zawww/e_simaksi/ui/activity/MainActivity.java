package com.zawww.e_simaksi.ui.activity;

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

import com.zawww.e_simaksi.util.SessionManager;
import com.zawww.e_simaksi.ui.fragment.HomeFragment;
import com.zawww.e_simaksi.ui.fragment.HubungiKamiFragment;
import com.zawww.e_simaksi.ui.fragment.ReservasiFragment;
import com.zawww.e_simaksi.ui.fragment.TransaksiFragment;

// [BARU] Impor ProfileFragment yang sudah kita buat
import com.zawww.e_simaksi.ui.fragment.ProfileFragment;

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
            ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                marginParams.bottomMargin = systemBars.bottom;
                v.setLayoutParams(marginParams);
            }
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(swipeRefreshLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        // Listener untuk navigasi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentPosition = getPositionById(itemId);
            int enterAnim, exitAnim, popEnterAnim, popExitAnim;

            if (currentPosition > previousPosition) {
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
                popEnterAnim = R.anim.slide_in_left;
                popExitAnim = R.anim.slide_out_right;
            } else if (currentPosition < previousPosition) {
                enterAnim = R.anim.slide_in_left;
                exitAnim = R.anim.slide_out_right;
                popEnterAnim = R.anim.slide_in_right;
                popExitAnim = R.anim.slide_out_left;
            } else {
                return false;
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
                // [PERBAIKAN] Arahkan ke ProfileFragment
                selectedFragment = new ProfileFragment();
                // showLogoutDialog(); // Dihapus dari sini
                // return true; // Dihapus dari sini
            }

            if (selectedFragment != null) {
                loadFragmentWithDirection(selectedFragment, enterAnim, exitAnim, popEnterAnim, popExitAnim);
            }

            return true; // Berhasil, item dipilih
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            // [DIUBAH] Tambahkan ProfileFragment ke daftar
            if (currentFragment instanceof HomeFragment ||
                    currentFragment instanceof TransaksiFragment ||
                    currentFragment instanceof HubungiKamiFragment ||
                    currentFragment instanceof ProfileFragment) { // <-- Tambahkan ini
                showBottomNav(true);
            } else {
                showBottomNav(false);
            }
        });

        // PERBAIKAN PALING PENTING:
        // Muat HomeFragment secara default saat activity pertama kali dibuka.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Dapatkan fragment container utama
                FragmentManager supportFm = getSupportFragmentManager();
                Fragment currentFragment = supportFm.findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof ReservasiFragment &&
                        currentFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    currentFragment.getChildFragmentManager().popBackStack();
                } else if (supportFm.getBackStackEntryCount() > 0) {
                    supportFm.popBackStack();
                } else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish(); // Keluar dari aplikasi
                    } else {
                        Toast.makeText(getBaseContext(), "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
                    }
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        // ... (Kode Anda yang ada tetap sama)
        swipeRefreshLayout.setOnRefreshListener(() -> {
            showCustomRefreshAnimation();
            new Handler().postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.hijau_tua_brand)
        );
    }

    private void showCustomRefreshAnimation() {
        // ... (Kode Anda yang ada tetap sama)
    }

    private void loadFragmentWithDirection(Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim) {
        // ... (Kode Anda yang ada tetap sama)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // [DIHAPUS] Metode showLogoutDialog() dipindahkan ke ProfileFragment.java
    // private void showLogoutDialog() { ... }

    public void showBottomNav(boolean show) {
        // ... (Kode Anda yang ada tetap sama)
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) swipeRefreshLayout.getLayoutParams();
            if (show) {
                params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.bottom_nav_height);
            } else {
                params.bottomMargin = 0;
            }
            swipeRefreshLayout.setLayoutParams(params);
        }
    }

    private int getPositionById(int itemId) {
        // ... (Kode Anda yang ada tetap sama)
        if (itemId == R.id.nav_beranda) {
            return 0;
        } else if (itemId == R.id.nav_transaksi) {
            return 1;
        } else if (itemId == R.id.nav_hubungi) {
            return 2;
        } else if (itemId == R.id.nav_profil) {
            return 3;
        } else {
            return 0;
        }
    }
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        // ... (Kode Anda yang ada tetap sama)
        return swipeRefreshLayout;
    }

    public void navigateToFragment(Fragment fragment) {
        // ... (Kode Anda yang ada tetap sama)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}