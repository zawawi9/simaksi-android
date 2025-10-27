package dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.Promosi;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    // --- Konstanta untuk Logging ---
    private static final String PROMO_DEBUG_TAG = "PROMO_DEBUG";

    // --- Komponen UI ---
    private ViewPager2 viewPager;
    private PromosiSliderAdapter promosiSliderAdapter;
    private CardView cardJadwalKosong;
    private CardView cardJadwalAda;
    private TextView tvTanggalPendakian;
    private TextView tvKodeBooking;
    private TextView tvJumlahPendaki;
    private Button btnLihatDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Inisialisasi Slider Promosi ---
        viewPager = view.findViewById(R.id.view_pager_promosi); // Pastikan ID ini ada di XML
        promosiSliderAdapter = new PromosiSliderAdapter(new ArrayList<>(), getContext());
        viewPager.setAdapter(promosiSliderAdapter);

        // --- Inisialisasi Komponen Jadwal ---
        cardJadwalKosong = view.findViewById(R.id.card_jadwal_kosong);
        cardJadwalAda = view.findViewById(R.id.card_jadwal_ada);
        tvTanggalPendakian = view.findViewById(R.id.tv_tanggal_pendakian);
        tvKodeBooking = view.findViewById(R.id.tv_kode_booking);
        tvJumlahPendaki = view.findViewById(R.id.tv_jumlah_pendaki);
        btnLihatDetail = view.findViewById(R.id.btn_lihat_detail);

        // Cegah crash jika ada ID yang belum ada di XML
        if (cardJadwalKosong == null || cardJadwalAda == null) {
            Toast.makeText(getContext(),
                    "Layout belum lengkap! Periksa ID di fragment_home.xml",
                    Toast.LENGTH_SHORT).show();
            return view;
        }

        // Tampilkan kondisi default & ambil data
        tampilkanJadwalKosong();
        fetchPromosiData(); // Ambil data promosi dari Supabase

        // Untuk test UI jika ada data, aktifkan baris di bawah:
        // tampilkanJadwalAda("Sabtu, 11 Oktober 2025", "ESIMAKSI-ABC123XYZ", 5);

        return view;
    }

    /** Menampilkan card JIKA ADA JADWAL **/
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

    /** Menampilkan card JIKA TIDAK ADA JADWAL **/
    private void tampilkanJadwalKosong() {
        cardJadwalAda.setVisibility(View.GONE);
        cardJadwalKosong.setVisibility(View.VISIBLE);
    }

    /** Mengambil data promosi dari Supabase untuk slider **/
    private void fetchPromosiData() {
        // 1. Log saat method dipanggil
        Log.d(PROMO_DEBUG_TAG, "fetchPromosiData() dipanggil.");

        SupabaseAuth.getPromosiPoster(new SupabaseAuth.PromosiCallback() {
            @Override
            public void onSuccess(List<Promosi> promosiList) {
                // 2. Log jumlah data yang diterima
                Log.d(PROMO_DEBUG_TAG, "onSuccess: Menerima " + promosiList.size() + " data promosi.");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!promosiList.isEmpty()) {
                            promosiSliderAdapter.updateData(promosiList);
                            viewPager.setVisibility(View.VISIBLE);
                        } else {
                            viewPager.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                // 3. Log jika terjadi error
                Log.e(PROMO_DEBUG_TAG, "onError: Gagal mengambil data - " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Gagal memuat promosi: " + errorMessage, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}