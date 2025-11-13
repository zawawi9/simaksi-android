package informasi_gunung;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zawww.e_simaksi.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.ImageView;
import android.widget.TextView;

// ===== OSMDroid import =====
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.ArrayList;
import java.util.List;

public class InformasiGunungFragment extends Fragment {

    private MapView mapView;
    private RecyclerView rvJalur;
    private MaterialButton btnDetail, btnInfo;
    private MaterialCardView cardStatus;
    private ImageView iconNotif, iconCart, logo;
    private TextView tvStatusGunung, tvDeskripsiStatus, tvUpdateWaktu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_informasi_gunung, container, false);

        // Inisialisasi komponen
        mapView = (MapView) view.findViewById(R.id.mapView);
        rvJalur = (RecyclerView) view.findViewById(R.id.rvJalur);
        btnDetail = (MaterialButton) view.findViewById(R.id.btnDetail);
        btnInfo = (MaterialButton) view.findViewById(R.id.btnInfo);
        cardStatus = (MaterialCardView) view.findViewById(R.id.cardStatusGunung);
        iconNotif = (ImageView) view.findViewById(R.id.icon_notif);
        iconCart = (ImageView) view.findViewById(R.id.icon_cart);
        logo = (ImageView) view.findViewById(R.id.logo);
        tvStatusGunung = (TextView) view.findViewById(R.id.tvStatusGunung);
        tvDeskripsiStatus = (TextView) view.findViewById(R.id.tvDeskripsiStatus);
        tvUpdateWaktu = (TextView) view.findViewById(R.id.tvUpdateWaktu);

        // Inisialisasi OSMDroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Titik-titik jalur pendakian Gunung Buthak via kucur
        GeoPoint basecamp = new GeoPoint(-7.96583, 112.54667);
        GeoPoint pintuRimba = new GeoPoint(-7.96500, 112.53417);
        GeoPoint pos1 = new GeoPoint(-7.96389, 112.52444);
        GeoPoint posBayangan2 = new GeoPoint(-7.96306, 112.51333);
        GeoPoint pos2 = new GeoPoint(-7.96528, 112.50222);
        GeoPoint pos3 = new GeoPoint(-7.840950, 112.542900);
        GeoPoint pos4 = new GeoPoint(-7.830570, 112.546300);
        GeoPoint puncak = new GeoPoint(-7.816800, 112.548700);

        // Tambahkan marker
        addMarker(basecamp, "Basecamp Panderman");
        addMarker(pintuRimba, "Pintu Rimba");
        addMarker(pos2, "Pos 1");
        addMarker(posBayangan2, "Pos bayangan 2");
        addMarker(pos2, "Pos 2");
        addMarker(pos3, "Pos 3");
        addMarker(pos4, "Pos 4");
        addMarker(puncak, "Puncak Gunung Buthak");

        // Gambar jalur pendakian
        List<GeoPoint> jalur = new ArrayList<>();
        jalur.add(basecamp);
        jalur.add(pos2);
        jalur.add(pos2);
        jalur.add(pos3);
        jalur.add(pos4);
        jalur.add(puncak);

        Polyline polyline = new Polyline();
        polyline.setPoints(jalur);
        polyline.setWidth(8f);
        polyline.setColor(0xFF2E7D32); // hijau tua
        mapView.getOverlays().add(polyline);

        // Atur kamera ke basecamp
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(basecamp);

        // Aksi tombol
        btnDetail.setOnClickListener(v -> System.out.println("Tombol Detail Kondisi ditekan"));
        btnInfo.setOnClickListener(v -> System.out.println("Tombol Info Resmi ditekan"));

        return view;
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
