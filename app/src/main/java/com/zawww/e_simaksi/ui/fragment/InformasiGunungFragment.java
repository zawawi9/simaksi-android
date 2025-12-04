package com.zawww.e_simaksi.ui.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.adapter.KuotaAdapter;
import com.zawww.e_simaksi.adapter.PengumumanAdapter;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.KuotaHarian;
import com.zawww.e_simaksi.model.PengaturanBiaya;
import com.zawww.e_simaksi.model.Pengumuman;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InformasiGunungFragment extends Fragment {

    private RecyclerView rvPengumuman, rvKuota, rvJalur;
    private PengumumanAdapter pengumumanAdapter;
    private KuotaAdapter kuotaAdapter;
    private LinearLayout layoutBiayaContainer, layoutNoPengumuman;
    private ProgressBar progressBar;
    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_informasi_gunung, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupMap();     // 👉 Semua kode map dipusatkan di sini
        fetchAllData(); // Ambil data dari server
    }

    private void initViews(View view) {
        rvPengumuman = view.findViewById(R.id.rv_pengumuman);
        rvKuota = view.findViewById(R.id.rv_kuota);
        rvJalur = view.findViewById(R.id.rvJalur);
        layoutBiayaContainer = view.findViewById(R.id.layout_biaya_container);
        layoutNoPengumuman = view.findViewById(R.id.layout_no_pengumuman);
        progressBar = view.findViewById(R.id.informasi_progress_bar);

        mapView = view.findViewById(R.id.mapView);
    }

    // ================================
    //          MAP INITIALIZATION
    // ================================
    private void setupMap() {

        // OSMDroid Settings
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Titik koordinat pendakian
        GeoPoint basecamp = new GeoPoint(-7.96583, 112.54667);
        GeoPoint pintuRimba = new GeoPoint(-7.96500, 112.53417);
        GeoPoint pos1 = new GeoPoint(-7.96389, 112.52444);
        GeoPoint mataAir1 = new GeoPoint(-7.962833, 112.513944);
        GeoPoint posBayangan2 = new GeoPoint(-7.96306, 112.51333);
        GeoPoint pos2 = new GeoPoint(-7.96528, 112.50222);
        GeoPoint mataAir2 = new GeoPoint(-7.96725, 112.50000);
        GeoPoint pos3 = new GeoPoint(-7.95833, 112.49833);
        GeoPoint posBayangan4 = new GeoPoint(-7.95667, 112.48667);
        GeoPoint pos4 = new GeoPoint(-7.95833, 112.48472);
        GeoPoint pos5 = new GeoPoint(-7.95444, 112.47583);
        GeoPoint puncak = new GeoPoint(-7.95558, 112.46531);

        // Tambah marker
        addCustomMarker(basecamp, "Basecamp Buthak via Kucur", R.drawable.ic_basecamp);
        addCustomMarker(pintuRimba, "Pintu Rimba", R.drawable.ic_pos);
        addCustomMarker(pos1, "Pos 1 Gantangan", R.drawable.ic_pos);
        addCustomMarker(posBayangan2, "Pos Bayangan 2", R.drawable.ic_pos);
        addCustomMarker(pos2, "Pos 2 Kuburan Perawan", R.drawable.ic_pos);
        addCustomMarker(pos3, "Pos 3 Gunung Malang", R.drawable.ic_pos);
        addCustomMarker(posBayangan4, "Pos Bayangan 4", R.drawable.ic_pos);
        addCustomMarker(pos4, "Pos 4 Gunung Gentong", R.drawable.ic_tenda);
        addCustomMarker(pos5, "Pos 5", R.drawable.ic_pos);
        addCustomMarker(mataAir1, "Sumber Mata Air 1", R.drawable.ic_mata_air);
        addCustomMarker(mataAir2, "Sumber Mata Air 2", R.drawable.ic_mata_air);
        addCustomMarker(puncak, "Puncak Gunung Buthak 2868 MDPL", R.drawable.ic_puncak);

        // Polyline jalur pendakian
        List<GeoPoint> jalur = new ArrayList<>();
        jalur.add(basecamp);
        jalur.add(pintuRimba);
        jalur.add(pos1);
        jalur.add(posBayangan2);
        jalur.add(pos2);
        jalur.add(pos3);
        jalur.add(posBayangan4);
        jalur.add(pos4);
        jalur.add(pos5);
        jalur.add(puncak);

        Polyline polyline = new Polyline();
        polyline.setPoints(jalur);
        polyline.setWidth(7f);
        polyline.setColor(Color.parseColor("#1B5E20"));

        Paint paint = polyline.getOutlinePaint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);

        mapView.getOverlays().add(polyline);

        // Fokus kamera awal
        mapView.getController().setZoom(13.5);
        mapView.getController().setCenter(basecamp);
    }

    private void addCustomMarker(GeoPoint point, String title, int iconRes) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(iconRes, null));
        mapView.getOverlays().add(marker);
    }

    // ================================
    //             FETCH DATA
    // ================================

    private void setupRecyclerViews() {
        rvPengumuman.setLayoutManager(new LinearLayoutManager(getContext()));
        pengumumanAdapter = new PengumumanAdapter(new ArrayList<>());
        rvPengumuman.setAdapter(pengumumanAdapter);

        rvKuota.setLayoutManager(new LinearLayoutManager(getContext()));
        kuotaAdapter = new KuotaAdapter(new ArrayList<>());
        rvKuota.setAdapter(kuotaAdapter);
    }

    private void fetchAllData() {
        showLoading(true);
        fetchPengumuman();
        fetchKuota();
        fetchBiaya();
    }

    private void fetchPengumuman() {
        SupabaseAuth.getAktifPengumuman(new SupabaseAuth.PengumumanCallback() {
            @Override
            public void onSuccess(List<Pengumuman> list) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (list.isEmpty()) {
                        layoutNoPengumuman.setVisibility(View.VISIBLE);
                        rvPengumuman.setVisibility(View.GONE);
                    } else {
                        layoutNoPengumuman.setVisibility(View.GONE);
                        rvPengumuman.setVisibility(View.VISIBLE);
                        pengumumanAdapter.updateData(list);
                    }
                    showLoading(false);
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    layoutNoPengumuman.setVisibility(View.VISIBLE);
                    rvPengumuman.setVisibility(View.GONE);
                    showLoading(false);
                });
            }
        });
    }

    private void fetchKuota() {
        SupabaseAuth.getKuotaMingguan(new SupabaseAuth.KuotaListCallback() {
            @Override
            public void onSuccess(List<KuotaHarian> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> kuotaAdapter.updateData(list));
                }
            }
            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void fetchBiaya() {
        SupabaseAuth.getPengaturanBiaya(new SupabaseAuth.BiayaCallback() {
            @Override
            public void onSuccess(List<PengaturanBiaya> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateBiayaUI(list);
                        showLoading(false);
                    });
                }
            }
            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void updateBiayaUI(List<PengaturanBiaya> list) {
        layoutBiayaContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        for (PengaturanBiaya biaya : list) {
            View biayaView = inflater.inflate(R.layout.item_biaya, layoutBiayaContainer, false);
            TextView tvNama = biayaView.findViewById(R.id.tv_biaya_nama);
            TextView tvHarga = biayaView.findViewById(R.id.tv_biaya_harga);

            tvNama.setText(biaya.getNamaItem());
            tvHarga.setText(rupiah.format(biaya.getHarga()));

            layoutBiayaContainer.addView(biayaView);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
