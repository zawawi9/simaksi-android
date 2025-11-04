package informasi_gunung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zawww.e_simaksi.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class InformasiGunungFragment extends Fragment {

    private RecyclerView rvJalur;
    private JalurAdapter adapter;

    // Tambahkan semua komponen TextView dari XML
    private TextView tvStatusGunung, tvDeskripsiStatus, tvUpdateWaktu;
    private TextView tvCuaca, tvSuhu, tvAir;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_informasi_gunung, container, false);

        // Inisialisasi semua komponen
        tvStatusGunung = view.findViewById(R.id.tvStatusGunung);
        tvDeskripsiStatus = view.findViewById(R.id.tvDeskripsiStatus);
        tvUpdateWaktu = view.findViewById(R.id.tvUpdateWaktu);
        tvCuaca = view.findViewById(R.id.tvCuaca);
        tvSuhu = view.findViewById(R.id.tvSuhu);
        tvAir = view.findViewById(R.id.tvAir);

        rvJalur = view.findViewById(R.id.rvJalur);

        // Set waktu update otomatis
        String waktu = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        tvUpdateWaktu.setText("Diperbarui: " + waktu + " WIB");

        // (Contoh update dinamis, jika kamu ingin)
        tvStatusGunung.setText("Jalur Pendakian Buka");
        tvDeskripsiStatus.setText("Aman untuk melakukan pendakian. Tetap waspada terhadap cuaca ekstrem.");
        tvCuaca.setText("Cerah");
        tvSuhu.setText("16°C");
        tvAir.setText("Aman");

        // Set RecyclerView
        rvJalur.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new JalurAdapter(getJalurList());
        rvJalur.setAdapter(adapter);

        return view;
    }

    private ArrayList<JalurModel> getJalurList() {
        ArrayList<JalurModel> list = new ArrayList<>();
        list.add(new JalurModel("Basecamp → Pintu Rimba", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pintu Rimba → Pos 1", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos 1 → Pos Bayangan 2", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos Bayangan 2 → Pos 2", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos 2 → Pos 3", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos 3 → Pos Bayangan 4", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos Bayangan 4 → Pos 4", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos 4 → Pos 5", "Kering", "Tidak ada"));
        list.add(new JalurModel("Pos 5 → Puncak Gunung Buthak", "Kering", "Tidak ada"));
        return list;
    }
}
