package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;

public class KuotaHarian {
    @SerializedName("id_kuota")
    private int idKuota;

    @SerializedName("tanggal_kuota")
    private String tanggalKuota;

    @SerializedName("kuota_maksimal")
    private int kuotaMaksimal;

    @SerializedName("kuota_terpesan")
    private int kuotaTerpesan;

    // Buat Getter untuk semua field ini (Penting untuk Retrofit)
    public int getIdKuota() { return idKuota; }
    public String getTanggalKuota() { return tanggalKuota; }
    public int getKuotaMaksimal() { return kuotaMaksimal; }
    public int getKuotaTerpesan() { return kuotaTerpesan; }
}