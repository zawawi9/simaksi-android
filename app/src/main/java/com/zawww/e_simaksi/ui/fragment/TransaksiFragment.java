package com.zawww.e_simaksi.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Pastikan library Glide sudah ada
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.adapter.TransaksiAdapter;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.Reservasi;
import com.zawww.e_simaksi.util.DateUtil;
import com.zawww.e_simaksi.util.RefundHelper;
import com.zawww.e_simaksi.util.SessionManager;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.zawww.e_simaksi.util.ErrorHandler;



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

                        ErrorHandler.showError(requireView(), "Gagal memuat riwayat: " + errorMessage);

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

            String statusToFilter;

            switch (selectedStatus.toLowerCase()) {

                case "menunggu pembayaran": statusToFilter = "menunggu_pembayaran"; break;

                case "terkonfirmasi": statusToFilter = "terkonfirmasi"; break;

                case "dibatalkan": statusToFilter = "dibatalkan"; break;

                case "selesai": statusToFilter = "selesai"; break;

                default: statusToFilter = selectedStatus.toLowerCase(); break;

            }

            final String finalStatus = statusToFilter;

            filteredList = filteredList.stream()

                    .filter(r -> r.getStatus().toLowerCase().equals(finalStatus))

                    .collect(Collectors.toList());

        }



        // Filter by Date

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (tglMulaiCalendar != null) {

            filteredList = filteredList.stream().filter(r -> {

                try {

                    Date tglReservasi = sdf.parse(r.getTanggalPendakian());

                    return tglReservasi != null && !tglReservasi.before(tglMulaiCalendar.getTime());

                } catch (ParseException e) { return false; }

            }).collect(Collectors.toList());

        }

        if (tglAkhirCalendar != null) {

            filteredList = filteredList.stream().filter(r -> {

                try {

                    Date tglReservasi = sdf.parse(r.getTanggalPendakian());

                    return tglReservasi != null && !tglReservasi.after(tglAkhirCalendar.getTime());

                } catch (ParseException e) { return false; }

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



    @Override

    public void onBayarClick(Reservasi reservasi) {

        ErrorHandler.showError(requireView(), "Mengarahkan ke halaman pembayaran...");

        SupabaseAuth.getSnapToken(reservasi.getKodeReservasi(), reservasi.getTotalHarga(), new SupabaseAuth.TokenCallback() {

            @Override

            public void onSuccess(String snapToken, String redirectUrl) {

                if (redirectUrl != null && !redirectUrl.isEmpty()) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));

                    startActivity(intent);

                } else {

                    ErrorHandler.showError(requireView(), "Gagal mendapatkan link pembayaran.");

                }

            }



            @Override

            public void onError(String error) {

                ErrorHandler.showError(requireView(), "Gagal memulai pembayaran: " + error);

            }

        });

    }



    private void panggilDetailReservasi(long idReservasi) {

        if (idReservasi == -1L || getContext() == null) return;

        ErrorHandler.showError(requireView(), "Memuat detail...");



        // Casting ke int karena SupabaseAuth.getDetailReservasi kemungkinan minta int

        // Kalau method di SupabaseAuth sudah kamu ubah jadi long, hapus (int)-nya

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

                    getActivity().runOnUiThread(() -> ErrorHandler.showError(requireView(), errorMessage));

                }

            }

        });

    }



    // =========================================================================

    //  METHOD MENAMPILKAN DETAIL + LOGIC REFUND

    // =========================================================================

        private void showDetailDialog(Reservasi reservasi) {

            if (getContext() == null) return;

    

            final Dialog dialog = new Dialog(getContext());

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setContentView(R.layout.dialog_detail_reservasi);

    

            if (dialog.getWindow() != null) {

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            }

    

            // Init Components

            ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);

            TextView tvKode = dialog.findViewById(R.id.tv_detail_kode);

            TextView tvTanggal = dialog.findViewById(R.id.tv_detail_tanggal);

            TextView tvDipesan = dialog.findViewById(R.id.tv_detail_dipesan_pada);

    

            // Status Reservasi

            TextView tvStatus = dialog.findViewById(R.id.tv_detail_status);

            CardView cvStatus = (CardView) tvStatus.getParent().getParent(); // Get parent CardView

    

            // Status Sampah

            TextView tvStatusSampah = dialog.findViewById(R.id.tv_detail_status_sampah);

            CardView cvStatusSampah = (CardView) tvStatusSampah.getParent().getParent(); // Get parent CardView

    

            LinearLayout llPendaki = dialog.findViewById(R.id.ll_container_pendaki);

            LinearLayout llSampah = dialog.findViewById(R.id.ll_container_sampah);

            TextView tvJmlPendaki = dialog.findViewById(R.id.tv_detail_jumlah_pendaki);

            TextView tvTiketParkir = dialog.findViewById(R.id.tv_detail_tiket_parkir);

            TextView tvTotalHarga = dialog.findViewById(R.id.tv_detail_total_harga);

    

            // Komponen Refund UI

            LinearLayout layoutInfoRefund = dialog.findViewById(R.id.layout_info_refund);

            TextView tvStatusRefundText = dialog.findViewById(R.id.tv_status_refund_text);

            TextView tvNominalRefundInfo = dialog.findViewById(R.id.tv_nominal_refund_info);

    

            LinearLayout layoutBuktiRefund = dialog.findViewById(R.id.layout_bukti_refund);

            ImageView imgBuktiRefund = dialog.findViewById(R.id.img_bukti_refund);

            MaterialButton btnLihatBuktiFull = dialog.findViewById(R.id.btn_lihat_bukti_full);

            MaterialButton btnBatalkan = dialog.findViewById(R.id.btn_batalkan_pesanan);

    

            // --- SET DATA ---

            tvKode.setText(reservasi.getKodeReservasi());

    

            // Format tanggal

            String tanggalPendakianFormatted = reservasi.getTanggalPendakian();

            try {

                SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date date = inputSdf.parse(reservasi.getTanggalPendakian());

                if (date != null) {

                    SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

                    tanggalPendakianFormatted = outputSdf.format(date);

                }

            } catch (ParseException e) { e.printStackTrace(); }

            tvTanggal.setText(tanggalPendakianFormatted);

    

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                tvDipesan.setText(DateUtil.formatDate(reservasi.getDipesanPada()));

            } else {

                tvDipesan.setText(reservasi.getDipesanPada());

            }

    

            applyReservationStatusStyling(reservasi.getStatus(), tvStatus, cvStatus);

            applySampahStatusStyling(reservasi.getStatusSampah(), tvStatusSampah, cvStatusSampah);

    

            tvJmlPendaki.setText(reservasi.getJumlahPendaki() + " orang");

    

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

            String hargaParkir = currencyFormat.format(reservasi.getJumlahTiketParkir() * 5000L);

            tvTiketParkir.setText(hargaParkir.replace("Rp", "Rp "));

    

            String totalHarga = currencyFormat.format(reservasi.getTotalHarga());

            tvTotalHarga.setText(totalHarga.replace("Rp", "Rp "));

    

            // Load Pendaki & Barang

            setupPendakiAndBarang(reservasi, llPendaki, llSampah);

    

            // --- LOGIC REFUND VISIBILITY ---

            String status = reservasi.getStatus();

            layoutInfoRefund.setVisibility(View.GONE);

            layoutBuktiRefund.setVisibility(View.GONE);

            btnBatalkan.setVisibility(View.VISIBLE);

    

            if (status.equals("pengajuan_refund")) {

                btnBatalkan.setVisibility(View.GONE);

                layoutInfoRefund.setVisibility(View.VISIBLE);

                tvStatusRefundText.setText("MENUNGGU PROSES REFUND");

                String nominal = currencyFormat.format(reservasi.getNominalRefund());

                tvNominalRefundInfo.setText("Estimasi Pengembalian: " + nominal.replace("Rp", "Rp "));

                layoutInfoRefund.setBackgroundColor(requireContext().getColor(R.color.status_pengajuan_refund)); // Set background from colors.xml

                tvStatusRefundText.setTextColor(requireContext().getColor(R.color.white)); // Set text color for contrast

    

            } else if (status.equals("refund_selesai")) {

                btnBatalkan.setVisibility(View.GONE);

                layoutInfoRefund.setVisibility(View.VISIBLE);

                tvStatusRefundText.setText("DANA DIKEMBALIKAN");

                tvNominalRefundInfo.setText("Silakan cek rekening Anda.");

                layoutInfoRefund.setBackgroundColor(requireContext().getColor(R.color.status_refund_selesai)); // Set background from colors.xml

                tvStatusRefundText.setTextColor(requireContext().getColor(R.color.white)); // Set text color for contrast

    

                if (reservasi.getBuktiRefund() != null && !reservasi.getBuktiRefund().isEmpty()) {

                    layoutBuktiRefund.setVisibility(View.VISIBLE);

                    Glide.with(this)

                            .load(reservasi.getBuktiRefund())

                            .centerCrop()

                            .placeholder(android.R.color.darker_gray)

                            .into(imgBuktiRefund);

    

                    btnLihatBuktiFull.setOnClickListener(v -> {

                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setDataAndType(Uri.parse(reservasi.getBuktiRefund()), "image/*");

                        startActivity(intent);

                    });

                }

            } else if (status.equals("dibatalkan") || status.equals("terkonfirmasi")) {

                btnBatalkan.setVisibility(View.GONE);

            }

    

            btnBatalkan.setOnClickListener(v -> {

                dialog.dismiss();

                handleTombolBatal(reservasi);

            });

    

            btnClose.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

        }

    

        // Helper method to apply styling for reservation status

        private void applyReservationStatusStyling(String status, TextView textView, CardView cardView) {

            String displayText = status.replace("_", " ").toUpperCase();

            textView.setText(displayText);

    

            int backgroundColorRes;

            int textColorRes = R.color.white; // Default text color for badges

    

            switch (status.toLowerCase()) {

                case "menunggu_pembayaran":

                    backgroundColorRes = R.color.status_menunggu_pembayaran;

                    break;

                case "terkonfirmasi":

                    backgroundColorRes = R.color.status_terkonfirmasi;

                    break;

                case "dibatalkan":

                    backgroundColorRes = R.color.status_dibatalkan;

                    break;

                case "pengajuan_refund":

                    backgroundColorRes = R.color.status_pengajuan_refund;

                    break;

                case "refund_selesai":

                    backgroundColorRes = R.color.status_refund_selesai;

                    break;

                default:

                    backgroundColorRes = R.color.black; // Fallback background color

                    break;

            }

    

            cardView.setCardBackgroundColor(requireContext().getColor(backgroundColorRes));

            textView.setTextColor(requireContext().getColor(textColorRes));

        }

    

            // Helper method to apply styling for sampah status

    

            private void applySampahStatusStyling(String statusSampah, TextView textView, CardView cardView) {

    

                String displayText = statusSampah.replace("_", " ").toUpperCase();

    

                textView.setText(displayText);

    

        

    

                int backgroundColorRes;

    

                int textColorRes = R.color.white; // Default text color for badges

    

        

    

                switch (statusSampah.toLowerCase()) {

    

                    case "sesuai":

    

                        backgroundColorRes = R.color.status_sampah_sesuai;

    

                        break;

    

                    case "tidak sesuai":

    

                        backgroundColorRes = R.color.status_sampah_tidak_sesuai;

    

                        break;

    

                    default:

    

                        backgroundColorRes = R.color.black; // Fallback background color

    

                        break;

    

                }

    

        

    

                cardView.setCardBackgroundColor(requireContext().getColor(backgroundColorRes));

    

                textView.setTextColor(requireContext().getColor(textColorRes));

    

            }



    private void setupPendakiAndBarang(Reservasi reservasi, LinearLayout llPendaki, LinearLayout llSampah) {

        llPendaki.removeAllViews();

        if (reservasi.getPendakiRombongan() != null && !reservasi.getPendakiRombongan().isEmpty()) {

            for (PendakiRombongan pendaki : reservasi.getPendakiRombongan()) {

                TextView tv = new TextView(getContext());

                tv.setText("\u2022 " + pendaki.getNamaLengkap() + " (" + pendaki.getNik() + ")");

                llPendaki.addView(tv);

            }

        } else {

            TextView tv = new TextView(getContext());

            tv.setText("-"); llPendaki.addView(tv);

        }



        llSampah.removeAllViews();

        if (reservasi.getBarangBawaanSampah() != null && !reservasi.getBarangBawaanSampah().isEmpty()) {

            for (BarangBawaanSampah barang : reservasi.getBarangBawaanSampah()) {

                TextView tv = new TextView(getContext());

                tv.setText("\u2022 " + barang.getNamaBarang() + " (" + barang.getJumlah() + " buah)");

                llSampah.addView(tv);

            }

        } else {

            TextView tv = new TextView(getContext());

            tv.setText("-"); llSampah.addView(tv);

        }

    }



    // =========================================================================

    //  METHOD 1: LOGIC PENGECEKAN H-SEKIAN

    // =========================================================================

    private void handleTombolBatal(Reservasi data) {

        if (data.getStatus().equals("menunggu_pembayaran")) {

            // Kalau belum bayar, kamu bisa tambahkan API khusus pembatalan langsung

            ErrorHandler.showError(requireView(), "Silakan tunggu timeout sistem.");

            return;

        }



        RefundHelper.RefundResult hasil = RefundHelper.hitungRefund(data.getTanggalPendakian(), data.getTotalHarga());



        if (!hasil.isRefundable) {

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())

                    .setTitle("Tidak Dapat Refund")

                    .setMessage(hasil.pesan)

                    .setPositiveButton("Mengerti", null)

                    .show();

            return;

        }



        showDialogAjukanRefund(data.getIdReservasi(), hasil);

    }



    // =========================================================================

    //  METHOD 2: TAMPILKAN DIALOG FORM (FIXED: long idReservasi)

    // =========================================================================

    private void showDialogAjukanRefund(long idReservasi, RefundHelper.RefundResult hasil) {

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ajukan_refund, null);



        TextView tvEstimasi = view.findViewById(R.id.tv_estimasi_refund);

        TextView tvPersen = view.findViewById(R.id.tv_persentase_refund);

        TextInputEditText etAlasan = view.findViewById(R.id.et_alasan);

        TextInputEditText etBank = view.findViewById(R.id.et_bank);

        TextInputEditText etRek = view.findViewById(R.id.et_rekening);

        TextInputEditText etNama = view.findViewById(R.id.et_atas_nama);

        Button btnBatal = view.findViewById(R.id.btn_batal_dialog);

        Button btnKirim = view.findViewById(R.id.btn_kirim_refund);



        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        String nominalStr = currencyFormat.format(hasil.nominal).replace("Rp", "Rp ");

        tvEstimasi.setText("Estimasi Refund: " + nominalStr);

        tvPersen.setText(hasil.pesan);



        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();



        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        }



        btnBatal.setOnClickListener(v -> dialog.dismiss());



        btnKirim.setOnClickListener(v -> {

            String alasan = etAlasan.getText().toString();

            String bank = etBank.getText().toString();

            String rek = etRek.getText().toString();

            String nama = etNama.getText().toString();



            if(alasan.isEmpty() || bank.isEmpty() || rek.isEmpty() || nama.isEmpty()){

                ErrorHandler.showError(requireView(), "Mohon lengkapi semua data!");

                return;

            }



            // Kirim ke API (Passing ID sebagai long)

            sendRefundRequest(idReservasi, alasan, bank, rek, nama, hasil.persentase, hasil.nominal, dialog);

        });



        dialog.show();

    }

    // =========================================================================

    //  METHOD 3: KIRIM KE SUPABASE (FIXED: long idReservasi & casting)

    // =========================================================================

    private void sendRefundRequest(long idReservasi, String alasan, String bank, String rek, String nama, int persentase, long nominal, androidx.appcompat.app.AlertDialog dialog) {

        // Casting (int) disini aman jika SupabaseAuth masih pakai int.

        // Jika SupabaseAuth sudah diupdate jadi long, hapus (int)-nya.

        SupabaseAuth.ajukanRefund((int) idReservasi, alasan, bank, rek, nama, persentase, nominal, new SupabaseAuth.GeneralCallback() {

            @Override

            public void onSuccess() {

                Toast.makeText(requireContext(), "Permintaan Refund Terkirim!", Toast.LENGTH_LONG).show();

                dialog.dismiss();

                loadTransaksiData();

            }



            @Override

            public void onError(String errorMessage) {

                ErrorHandler.showError(requireView(), "Gagal: " + errorMessage);

            }

        });

    }

}
