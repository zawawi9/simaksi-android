package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;

public class PengaturanBiaya {
    @SerializedName("nama_item")
    private String namaItem;

    @SerializedName("harga")
    private int harga;

    // Buat Getter
    public String getNamaItem() { return namaItem; }
    public int getHarga() { return harga; }
}
