package dashboard;

import android.os.Bundle;
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

import com.zawww.e_simaksi.R;

public class HomeFragment extends Fragment {

    // Komponen UI
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

        // Inisialisasi komponen UI
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

        // Tampilkan kondisi default
        tampilkanJadwalKosong();

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
}