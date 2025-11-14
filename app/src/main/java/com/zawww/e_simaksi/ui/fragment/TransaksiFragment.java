package com.zawww.e_simaksi.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zawww.e_simaksi.util.SessionManager;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.adapter.TransaksiAdapter;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Reservasi;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TransaksiFragment extends Fragment implements TransaksiAdapter.OnTransaksiClickListener {

    private RecyclerView recyclerView;
    private TransaksiAdapter adapter;
    private SessionManager sessionManager;
    private LinearLayout emptyStateLayout;

    private List<Reservasi> originalList = new ArrayList<>();
    private ChipGroup chipGroupStatus;
    private Button btnTglMulai, btnTglAkhir;

    private Calendar tglMulaiCalendar, tglAkhirCalendar;
    private String selectedStatus = "Semua";

    public TransaksiFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaksi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        recyclerView = view.findViewById(R.id.recycler_transaksi);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        btnTglMulai = view.findViewById(R.id.btn_tgl_mulai);
        btnTglAkhir = view.findViewById(R.id.btn_tgl_akhir);
        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();
        setupFilters();
        loadTransaksiData();
    }


    private void setupRecyclerView() {
        adapter = new TransaksiAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                selectedStatus = chip.getText().toString();
                applyFilters();
            }
        });

        btnTglMulai.setOnClickListener(v -> showDatePickerDialog(true));
        btnTglAkhir.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(boolean isTglMulai) {
        if (getContext() == null) return;
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            if (isTglMulai) {
                tglMulaiCalendar = selectedDate;
                btnTglMulai.setText(sdf.format(tglMulaiCalendar.getTime()));
            } else {
                tglAkhirCalendar = selectedDate;
                btnTglAkhir.setText(sdf.format(tglAkhirCalendar.getTime()));
            }
            applyFilters();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadTransaksiData() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showEmptyState(true);
            return;
        }

        SupabaseAuth.getReservasiHistory(userId, new SupabaseAuth.ReservasiCallback() {
            @Override
            public void onSuccess(List<Reservasi> reservasiList) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        originalList.clear();
                        if (reservasiList != null) {
                            originalList.addAll(reservasiList);
                        }
                        applyFilters();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Gagal memuat riwayat: " + errorMessage, Toast.LENGTH_SHORT).show();
                        applyFilters(); // Show empty state
                    });
                }
            }
        });
    }

    private void applyFilters() {
        List<Reservasi> filteredList = new ArrayList<>(originalList);

        // Filter by Status
        if (!selectedStatus.equalsIgnoreCase("Semua")) {
            // Adjust status mapping if necessary
            String statusToFilter = selectedStatus;
            if (selectedStatus.equalsIgnoreCase("Berhasil")) {
                statusToFilter = "terkonfirmasi";
            } else if (selectedStatus.equalsIgnoreCase("Gagal")) {
                statusToFilter = "dibatalkan";
            }
            String finalStatus = statusToFilter.toLowerCase();
            filteredList = filteredList.stream()
                    .filter(r -> r.getStatus().equalsIgnoreCase(finalStatus))
                    .collect(Collectors.toList());
        }

        // Filter by Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (tglMulaiCalendar != null) {
            filteredList = filteredList.stream().filter(r -> {
                try {
                    Date tglReservasi = sdf.parse(r.getTanggalPendakian());
                    return tglReservasi != null && !tglReservasi.before(tglMulaiCalendar.getTime());
                } catch (ParseException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
        if (tglAkhirCalendar != null) {
            filteredList = filteredList.stream().filter(r -> {
                try {
                    Date tglReservasi = sdf.parse(r.getTanggalPendakian());
                    return tglReservasi != null && !tglReservasi.after(tglAkhirCalendar.getTime());
                } catch (ParseException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        adapter.updateData(filteredList);
        showEmptyState(filteredList.isEmpty());
    }

    private void showEmptyState(boolean show) {
        if (emptyStateLayout != null && recyclerView != null) {
            emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDetailClick(Reservasi reservasi) {
        panggilDetailReservasi(reservasi.getIdReservasi());
    }

    private void panggilDetailReservasi(long idReservasi) {
        if (idReservasi == -1L || getContext() == null) return;
        Toast.makeText(getContext(), "Memuat detail...", Toast.LENGTH_SHORT).show();

        SupabaseAuth.getDetailReservasi((int) idReservasi, new SupabaseAuth.DetailReservasiCallback() {
            @Override
            public void onSuccess(Reservasi reservasi) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> showDetailDialog(reservasi));
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void showDetailDialog(Reservasi reservasi) {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detail_reservasi);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        TextView tvKode = dialog.findViewById(R.id.tv_detail_kode);
        TextView tvTanggal = dialog.findViewById(R.id.tv_detail_tanggal);
        TextView tvDipesan = dialog.findViewById(R.id.tv_detail_dipesan_pada);
        TextView tvStatus = dialog.findViewById(R.id.tv_detail_status);
        TextView tvStatusSampah = dialog.findViewById(R.id.tv_detail_status_sampah);
        LinearLayout llPendaki = dialog.findViewById(R.id.ll_container_pendaki);
        LinearLayout llSampah = dialog.findViewById(R.id.ll_container_sampah);
        TextView tvJmlPendaki = dialog.findViewById(R.id.tv_detail_jumlah_pendaki);
        TextView tvTiketParkir = dialog.findViewById(R.id.tv_detail_tiket_parkir);
        TextView tvTotalHarga = dialog.findViewById(R.id.tv_detail_total_harga);

        tvKode.setText("Kode: " + reservasi.getKodeReservasi());
        tvTanggal.setText("Tanggal Pendakian: " + reservasi.getTanggalPendakian());
        tvDipesan.setText("Dipesan: " + reservasi.getDipesanPada());
        tvStatus.setText("Status: " + reservasi.getStatus());
        tvStatusSampah.setText("Status Sampah: " + reservasi.getStatusSampah());

        tvJmlPendaki.setText(reservasi.getJumlahPendaki() + " orang");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String hargaParkir = currencyFormat.format(reservasi.getJumlahTiketParkir() * 2000L);
        tvTiketParkir.setText(hargaParkir.replace("Rp", "Rp "));

        String totalHarga = currencyFormat.format(reservasi.getTotalHarga());
        tvTotalHarga.setText(totalHarga.replace("Rp", "Rp "));

        llPendaki.removeAllViews();
        if (reservasi.getPendakiRombongan() != null && !reservasi.getPendakiRombongan().isEmpty()) {
            for (PendakiRombongan pendaki : reservasi.getPendakiRombongan()) {
                TextView tv = new TextView(getContext());
                tv.setText("\u2022 " + pendaki.getNamaLengkap() + " (" + pendaki.getNik() + ")");
                llPendaki.addView(tv);
            }
        } else {
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data rombongan tidak ditemukan.");
            llPendaki.addView(tvKosong);
        }

        llSampah.removeAllViews();
        if (reservasi.getBarangBawaanSampah() != null && !reservasi.getBarangBawaanSampah().isEmpty()) {
            for (BarangBawaanSampah barang : reservasi.getBarangBawaanSampah()) {
                TextView tv = new TextView(getContext());
                tv.setText("\u2022 " + barang.getNamaBarang() + " (" + barang.getJumlah() + " buah)");
                llSampah.addView(tv);
            }
        } else {
            TextView tvKosong = new TextView(getContext());
            tvKosong.setText("Data sampah tidak ditemukan.");
            llSampah.addView(tvKosong);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}