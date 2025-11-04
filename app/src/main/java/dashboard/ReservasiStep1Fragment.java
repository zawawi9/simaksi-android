package dashboard; // Sesuaikan package Anda

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.KuotaHarian;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReservasiStep1Fragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private TextInputEditText etTanggalMasuk, etTanggalKeluar, etJumlahPendaki;
    private TextView tvStatusKuota;
    private long tanggalMasukMillis = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_step1_tanggal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hubungkan ke ViewModel Induk
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservasiSharedViewModel.class);

        // Binding
        etTanggalMasuk = view.findViewById(R.id.et_tanggal_masuk);
        etTanggalKeluar = view.findViewById(R.id.et_tanggal_keluar);
        etJumlahPendaki = view.findViewById(R.id.et_jumlah_pendaki);
        tvStatusKuota = view.findViewById(R.id.tv_status_kuota);

        setupDatePickerListeners();
        setupTextWatchers();
    }

    private void setupDatePickerListeners() {
        // 1. Tanggal Masuk (Validasi H-7)
        etTanggalMasuk.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_YEAR, 7);
            long H_7_dari_sekarang = cal.getTimeInMillis();

            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setStart(H_7_dari_sekarang)
                    .setValidator(DateValidatorPointForward.from(H_7_dari_sekarang))
                    .build();

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.reservasi_tanggal_masuk_hint)
                    .setCalendarConstraints(constraints)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                tanggalMasukMillis = selection;
                String tanggalFormatted = formatDate(selection);
                etTanggalMasuk.setText(tanggalFormatted);
                etTanggalKeluar.setText(""); // Reset tanggal keluar
                viewModel.tanggalMasuk.setValue(tanggalFormatted); // Simpan ke ViewModel
                triggerKuotaCheck();
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_MASUK");
        });

        // 2. Tanggal Keluar
        etTanggalKeluar.setOnClickListener(v -> {
            if (tanggalMasukMillis == 0) {
                Toast.makeText(getContext(), "Pilih tanggal masuk terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Batasi tanggal keluar HANYA BISA setelah tanggal masuk
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setStart(tanggalMasukMillis)
                    .setValidator(DateValidatorPointForward.from(tanggalMasukMillis))
                    .build();

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.reservasi_tanggal_keluar_hint)
                    .setCalendarConstraints(constraints)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                String tanggalFormatted = formatDate(selection);
                etTanggalKeluar.setText(tanggalFormatted);
                viewModel.tanggalKeluar.setValue(tanggalFormatted); // Simpan ke ViewModel
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_KELUAR");
        });
    }

    private void setupTextWatchers() {
        TextWatcher kuotaWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                triggerKuotaCheck();
            }
        };
        etJumlahPendaki.addTextChangedListener(kuotaWatcher);
    }

    private void triggerKuotaCheck() {
        String tanggal = etTanggalMasuk.getText().toString();
        String jumlahStr = etJumlahPendaki.getText().toString();

        viewModel.setStep1Valid(false); // Selalu set tidak valid saat pengecekan

        if (tanggal.isEmpty() || jumlahStr.isEmpty()) {
            tvStatusKuota.setText(R.string.reservasi_status_kuota_default);
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);
        if (jumlah <= 0 || jumlah > 10) {
            tvStatusKuota.setText("Jumlah pendaki harus antara 1-10.");
            return;
        }

        viewModel.jumlahPendaki.setValue(jumlah); // Simpan ke ViewModel
        tvStatusKuota.setText("Mengecek kuota...");

        // INI DIA KONEKSI DATABASE-NYA
        SupabaseAuth.cekKuota(tanggal, new SupabaseAuth.KuotaCallback() {
            @Override
            public void onSuccess(KuotaHarian kuota) {
                int sisaKuota = kuota.getKuotaMaksimal() - kuota.getKuotaTerpesan();
                if (sisaKuota >= jumlah) {
                    tvStatusKuota.setText("Kuota tersedia! Sisa: " + sisaKuota);
                    tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    viewModel.setStep1Valid(true); // <-- PENTING: Izinkan lanjut

                    // Hitung harga (simulasi, ambil dari pengaturan_biaya nanti)
                    int hargaTiket = 20000;
                    viewModel.totalHarga.setValue(hargaTiket * jumlah);

                } else {
                    tvStatusKuota.setText("Kuota tidak cukup. Sisa kuota: " + sisaKuota);
                    tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
            @Override
            public void onError(String errorMessage) {
                tvStatusKuota.setText(errorMessage);
                tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}