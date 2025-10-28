package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
public class BarangBawaanSampah {

    @SerializedName("id_barang")
    private long idBarang;

    @SerializedName("id_reservasi")
    private long idReservasi;

    @SerializedName("nama_barang")
    private String namaBarang;

    @SerializedName("jenis_sampah")
    private String jenisSampah;
    @SerializedName("jumlah")
    private int jumlah;

    public long getIdBarang() { return idBarang; }
    public long getIdReservasi() { return idReservasi; }
    public String getNamaBarang() { return namaBarang; }
    public String getJenisSampah() { return jenisSampah; }
    public int getJumlah() { return jumlah; }
}