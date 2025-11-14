package com.zawww.e_simaksi.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.core.view.WindowCompat;

import com.zawww.e_simaksi.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Locale;

public class LokasiActivity extends AppCompatActivity {

    private MapView mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    private final GeoPoint basecampKucur = new GeoPoint(-7.923, 112.516); // Koordinat Basecamp Kucur
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Essential osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_lokasi);

        // Set status bar icons to dark for better visibility on light backgrounds
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_lokasi);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize MapView
        mMapView = findViewById(R.id.map);
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
        Button btnNavigasi = findViewById(R.id.btn_mulai_navigasi);
        btnNavigasi.setOnClickListener(v -> {
            String uriString = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", basecampKucur.getLatitude(), basecampKucur.getLongitude());
            Uri gmmIntentUri = Uri.parse(uriString);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Aplikasi Google Maps tidak ditemukan. Silakan install terlebih dahulu.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMapView != null) {
                mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMapView);
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
                Toast.makeText(this, "Izin lokasi ditolak. Fitur 'My Location' tidak akan aktif.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This is important for osmdroid's lifecycle
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // This is important for osmdroid's lifecycle
        if (mMapView != null) {
            mMapView.onPause();
        }
    }
}
