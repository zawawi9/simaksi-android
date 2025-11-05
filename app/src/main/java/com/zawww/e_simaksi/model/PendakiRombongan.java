package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;
public class PendakiRombongan {
    public PendakiRombongan(String namaLengkap, String nik, String alamat, String nomorTelepon, String kontakDarurat) {
        this.namaLengkap = namaLengkap;
        this.nik = nik;
        this.alamat = alamat;
        this.nomorTelepon = nomorTelepon;
        this.kontakDarurat = kontakDarurat;
        // Kita biarkan idPendaki, idReservasi, dan urlSuratSehat null/0
    }
    public void setUrlSuratSehat(String urlSuratSehat) {
        this.urlSuratSehat = urlSuratSehat;
    }

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
    public void setIdReservasi(long idReservasi) {
        this.idReservasi = idReservasi;
    }
}