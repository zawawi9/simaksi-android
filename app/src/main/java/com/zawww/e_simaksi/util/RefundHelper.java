package com.zawww.e_simaksi.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RefundHelper {

    public static class RefundResult {
        public int persentase;
        public long nominal;
        public boolean isRefundable;
        public String pesan;

        public RefundResult(int p, long n, boolean r, String msg) {
            this.persentase = p;
            this.nominal = n;
            this.isRefundable = r;
            this.pesan = msg;
        }
    }

    public static RefundResult hitungRefund(String tanggalPendakian, long totalBayar) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                LocalDate tglDaki = LocalDate.parse(tanggalPendakian);
                LocalDate hariIni = LocalDate.now();

                // Hitung selisih hari
                long selisihHari = ChronoUnit.DAYS.between(hariIni, tglDaki);

                int persentase;
                String pesan;
                boolean bisaRefund = true;

                if (selisihHari >= 6) {
                    persentase = 100;
                    pesan = "Pengembalian Dana Penuh (100%)";
                } else if (selisihHari >= 4) {
                    persentase = 75;
                    pesan = "Pengembalian Dana 75% (Pembatalan H-4/H-5)";
                } else if (selisihHari >= 2) {
                    persentase = 50;
                    pesan = "Pengembalian Dana 50% (Pembatalan H-2/H-3)";
                } else if (selisihHari == 1) {
                    persentase = 25;
                    pesan = "Pengembalian Dana 25% (Pembatalan H-1)";
                } else {
                    persentase = 0;
                    pesan = "Pembatalan pada Hari-H tidak dapat direfund.";
                    bisaRefund = false;
                }

                long nominal = (totalBayar * persentase) / 100;
                return new RefundResult(persentase, nominal, bisaRefund, pesan);

            } catch (Exception e) {
                // Fallback kalau tanggal error
                return new RefundResult(0, 0, false, "Format tanggal error");
            }
        }
        return new RefundResult(0, 0, false, "Versi Android tidak mendukung");
    }
}