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

import com.zawww.e_simaksi.R; // Pastikan import R ini benar

public class HomeFragment extends Fragment {

    // Deklarasikan variabel untuk semua komponen UI
    private CardView cardJadwalKosong;
    private CardView cardJadwalAda;
    private TextView tvTanggalPendakian;
    private TextView tvKodeBooking;
    private TextView tvJumlahPendaki;
    private Button btnLihatDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menghubungkan layout XML dengan file Java ini
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi semua komponen UI yang ada di layout
        //cardJadwalKosong = view.findViewById(R.id.card_jadwal_kosong);
        //cardJadwalAda = view.findViewById(R.id.card_jadwal_ada);
        //tvTanggalPendakian = view.findViewById(R.id.tv_tanggal_pendakian);
        //tvKodeBooking = view.findViewById(R.id.tv_kode_booking);
        //tvJumlahPendaki = view.findViewById(R.id.tv_jumlah_pendaki);
        //btnLihatDetail = view.findViewById(R.id.btn_lihat_detail);

        // Nanti, di sini Anda akan memanggil data dari Supabase.
        // Untuk sekarang, kita bisa panggil salah satu fungsi di bawah untuk testing UI.

        // Tampilkan kondisi default (tidak ada jadwal)
        tampilkanJadwalKosong();

        // Untuk mencoba tampilan jika ada data, hapus baris di atas dan aktifkan baris di bawah ini:
        // tampilkanJadwalAda("Sabtu, 11 Oktober 2025", "ESIMAKSI-ABC123XYZ", 5);

        return view;
    }

    /**
     * Fungsi ini dipanggil untuk menampilkan card JIKA ADA JADWAL.
     * @param tanggal String tanggal pendakian
     * @param kodeBooking String kode booking
     * @param jumlahPendaki integer jumlah pendaki
     */
    private void tampilkanJadwalAda(String tanggal, String kodeBooking, int jumlahPendaki) {
        cardJadwalAda.setVisibility(View.VISIBLE);
        cardJadwalKosong.setVisibility(View.GONE);

        tvTanggalPendakian.setText(tanggal);
        tvKodeBooking.setText(kodeBooking);
        tvJumlahPendaki.setText(jumlahPendaki + " Orang");

        btnLihatDetail.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tombol Detail Ditekan!", Toast.LENGTH_SHORT).show();
            // Nanti di sini bisa diisi logika pindah halaman ke detail tiket
        });
    }

    /**
     * Fungsi ini dipanggil untuk menampilkan card JIKA TIDAK ADA JADWAL.
     */
    private void tampilkanJadwalKosong() {
        cardJadwalAda.setVisibility(View.GONE);
        cardJadwalKosong.setVisibility(View.VISIBLE);
    }
}