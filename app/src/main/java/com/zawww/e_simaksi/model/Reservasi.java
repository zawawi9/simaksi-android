package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Reservasi {

    @SerializedName("id_reservasi")
    private long idReservasi; // BIGINT lebih aman pakai long

    @SerializedName("id_pengguna")
    private String idPengguna; // uuid

    @SerializedName("kode_reservasi")
    private String kodeReservasi;

    @SerializedName("tanggal_pendakian")
    private String tanggalPendakian; // date

    @SerializedName("jumlah_pendaki")
    private int jumlahPendaki; // smallint

    @SerializedName("jumlah_tiket_parkir")
    private int jumlahTiketParkir; // smallint

    @SerializedName("total_harga")
    private int totalHarga; // int

    @SerializedName("status") // Nama kolom di skema adalah 'status'
    private String status; // status_reservasi (enum)

    @SerializedName("jumlah_potensi_sampah")
    private int jumlahPotensiSampah; // smallint

    @SerializedName("status_sampah") // Kolom baru dari skema
    private String statusSampah; // status_sampah_enum

    @SerializedName("dipesan_pada")
    private String dipesanPada; // timestamptz

    @SerializedName("tanggal_keluar")
    private String tanggalKeluar; // date

    // Relasi (Nama field harus sama dengan nama tabel relasi)
    @SerializedName("pendaki_rombongan")
    private List<PendakiRombongan> pendakiRombongan;

    @SerializedName("barang_bawaan_sampah")
    private List<BarangBawaanSampah> barangBawaanSampah;

    @com.google.gson.annotations.SerializedName("nominal_refund")
    private Long nominalRefund;

    @com.google.gson.annotations.SerializedName("bukti_refund")
    private String buktiRefund;

    @com.google.gson.annotations.SerializedName("alasan_batal")
    private String alasanBatal;

    @com.google.gson.annotations.SerializedName("bank_refund")
    private String bankRefund;

    @com.google.gson.annotations.SerializedName("no_rek_refund")
    private String noRekRefund;

    @com.google.gson.annotations.SerializedName("atas_nama_refund")
    private String atasNamaRefund;

    @com.google.gson.annotations.SerializedName("persentase_refund")
    private Integer persentaseRefund;

    public long getIdReservasi() {
        return idReservasi;
    }
    public String getIdPengguna() {
        return idPengguna;
    }
    public String getKodeReservasi() {
        return kodeReservasi;
    }
    public String getTanggalPendakian() {
        return tanggalPendakian;
    }
    public int getJumlahPendaki() {
        return jumlahPendaki;
    }
    public int getJumlahTiketParkir() {
        return jumlahTiketParkir;
    }
    public int getTotalHarga() {
        return totalHarga;
    }
    public String getStatus() {
        return status;
    }
    public int getJumlahPotensiSampah() {
        return jumlahPotensiSampah;
    }
    public String getStatusSampah() {
        return statusSampah;
    }
    public String getDipesanPada() {
        return dipesanPada;
    }
    public String getTanggalKeluar() {
        return tanggalKeluar;
    }
    public List<PendakiRombongan> getPendakiRombongan() {
        return pendakiRombongan;
    }
    public List<BarangBawaanSampah> getBarangBawaanSampah() {
        return barangBawaanSampah;
    }
    public Long getNominalRefund() {
        return nominalRefund != null ? nominalRefund : 0L;
    }

    public String getBuktiRefund() {
        return buktiRefund;
    }

    public String getAlasanBatal() {
        return alasanBatal;
    }
}