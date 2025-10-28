package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
public class PendakiRombongan {

    @SerializedName("id_pendaki")
    private long idPendaki;

    @SerializedName("id_reservasi")
    private long idReservasi;

    @SerializedName("nama_lengkap")
    private String namaLengkap;

    @SerializedName("nik")
    private String nik;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("nomor_telepon")
    private String nomorTelepon;

    @SerializedName("kontak_darurat")
    private String kontakDarurat;

    @SerializedName("url_surat_sehat")
    private String urlSuratSehat;

    public long getIdPendaki() { return idPendaki; }
    public long getIdReservasi() { return idReservasi; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getNik() { return nik; }
    public String getAlamat() { return alamat; }
    public String getNomorTelepon() { return nomorTelepon; }
    public String getKontakDarurat() { return kontakDarurat; }
    public String getUrlSuratSehat() { return urlSuratSehat; }
}