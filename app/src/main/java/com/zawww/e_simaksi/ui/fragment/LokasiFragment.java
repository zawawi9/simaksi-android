package com.zawww.e_simaksi.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment; // Changed from AppCompatActivity

import com.zawww.e_simaksi.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Locale;

public class LokasiFragment extends Fragment { // Changed from AppCompatActivity

    private MapView mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    // Updated coordinates
    private final GeoPoint basecampKucur = new GeoPoint(-7.9659126, 112.54681);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_lokasi, container, false);

        // Essential osmdroid configuration
        // Use requireContext() for Fragment context
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize MapView
        mMapView = view.findViewById(R.id.map); // Use fragment's view
        mMapView.setTileSource(TileSourceFactory.MAPNIK); // Standard tile source
        mMapView.setMultiTouchControls(true);

        // Set initial map center and zoom
        mMapView.getController().setZoom(15.0);
        mMapView.getController().setCenter(basecampKucur);

        // Add marker for Basecamp Kucur
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(basecampKucur);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Basecamp Kucur, Gunung Butak");
        mMapView.getOverlays().add(startMarker);

        // Check for location permissions
        checkAndRequestLocationPermissions();

        // Setup Navigation Button
        Button btnNavigasi = view.findViewById(R.id.btn_mulai_navigasi); // Use fragment's view
        btnNavigasi.setOnClickListener(v -> {
            String uriString = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", basecampKucur.getLatitude(), basecampKucur.getLongitude());
            Uri gmmIntentUri = Uri.parse(uriString);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) { // Use requireContext()
                startActivity(mapIntent);
            } else {
                Toast.makeText(requireContext(), "Aplikasi Google Maps tidak ditemukan. Silakan install terlebih dahulu.", Toast.LENGTH_LONG).show(); // Use requireContext()
            }
        });
    }

    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Use Fragment's requestPermissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMapView != null) {
                // Use requireContext() for GpsMyLocationProvider
                mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mMapView);
                mLocationOverlay.enableMyLocation();
                mMapView.getOverlays().add(mLocationOverlay);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Izin lokasi ditolak. Fitur 'My Location' tidak akan aktif.", Toast.LENGTH_LONG).show(); // Use requireContext()
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // This is important for osmdroid's lifecycle
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // This is important for osmdroid's lifecycle
        if (mMapView != null) {
            mMapView.onPause();
        }
    }
}