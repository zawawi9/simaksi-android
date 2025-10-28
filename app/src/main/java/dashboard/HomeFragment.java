package dashboard;

import android.app.Dialog;
import android.content.Context;
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
import com.zawww.e_simaksi.model.Promosi;
import com.zawww.e_simaksi.model.Reservasi;
import FITURLOGIN.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // --- Konstanta ---
    private static final String PROMO_DEBUG_TAG = "PROMO_DEBUG";
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

        // --- Setup Adapter Slider ---
        promosiSliderAdapter = new PromosiSliderAdapter(new ArrayList<>(), getContext());
        viewPagerPromosi.setAdapter(promosiSliderAdapter);

        // Ambil SwipeRefreshLayout dari Activity induk (MainActivity)
        if (getActivity() instanceof MainActivity) {
            swipeRefreshLayout = ((MainActivity) getActivity()).getSwipeRefreshLayout();
        }

        // --- Memuat Data ---
        // 1. Cek apakah ada jadwal aktif
        fetchJadwalAktif();

        // 2. Ambil data untuk slider promosi
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
        Log.d(PROMO_DEBUG_TAG, "fetchPromosiData() dipanggil.");
        SupabaseAuth.getPromosiPoster(new SupabaseAuth.PromosiCallback() {
            @Override
            public void onSuccess(List<Promosi> data) {
                Log.d(PROMO_DEBUG_TAG, "onSuccess: Menerima " + data.size() + " data promosi.");
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
                Log.e(PROMO_DEBUG_TAG, "onError: Gagal mengambil data - " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Gagal memuat promosi", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // --- BAGIAN SLIDER PROMOSI ---
    // (Tidak ada perubahan di sini, semua sudah benar)

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
                Toast.makeText(getContext(), "Error: ID Reservasi tidak valid", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Menampilkan popup (Dialog) dengan semua detail reservasi
     */
    private void showDetailDialog(Reservasi reservasi) {
        if (getContext() == null) return; // Pastikan fragment masih ter-attach

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detail_reservasi); // Layout XML yang kita buat

        // Atur agar dialog lebar dan punya background transparan
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 1. Inisialisasi semua View dari layout dialog
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

        // 2. Isi data utama
        tvKode.setText("Kode: " + reservasi.getKodeReservasi());
        tvTanggal.setText("Tanggal Pendakian: " + reservasi.getTanggalPendakian());
        tvDipesan.setText("Dipesan: " + reservasi.getDipesanPada()); // Format tanggal jika perlu
        tvStatus.setText("Status: " + reservasi.getStatus());
        tvStatusSampah.setText("Status Sampah: " + reservasi.getStatusSampah());

        // 3. Isi data pembayaran
        tvJmlPendaki.setText(reservasi.getJumlahPendaki() + " orang");
        tvTiketParkir.setText(reservasi.getJumlahTiketParkir() + " tiket");
        tvTotalHarga.setText("Rp " + reservasi.getTotalHarga()); // Format mata uang jika perlu

        // 4. Isi daftar pendaki secara dinamis
        llPendaki.removeAllViews(); // Kosongkan dulu
        if (reservasi.getPendakiRombongan() != null && !reservasi.getPendakiRombongan().isEmpty()) {
            for (PendakiRombongan pendaki : reservasi.getPendakiRombongan()) {
                // Buat layout kecil untuk tiap pendaki
                LinearLayout layoutPendaki = new LinearLayout(getContext());
                layoutPendaki.setOrientation(LinearLayout.VERTICAL);
                layoutPendaki.setPadding(0, dpToPx(4), 0, dpToPx(4)); // Beri jarak antar pendaki

                TextView tvNama = new TextView(getContext());
                tvNama.setText(pendaki.getNamaLengkap() + " (" + pendaki.getNik() + ")");
                tvNama.setTextSize(14);
                tvNama.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                tvNama.setTypeface(tvNama.getTypeface(), android.graphics.Typeface.BOLD);

                TextView tvKontak = new TextView(getContext());
                tvKontak.setText("Telp: " + pendaki.getNomorTelepon() + " | Darurat: " + pendaki.getKontakDarurat());
                tvKontak.setTextSize(12);

                TextView tvAlamat = new TextView(getContext());
                tvAlamat.setText("Alamat: " + pendaki.getAlamat());
                tvAlamat.setTextSize(12);

                layoutPendaki.addView(tvNama);
                layoutPendaki.addView(tvKontak);
                layoutPendaki.addView(tvAlamat);

                // Tampilkan Link Surat Sehat jika ada
                if (pendaki.getUrlSuratSehat() != null && !pendaki.getUrlSuratSehat().isEmpty()) {
                    TextView tvSurat = new TextView(getContext());
                    tvSurat.setText("Lihat Surat Sehat"); // Nanti bisa Anda buat clickable
                    tvSurat.setTextSize(12);
                    tvSurat.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_500)); // Ganti warnanya
                    // tvSurat.setOnClickListener(v -> /* Buka link */);
                    layoutPendaki.addView(tvSurat);
                }

                llPendaki.addView(layoutPendaki);
            }
        } else {
            // Jika tidak ada data rombongan
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data rombongan tidak ditemukan.");
            tvKosong.setTextSize(12);
            llPendaki.addView(tvKosong);
        }

        // 5. Isi daftar sampah secara dinamis
        llSampah.removeAllViews(); // Kosongkan dulu
        if (reservasi.getBarangBawaanSampah() != null && !reservasi.getBarangBawaanSampah().isEmpty()) {
            for (BarangBawaanSampah barang : reservasi.getBarangBawaanSampah()) {
                String teksBarang = "• " + barang.getNamaBarang() + " (" + barang.getJenisSampah() + ") - Jumlah: " + barang.getJumlah();
                TextView tvBarang = new TextView(getContext());
                tvBarang.setText(teksBarang);
                tvBarang.setTextSize(14);
                tvBarang.setPadding(0, dpToPx(2), 0, dpToPx(2)); // Beri jarak
                llSampah.addView(tvBarang);
            }
        } else {
            // Jika tidak ada data sampah
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data sampah tidak ditemukan.");
            tvKosong.setTextSize(12);
            llSampah.addView(tvKosong);
        }

        // 6. Atur tombol close
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // 7. Tampilkan dialog
        dialog.show();
    }
}