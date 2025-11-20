package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;

public class HitungReservasiResponse {
    @SerializedName("harga_awal")
    private long hargaAwal;

    @SerializedName("nominal_diskon")
    private long nominalDiskon;

    @SerializedName("total_akhir")
    private long totalAkhir;

    @SerializedName("id_promosi_applied")
    private Long idPromosiApplied; // Bisa null

    @SerializedName("berhasil")
    private boolean berhasil;
    public long getHargaAwal() { return hargaAwal; }
    public long getNominalDiskon() { return nominalDiskon; }
    public long getTotalAkhir() { return totalAkhir; }
    public Long getIdPromosiApplied() { return idPromosiApplied; }
    public boolean isBerhasil() { return berhasil; }
}