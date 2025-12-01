package com.zawww.e_simaksi.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.PendakiRombongan;

import com.zawww.e_simaksi.model.Reservasi;
import com.zawww.e_simaksi.util.SessionManager;
import com.zawww.e_simaksi.adapter.PromosiSliderAdapter;
import com.zawww.e_simaksi.model.Promosi;

import com.zawww.e_simaksi.ui.activity.MainActivity;
import com.zawww.e_simaksi.util.ErrorHandler;


import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // --- Konstanta ---
    private static final String PROMOSI_SLIDER_DEBUG_TAG = "PROMOSI_SLIDER_DEBUG";
    private static final long SLIDER_DELAY_MS = 5000;

    // --- UI Views ---
    private ViewPager2 viewPagerPromosi;
    private LinearLayout layoutDotsIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CardView cardJadwalKosong;
    private CardView cardJadwalAda;
    private TextView tvTanggalPendakian;
    private TextView tvKodeBooking;
    private TextView tvJumlahPendaki;
    private Button btnLihatDetail;

    // --- Slider ---
    private PromosiSliderAdapter promosiSliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Promosi> promosiList = new ArrayList<>();

    // --- State ---
    private long idReservasiAktif = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Inisialisasi Semua View ---
        viewPagerPromosi = view.findViewById(R.id.view_pager_promosi);
        layoutDotsIndicator = view.findViewById(R.id.layout_dots_indicator);
        cardJadwalKosong = view.findViewById(R.id.card_jadwal_kosong);
        cardJadwalAda = view.findViewById(R.id.card_jadwal_ada);
        tvTanggalPendakian = view.findViewById(R.id.tv_tanggal_pendakian);
        tvKodeBooking = view.findViewById(R.id.tv_kode_booking);
        tvJumlahPendaki = view.findViewById(R.id.tv_jumlah_pendaki);
        btnLihatDetail = view.findViewById(R.id.btn_lihat_detail);
        CardView cardPesanTiket = view.findViewById(R.id.card_pesan_tiket);
        CardView cardCuaca = view.findViewById(R.id.card_cuaca);
        CardView cardLokasi = view.findViewById(R.id.card_lokasi);
        CardView cardInformasiGunung = view.findViewById(R.id.card_informasi_gunung); // Keep this


        cardInformasiGunung.setOnClickListener(v -> { // Keep this
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(new InformasiGunungFragment());
            }
        });

        cardLokasi.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(new com.zawww.e_simaksi.ui.fragment.LokasiFragment());
            }
        });

        cardCuaca.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(new WeatherFragment());
            }
        });

        cardPesanTiket.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(new ReservasiFragment());
            }
        });

        // --- Setup Adapter Slider ---
        promosiSliderAdapter = new PromosiSliderAdapter(new ArrayList<>(), getContext());
        viewPagerPromosi.setAdapter(promosiSliderAdapter);

        // Ambil SwipeRefreshLayout dari Activity induk (MainActivity)
        if (getActivity() instanceof MainActivity) {
            swipeRefreshLayout = ((MainActivity) getActivity()).getSwipeRefreshLayout();
        }

        // --- Memuat Data ---
        fetchJadwalAktif();
        fetchPromosiData();
    }


    private void fetchJadwalAktif() {
        if (getContext() == null) return;
        SessionManager sessionManager = new SessionManager(getContext());
        String userId = sessionManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            // Jika tidak ada user ID (belum login), tampilkan card 'kosong'
            Log.e("HomeFragment", "User ID tidak ditemukan di Session, menampilkan jadwal kosong.");
            tampilkanJadwalKosong();
            return;
        }

        Log.d("HomeFragment", "Mencari jadwal aktif untuk user: " + userId);

        // Panggil API
        SupabaseAuth.getJadwalAktif(userId, new SupabaseAuth.JadwalCallback() {
            @Override
            public void onSuccess(Reservasi jadwal) {
                // JADWAL DITEMUKAN!
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // Tampilkan card yang 'Ada Jadwal'
                    tampilkanJadwalAda(
                            jadwal.getIdReservasi(),
                            jadwal.getTanggalPendakian(),
                            jadwal.getKodeReservasi(),
                            jadwal.getJumlahPendaki()
                    );
                });
            }

            @Override
            public void onError(String errorMessage) {
                // TIDAK ADA JADWAL (atau error)
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Log.d("HomeFragment", "Gagal/Tidak ada jadwal: " + errorMessage);
                    tampilkanJadwalKosong(); // Tampilkan card 'Kosong'
                });
            }
        });
    }

    /**
     * Mengambil data promosi untuk slider
     */
    private void fetchPromosiData() {
        Log.d(PROMOSI_SLIDER_DEBUG_TAG, "fetchPromosiData() dipanggil.");
        SupabaseAuth.getPromosiPoster(new SupabaseAuth.PromosiCallback() {
            @Override
            public void onSuccess(List<Promosi> data) {
                Log.d(PROMOSI_SLIDER_DEBUG_TAG, "onSuccess: Menerima " + data.size() + " data promosi.");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        promosiList.clear();
                        promosiList.addAll(data);
                        setupSlider(); // Panggil setupSlider setelah data siap
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(PROMOSI_SLIDER_DEBUG_TAG, "onError: Gagal mengambil data promosi - " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            ErrorHandler.showError(requireView(), "Gagal memuat promosi"));
                }
            }
        });
    }

    // --- BAGIAN SLIDER PROMOSI ---
    // (Tidak ada perubahan di sini, semua sudah benar)

    private void setupSlider() {
        if (getContext() == null || promosiList.isEmpty()) {
            Log.w(PROMOSI_SLIDER_DEBUG_TAG, "List promosi kosong, slider disembunyikan.");
            viewPagerPromosi.setVisibility(View.GONE);
            layoutDotsIndicator.setVisibility(View.GONE);
            return;
        }

        Log.d(PROMOSI_SLIDER_DEBUG_TAG, "setupSlider() dipanggil. Ukuran list: " + promosiList.size());

        viewPagerPromosi.setVisibility(View.VISIBLE);
        layoutDotsIndicator.setVisibility(View.VISIBLE);

        // 1. Update data di Adapter
        promosiSliderAdapter.updateData(promosiList);

        // 2. Buat titik-titik indikator
        setupDotsIndicator();

        // 3. Atasi Konflik Geser dengan SwipeRefreshLayout
        viewPagerPromosi.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (swipeRefreshLayout != null) {
                    // Nonaktifkan swipe-to-refresh HANYA saat ViewPager sedang digeser
                    swipeRefreshLayout.setEnabled(state != ViewPager2.SCROLL_STATE_DRAGGING);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update titik aktif
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
            Log.d(PROMOSI_SLIDER_DEBUG_TAG, "Memulai auto-slider...");
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
        if (isAdded() && getResources() != null) {
            return (int) (dp * getResources().getDisplayMetrics().density);
        }
        return 0;
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

    // --- Lifecycle Methods (untuk slider) ---
    // (Tidak ada perubahan di sini, semua sudah benar)

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

    // --- BAGIAN KARTU JADWAL ---
    // (Tidak ada perubahan di sini, semua sudah benar)

    /**
     * Menampilkan card "Pendakian Anda Selanjutnya"
     */
    private void tampilkanJadwalAda(long idReservasi, String tanggal, String kodeBooking, int jumlahPendaki) {
        this.idReservasiAktif = idReservasi; // Simpan ID-nya untuk dialog detail

        cardJadwalAda.setVisibility(View.VISIBLE);
        cardJadwalKosong.setVisibility(View.GONE);
        tvTanggalPendakian.setText(tanggal);
        tvKodeBooking.setText(kodeBooking);
        tvJumlahPendaki.setText(jumlahPendaki + " Orang");

        // Atur OnClickListener untuk memanggil dialog
        btnLihatDetail.setOnClickListener(v -> {
            panggilDetailReservasi();
        });
    }

    /**
     * Menampilkan card "Belum Ada Pendakian"
     */
    private void tampilkanJadwalKosong() {
        cardJadwalAda.setVisibility(View.GONE);
        cardJadwalKosong.setVisibility(View.VISIBLE);
        this.idReservasiAktif = -1L; // Reset ID
    }


    // --- BAGIAN DIALOG DETAIL RESERVASI ---
    // (Tidak ada perubahan di sini, semua sudah benar)

    /**
     * Memanggil API untuk mengambil detail lengkap reservasi
     */
    private void panggilDetailReservasi() {
        if (idReservasiAktif == -1L) {
            if (getContext() != null) {
                ErrorHandler.showError(requireView(), "Error: ID Reservasi tidak valid");
            }
            return;
        }

        // Tampilkan loading sederhana
        if (getContext() != null) {
            Toast.makeText(getContext(), "Memuat detail...", Toast.LENGTH_SHORT).show();
        }

        // Panggil API
        SupabaseAuth.getDetailReservasi((int) idReservasiAktif, new SupabaseAuth.DetailReservasiCallback() {
            @Override
            public void onSuccess(Reservasi reservasi) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // Sembunyikan loading
                    showDetailDialog(reservasi); // Tampilkan popup
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // Sembunyikan loading
                    ErrorHandler.showError(requireView(), errorMessage);
                });
            }
        });
    }

    /**
     * Menampilkan popup (Dialog) dengan semua detail reservasi
     */
    private void showDetailDialog(Reservasi reservasi) {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detail_reservasi);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        TextView tvKode = dialog.findViewById(R.id.tv_detail_kode);
        TextView tvTanggal = dialog.findViewById(R.id.tv_detail_tanggal);
        TextView tvDipesan = dialog.findViewById(R.id.tv_detail_dipesan_pada);
        TextView tvStatus = dialog.findViewById(R.id.tv_detail_status);
        TextView tvStatusSampah = dialog.findViewById(R.id.tv_detail_status_sampah);
        LinearLayout llPendaki = dialog.findViewById(R.id.ll_container_pendaki);
        LinearLayout llSampah = dialog.findViewById(R.id.ll_container_sampah);
        TextView tvJmlPendaki = dialog.findViewById(R.id.tv_detail_jumlah_pendaki);
        TextView tvTiketParkir = dialog.findViewById(R.id.tv_detail_tiket_parkir);
        TextView tvTotalHarga = dialog.findViewById(R.id.tv_detail_total_harga);

        tvKode.setText(reservasi.getKodeReservasi());

        // Format tanggal pendakian
        String tanggalPendakianFormatted = reservasi.getTanggalPendakian();
        try {
            java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = inputSdf.parse(reservasi.getTanggalPendakian());
            if (date != null) {
                java.text.SimpleDateFormat outputSdf = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("id", "ID"));
                tanggalPendakianFormatted = outputSdf.format(date);
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        tvTanggal.setText(tanggalPendakianFormatted);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            tvDipesan.setText(com.zawww.e_simaksi.util.DateUtil.formatDate(reservasi.getDipesanPada()));
        } else {
            tvDipesan.setText(reservasi.getDipesanPada());
        }
        tvStatus.setText(reservasi.getStatus());
        tvStatusSampah.setText(reservasi.getStatusSampah());

        tvJmlPendaki.setText(reservasi.getJumlahPendaki() + " orang");

        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("in", "ID"));
        String hargaParkir = currencyFormat.format(reservasi.getJumlahTiketParkir() * 5000L);
        tvTiketParkir.setText(hargaParkir.replace("Rp", "Rp "));

        String totalHarga = currencyFormat.format(reservasi.getTotalHarga());
        tvTotalHarga.setText(totalHarga.replace("Rp", "Rp "));

        llPendaki.removeAllViews();
        if (reservasi.getPendakiRombongan() != null && !reservasi.getPendakiRombongan().isEmpty()) {
            for (PendakiRombongan pendaki : reservasi.getPendakiRombongan()) {
                TextView tv = new TextView(getContext());
                tv.setText("\u2022 " + pendaki.getNamaLengkap() + " (" + pendaki.getNik() + ")");
                llPendaki.addView(tv);
            }
        } else {
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data rombongan tidak ditemukan.");
            llPendaki.addView(tvKosong);
        }

        llSampah.removeAllViews();
        if (reservasi.getBarangBawaanSampah() != null && !reservasi.getBarangBawaanSampah().isEmpty()) {
            for (BarangBawaanSampah barang : reservasi.getBarangBawaanSampah()) {
                TextView tv = new TextView(getContext());
                tv.setText("\u2022 " + barang.getNamaBarang() + " (" + barang.getJumlah() + " buah)");
                llSampah.addView(tv);
            }
        } else {
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data sampah tidak ditemukan.");
            llSampah.addView(tvKosong);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}