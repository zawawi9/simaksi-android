package com.zawww.e_simaksi.model; // Sesuaikan package Anda

import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.zawww.e_simaksi.model.PendakiRombongan;
import com.zawww.e_simaksi.model.BarangBawaanSampah;

public class BuatReservasiRequest {

    @SerializedName("p_id_pengguna")
    private String idPengguna;

    @SerializedName("p_tanggal_pendakian")
    private String tanggalPendakian;

    @SerializedName("p_tanggal_keluar")
    private String tanggalKeluar;

    @SerializedName("p_jumlah_pendaki")
    private int jumlahPendaki;

    @SerializedName("p_jumlah_parkir")
    private int jumlahParkir;

    @SerializedName("p_kode_promo")
    private String kodePromo;

    @SerializedName("p_pendaki_rombongan")
    private List<PendakiRombongan> pendakiRombongan; // <-- Ini sudah benar

    @SerializedName("p_barang_bawaan")
    private List<BarangBawaanSampah> barangBawaan;
    public BuatReservasiRequest(String idPengguna, String tanggalPendakian, String tanggalKeluar,
                                int jumlahPendaki, int jumlahParkir, String kodePromo,
                                List<PendakiRombongan> pendakiRombongan,
                                List<BarangBawaanSampah> barangBawaan) { // <-- DIUBAH DI SINI
        this.idPengguna = idPengguna;
        this.tanggalPendakian = tanggalPendakian;
        this.tanggalKeluar = tanggalKeluar;
        this.jumlahPendaki = jumlahPendaki;
        this.jumlahParkir = jumlahParkir;
        this.kodePromo = kodePromo;
        this.pendakiRombongan = pendakiRombongan;
        this.barangBawaan = barangBawaan; // <-- DIUBAH DI SINI
    }
}