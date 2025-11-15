package com.zawww.e_simaksi.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.util.SessionManager;

public class UbahPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private Button btnKirimTautan;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ubah_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        initViews(view);
        setupListeners();

        // Set email from session
        etEmail.setText(sessionManager.getUserEmail());
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        btnKirimTautan = view.findViewById(R.id.btn_kirim_tautan);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        btnKirimTautan.setOnClickListener(v -> {
            sendPasswordResetLink();
        });
    }

    private void sendPasswordResetLink() {
        showLoading(true);
        String email = sessionManager.getUserEmail();

        // This method will be created in a later step
        SupabaseAuth.sendPasswordReset(email, new SupabaseAuth.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tautan Terkirim")
                .setMessage("Tautan untuk mengatur ulang kata sandi telah dikirim ke email Anda. Silakan periksa kotak masuk Anda.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    NavHostFragment.findNavController(UbahPasswordFragment.this).navigateUp();
                })
                .show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnKirimTautan.setEnabled(!isLoading);
    }
}
