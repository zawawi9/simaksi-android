package com.zawww.e_simaksi.ui.fragment;

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
import com.zawww.e_simaksi.adapter.PengumumanAdapter;
import com.zawww.e_simaksi.adapter.KuotaAdapter;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.Pengumuman;
import com.zawww.e_simaksi.model.KuotaHarian;
import com.zawww.e_simaksi.model.PengaturanBiaya;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformasiGunungFragment extends Fragment {

    private RecyclerView rvPengumuman, rvKuota;
    private PengumumanAdapter pengumumanAdapter;
    private KuotaAdapter kuotaAdapter;
    private LinearLayout layoutBiayaContainer;
    private ProgressBar progressBar;
    private LinearLayout layoutNoPengumuman;

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
        setupListeners();
        fetchAllData();
    }

    private void initViews(View view) {
        rvPengumuman = view.findViewById(R.id.rv_pengumuman);
        rvKuota = view.findViewById(R.id.rv_kuota);
        layoutBiayaContainer = view.findViewById(R.id.layout_biaya_container);
        progressBar = view.findViewById(R.id.informasi_progress_bar);
        layoutNoPengumuman = view.findViewById(R.id.layout_no_pengumuman);
    }

    private void setupRecyclerViews() {
        // Pengumuman
        rvPengumuman.setLayoutManager(new LinearLayoutManager(getContext()));
        pengumumanAdapter = new PengumumanAdapter(new ArrayList<>());
        rvPengumuman.setAdapter(pengumumanAdapter);

        // Kuota
        rvKuota.setLayoutManager(new LinearLayoutManager(getContext()));
        kuotaAdapter = new KuotaAdapter(new ArrayList<>());
        rvKuota.setAdapter(kuotaAdapter);
    }

    private void setupListeners() {
        // No listeners needed for now
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
            public void onSuccess(List<Pengumuman> pengumumanList) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (pengumumanList.isEmpty()) {
                            layoutNoPengumuman.setVisibility(View.VISIBLE);
                            rvPengumuman.setVisibility(View.GONE);
                        } else {
                            layoutNoPengumuman.setVisibility(View.GONE);
                            rvPengumuman.setVisibility(View.VISIBLE);
                            pengumumanAdapter.updateData(pengumumanList);
                        }
                        showLoading(false);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        layoutNoPengumuman.setVisibility(View.VISIBLE);
                        rvPengumuman.setVisibility(View.GONE);
                        showLoading(false);
                    });
                }
            }
        });
    }

    private void fetchKuota() {
        SupabaseAuth.getKuotaMingguan(new SupabaseAuth.KuotaListCallback() {
            @Override
            public void onSuccess(List<KuotaHarian> kuotaList) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        kuotaAdapter.updateData(kuotaList);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Optionally show an error message to the user
                        // ErrorHandler.showError(requireView(), "Gagal memuat kuota: " + errorMessage);
                        showLoading(false);
                    });
                }
            }
        });
    }

    private void fetchBiaya() {
        SupabaseAuth.getPengaturanBiaya(new SupabaseAuth.BiayaCallback() {
            @Override
            public void onSuccess(List<PengaturanBiaya> biayaList) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateBiayaUI(biayaList);
                        showLoading(false); // Hide loading after all data is fetched and UI updated
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Optionally show an error message to the user
                        // ErrorHandler.showError(requireView(), "Gagal memuat biaya: " + errorMessage);
                        showLoading(false);
                    });
                }
            }
        });
    }

    private void updateBiayaUI(List<PengaturanBiaya> biayaList) {
        if (getContext() == null) return;
        layoutBiayaContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRp = NumberFormat.getCurrencyInstance(localeID);

        for (PengaturanBiaya biaya : biayaList) {
            View biayaView = inflater.inflate(R.layout.item_biaya, layoutBiayaContainer, false);
            TextView tvNama = biayaView.findViewById(R.id.tv_biaya_nama);
            TextView tvHarga = biayaView.findViewById(R.id.tv_biaya_harga);

            tvNama.setText(biaya.getNamaItem());
            tvHarga.setText(formatRp.format(biaya.getHarga()));
            layoutBiayaContainer.addView(biayaView);
        }
    }


    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}