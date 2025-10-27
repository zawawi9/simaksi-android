package dashboard;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // <-- IMPORT BARU
import android.widget.LinearLayout; // <-- IMPORT BARU
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat; // <-- IMPORT BARU
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

// --- HAPUS IMPORT TABLAYOUT ---
// import com.google.android.material.tabs.TabLayout;
// import com.google.android.material.tabs.TabLayoutMediator;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.Promosi;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // --- Konstanta ---
    private static final String PROMO_DEBUG_TAG = "PROMO_DEBUG";
    private static final long SLIDER_DELAY_MS = 5000; // 5 detik

    // --- Komponen UI ---
    private ViewPager2 viewPagerPromosi;
    private LinearLayout layoutDotsIndicator; // <-- DIGANTI DARI TABLAYOUT
    private PromosiSliderAdapter promosiSliderAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // --- Komponen UI Jadwal ---
    private CardView cardJadwalKosong;
    private CardView cardJadwalAda;
    private TextView tvTanggalPendakian;
    private TextView tvKodeBooking;
    private TextView tvJumlahPendaki;
    private Button btnLihatDetail;

    // --- Untuk Auto-Scroll ---
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Promosi> promosiList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Inisialisasi Slider Promosi ---
        viewPagerPromosi = view.findViewById(R.id.view_pager_promosi);
        layoutDotsIndicator = view.findViewById(R.id.layout_dots_indicator); // <-- ID BARU
        promosiSliderAdapter = new PromosiSliderAdapter(new ArrayList<>(), getContext());
        viewPagerPromosi.setAdapter(promosiSliderAdapter);

        // --- Inisialisasi Komponen Jadwal ---
        cardJadwalKosong = view.findViewById(R.id.card_jadwal_kosong);
        cardJadwalAda = view.findViewById(R.id.card_jadwal_ada);
        tvTanggalPendakian = view.findViewById(R.id.tv_tanggal_pendakian);
        tvKodeBooking = view.findViewById(R.id.tv_kode_booking);
        tvJumlahPendaki = view.findViewById(R.id.tv_jumlah_pendaki);
        btnLihatDetail = view.findViewById(R.id.btn_lihat_detail);

        // --- Inisialisasi SwipeRefreshLayout (dari MainActivity) ---
        if (getActivity() instanceof MainActivity) {
            swipeRefreshLayout = ((MainActivity) getActivity()).getSwipeRefreshLayout();
        }

        tampilkanJadwalKosong();
        fetchPromosiData();
    }


    private void fetchPromosiData() {
        Log.d(PROMO_DEBUG_TAG, "fetchPromosiData() dipanggil.");
        SupabaseAuth.getPromosiPoster(new SupabaseAuth.PromosiCallback() {
            @Override
            public void onSuccess(List<Promosi> data) {
                Log.d(PROMO_DEBUG_TAG, "onSuccess: Menerima " + data.size() + " data promosi.");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        promosiList.clear();
                        promosiList.addAll(data);
                        setupSlider();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(PROMO_DEBUG_TAG, "onError: Gagal mengambil data - " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Gagal memuat promosi", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void setupSlider() {
        if (getContext() == null || promosiList.isEmpty()) {
            Log.w(PROMO_DEBUG_TAG, "List promosi kosong, slider disembunyikan.");
            viewPagerPromosi.setVisibility(View.GONE);
            layoutDotsIndicator.setVisibility(View.GONE);
            return;
        }

        Log.d(PROMO_DEBUG_TAG, "setupSlider() dipanggil. Ukuran list: " + promosiList.size());

        viewPagerPromosi.setVisibility(View.VISIBLE);
        layoutDotsIndicator.setVisibility(View.VISIBLE);

        // 1. Update data di Adapter
        promosiSliderAdapter.updateData(promosiList);

        // 2. Buat titik-titik indikator (LOGIKA BARU)
        setupDotsIndicator();

        // 3. Atasi Konflik Geser dengan SwipeRefreshLayout
        viewPagerPromosi.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (swipeRefreshLayout != null) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        swipeRefreshLayout.setEnabled(false);
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        swipeRefreshLayout.setEnabled(true);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update titik aktif (LOGIKA BARU)
                updateDotsIndicator(position);

                // Reset timer auto-scroll
                if (promosiList.size() > 1) {
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
                }
            }
        });

        // 4. Mulai Auto-Scroll jika gambar lebih dari 1
        if (promosiList.size() > 1) {
            Log.d(PROMO_DEBUG_TAG, "Memulai auto-slider...");
            startAutoSlider();
        }

        // 5. Set titik pertama sebagai aktif
        updateDotsIndicator(0);
    }

    private void setupDotsIndicator() {
        if (getContext() == null) return;

        layoutDotsIndicator.removeAllViews(); // Hapus titik-titik lama

        int dotSize = dpToPx(6);   // Ukuran titik 6dp
        int dotMargin = dpToPx(4); // Jarak antar titik 4dp

        for (int i = 0; i < promosiList.size(); i++) {
            ImageView dot = new ImageView(getContext());
            dot.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dot_selector));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0); // Jarak kiri & kanan
            dot.setLayoutParams(params);

            layoutDotsIndicator.addView(dot);
        }
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < layoutDotsIndicator.getChildCount(); i++) {
            ImageView dot = (ImageView) layoutDotsIndicator.getChildAt(i);
            dot.setSelected(i == position);
        }
    }

    private int dpToPx(int dp) {
        // Cek jika getResources() masih valid
        if (isAdded() && getResources() != null) {
            return (int) (dp * getResources().getDisplayMetrics().density);
        }
        return 0; // Kembalikan 0 jika fragment tidak ter-attach
    }

    private void startAutoSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderRunnable = () -> {
            if (viewPagerPromosi == null || promosiSliderAdapter == null || promosiSliderAdapter.getItemCount() == 0) return;

            int currentItem = viewPagerPromosi.getCurrentItem();
            int newItem = (currentItem + 1) % promosiSliderAdapter.getItemCount();

            viewPagerPromosi.setCurrentItem(newItem, true);
        };
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
    }

    private void stopAutoSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoSlider();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (promosiSliderAdapter != null && promosiList.size() > 1) {
            startAutoSlider();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlider();
    }
    private void tampilkanJadwalAda(String tanggal, String kodeBooking, int jumlahPendaki) {
        cardJadwalAda.setVisibility(View.VISIBLE);
        cardJadwalKosong.setVisibility(View.GONE);
        tvTanggalPendakian.setText(tanggal);
        tvKodeBooking.setText(kodeBooking);
        tvJumlahPendaki.setText(jumlahPendaki + " Orang");
        btnLihatDetail.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tombol Detail Ditekan!", Toast.LENGTH_SHORT).show();
            // TODO: Nanti arahkan ke halaman detail tiket
        });
    }
    private void tampilkanJadwalKosong() {
        cardJadwalAda.setVisibility(View.GONE);
        cardJadwalKosong.setVisibility(View.VISIBLE);
    }
}