package com.zawww.e_simaksi.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.ui.activity.MainActivity; // Import MainActivity Anda
import com.zawww.e_simaksi.viewmodel.ReservasiSharedViewModel;
import com.zawww.e_simaksi.ui.fragment.ReservasiStep1Fragment;
import com.zawww.e_simaksi.ui.fragment.ReservasiStep2Fragment;
import com.zawww.e_simaksi.ui.fragment.ReservasiStep3Fragment;

public class ReservasiFragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private Button btnKembali, btnLanjut, btnBuatReservasi;
    private ProgressBar progressBar;

    // HAPUS: private int currentStep = 1; (Kita tidak pakai ini lagi)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservasi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom nav when this fragment is created
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(false);
        }

        viewModel = new ViewModelProvider(this).get(ReservasiSharedViewModel.class);
        btnKembali = view.findViewById(R.id.btn_kembali);
        btnLanjut = view.findViewById(R.id.btn_lanjut);
        btnBuatReservasi = view.findViewById(R.id.btn_buat_reservasi);
        progressBar = view.findViewById(R.id.progress_bar);

        // Tampilkan Langkah 1 saat pertama kali dibuka
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ReservasiStep1Fragment())
                    // JANGAN addToBackStack untuk langkah pertama
                    .commit();
        }

        // PERBAIKAN: Gunakan BackStackEntryCount sebagai 'currentStep'
        // Ini adalah "Sumber Kebenaran"
        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            // Panggil updateButtonVisibility setiap kali back stack berubah
            updateButtonVisibility();
        });

        // Panggil sekali saat awal
        updateButtonVisibility();

        setupButtonListeners();

        // Amati status validasi dari Langkah 1
        viewModel.isStep1Valid.observe(getViewLifecycleOwner(), isValid -> {
            // Hanya aktifkan tombol Lanjut jika kita di Langkah 1
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                btnLanjut.setEnabled(isValid);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show bottom nav when this fragment is destroyed
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(true);
        }
    }

    private void setupButtonListeners() {
        btnLanjut.setOnClickListener(v -> {
            // Dapatkan step saat ini dari back stack
            int currentStep = getChildFragmentManager().getBackStackEntryCount();

            if (currentStep == 0) { // Kita di Langkah 1
                // Pindah ke Langkah 2
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ReservasiStep2Fragment())
                        .addToBackStack("step2") // Beri nama back stack
                        .commit();

            } else if (currentStep == 1) { // Kita di Langkah 2
                // Validasi dulu data di Langkah 2
                Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_container);
                // INI PERBAIKANNYA: Cek tipe fragment sebelum di-cast
                if (currentFragment instanceof ReservasiStep2Fragment) {
                    ReservasiStep2Fragment step2 = (ReservasiStep2Fragment) currentFragment;

                    if (step2.validateAndSaveData()) {
                        // Jika valid, baru pindah ke Langkah 3
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ReservasiStep3Fragment())
                                .addToBackStack("step3")
                                .commit();
                    } else {
                        // Jika tidak valid, jangan pindah
                        Toast.makeText(getContext(), "Harap lengkapi semua data", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnKembali.setOnClickListener(v -> {
            // Cukup panggil popBackStack, listener akan urus sisanya
            getChildFragmentManager().popBackStack();
        });

        // Listener untuk btnBuatReservasi ada di ReservasiStep3Fragment
    }

    private void updateButtonVisibility() {
        // Dapatkan step saat ini dari back stack
        int currentStep = getChildFragmentManager().getBackStackEntryCount();

        btnKembali.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);
        btnLanjut.setVisibility(currentStep < 2 ? View.VISIBLE : View.GONE);
        btnBuatReservasi.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);

        // Atur status enabled tombol Lanjut
        if (currentStep == 0) {
            // Di Langkah 1, 'enabled' diatur oleh LiveData
            btnLanjut.setEnabled(viewModel.isStep1Valid.getValue());
        } else if (currentStep == 1) {
            // Di Langkah 2, selalu 'enabled' (validasi saat diklik)
            btnLanjut.setEnabled(true);
        }
    }

    public void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnBuatReservasi.setEnabled(!show);
        btnKembali.setEnabled(!show);
    }
}