package com.zawww.e_simaksi.ui.fragment;

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
import com.zawww.e_simaksi.viewmodel.ReservasiSharedViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.api.SupabaseAuth;
import com.zawww.e_simaksi.model.KuotaHarian;
import com.zawww.e_simaksi.model.PengaturanBiaya; // <-- LENGKAPI: Impor model baru

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List; // <-- LENGKAPI: Impor List
import java.util.Locale;
import java.util.TimeZone;

import android.os.Parcel;
import android.os.Parcelable;

public class ReservasiStep1Fragment extends Fragment {

    private ReservasiSharedViewModel viewModel;
    private TextInputEditText etTanggalMasuk, etTanggalKeluar, etJumlahPendaki, etJumlahParkir;
    private TextView tvStatusKuota, tvHargaTiket, tvHargaParkir;
    private long tanggalMasukMillis = 0;

    // Ini adalah nilai default JIKA internet gagal
    private int hargaTiketDb = 20000;
    private int hargaParkirDb = 5000;

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
        etJumlahParkir = view.findViewById(R.id.et_jumlah_parkir);
        tvStatusKuota = view.findViewById(R.id.tv_status_kuota);
        tvHargaTiket = view.findViewById(R.id.tv_harga_tiket);
        tvHargaParkir = view.findViewById(R.id.tv_harga_parkir);

        setupDatePickerListeners();
        setupTextWatchers();
        loadHargaDariDatabase(); // Panggil method yang mengambil harga
    }

    // <-- LENGKAPI: Method ini hilang di kode Anda
    private void loadHargaDariDatabase() {
        SupabaseAuth.getPengaturanBiaya(new SupabaseAuth.BiayaCallback() {
            @Override
            public void onSuccess(List<PengaturanBiaya> biayaList) {
                // Loop harga dan simpan ke variabel
                for (PengaturanBiaya item : biayaList) {
                    if (item.getNamaItem().equals("tiket_masuk")) {
                        hargaTiketDb = item.getHarga();
                    } else if (item.getNamaItem().equals("tiket_parkir")) {
                        hargaParkirDb = item.getHarga();
                    }
                }
                
                // Format harga ke Rupiah
                Locale localeID = new Locale("in", "ID");
                NumberFormat formatRp = NumberFormat.getCurrencyInstance(localeID);
                formatRp.setMaximumFractionDigits(0);

                // Tampilkan harga di UI
                tvHargaTiket.setText(formatRp.format(hargaTiketDb));
                tvHargaParkir.setText(formatRp.format(hargaParkirDb));

                Log.d("Step1", "Harga berhasil dimuat: Tiket=" + hargaTiketDb + ", Parkir=" + hargaParkirDb);
                // Hitung ulang jika data sudah terisi
                triggerKuotaCheck();
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("Step1", "Gagal load harga: " + errorMessage + ". Pakai harga default.");
                // Biarkan pakai harga default (20000 & 5000)
            }
        });
    }

    private void setupDatePickerListeners() {
        // 1. Tanggal Masuk (Validasi maks H+7)
        etTanggalMasuk.setOnClickListener(v -> {
            // Bekerja dengan UTC untuk konsistensi MaterialDatePicker
            Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            // Set ke tengah malam hari ini di UTC
            utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
            utcCalendar.set(Calendar.MINUTE, 0);
            utcCalendar.set(Calendar.SECOND, 0);
            utcCalendar.set(Calendar.MILLISECOND, 0);
            long todayUtcMidnight = utcCalendar.getTimeInMillis();

            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(new RangeDateValidator(todayUtcMidnight));

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.reservasi_tanggal_masuk_hint)
                    .setSelection(todayUtcMidnight) // Set pilihan default ke hari ini
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                tanggalMasukMillis = selection;
                String tanggalFormatted = formatDate(selection);
                etTanggalMasuk.setText(tanggalFormatted);
                etTanggalKeluar.setText(""); // Reset tanggal keluar
                viewModel.tanggalMasuk.setValue(tanggalFormatted); // Simpan ke ViewModel
                viewModel.tanggalKeluar.setValue(null);
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

    public static class RangeDateValidator implements CalendarConstraints.DateValidator {
        private final long minDate;
        private final long maxDate;

        public RangeDateValidator(long minDate) {
            this.minDate = minDate;
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(minDate);
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            this.maxDate = calendar.getTimeInMillis();
        }

        @Override
        public boolean isValid(long date) {
            return date >= minDate && date <= maxDate;
        }

        // Parcelable implementation
        public static final Parcelable.Creator<RangeDateValidator> CREATOR = new Parcelable.Creator<RangeDateValidator>() {
            @Override
            public RangeDateValidator createFromParcel(Parcel source) {
                long minDate = source.readLong();
                return new RangeDateValidator(minDate);
            }

            @Override
            public RangeDateValidator[] newArray(int size) {
                return new RangeDateValidator[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(minDate);
        }
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
        etJumlahParkir.addTextChangedListener(kuotaWatcher);
    }

    private void triggerKuotaCheck() {
        String tanggal = etTanggalMasuk.getText().toString();
        String jumlahStr = etJumlahPendaki.getText().toString();
        String parkirStr = etJumlahParkir.getText().toString();

        viewModel.setStep1Valid(false); // Selalu set tidak valid saat pengecekan

        if (parkirStr.isEmpty()) {
            parkirStr = "0";
        }

        if (tanggal.isEmpty() || jumlahStr.isEmpty()) {
            tvStatusKuota.setText(R.string.reservasi_status_kuota_default);
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);
        int parkir = Integer.parseInt(parkirStr);

        if (jumlah <= 0 || jumlah > 10) {
            tvStatusKuota.setText("Jumlah pendaki harus antara 1-10.");
            return;
        }

        viewModel.jumlahPendaki.setValue(jumlah); // Simpan ke ViewModel
        viewModel.jumlahParkir.setValue(parkir); // <-- LENGKAPI: Simpan jumlah parkir
        tvStatusKuota.setText("Mengecek kuota...");

        // INI DIA KONEKSI DATABASE-NYA
        SupabaseAuth.cekKuota(tanggal, new SupabaseAuth.KuotaCallback() {
            @Override
            public void onSuccess(KuotaHarian kuota) {
                int sisaKuota = kuota.getKuotaMaksimal() - kuota.getKuotaTerpesan();
                if (sisaKuota >= jumlah) {
                    tvStatusKuota.setText("Kuota tersedia! Sisa: ".concat(String.valueOf(sisaKuota))); // Concat agar aman
                    tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    viewModel.setStep1Valid(true); // <-- PENTING: Izinkan lanjut

                    // <-- PERBAIKAN: Gunakan variabel yang benar dan hitung parkir
                    viewModel.totalHarga.setValue((hargaTiketDb * jumlah) + (hargaParkirDb * parkir));

                } else {
                    tvStatusKuota.setText("Kuota tidak cukup. Sisa kuota: ".concat(String.valueOf(sisaKuota)));
                    tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
            @Override
            public void onError(String errorMessage) {
                tvStatusKuota.setText(errorMessage);
                tvStatusKuota.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }

    // <-- PERBAIKAN: Pindahkan method ini ke DALAM class
    private String formatDate(long millis) {
        // Penting: SimpleDateFormat harus di set ke UTC agar tidak menggeser tanggal
        // karena MaterialDatePicker mengembalikan milidetik dalam UTC.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }
}