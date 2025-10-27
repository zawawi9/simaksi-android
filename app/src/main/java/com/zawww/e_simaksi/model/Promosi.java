package com.zawww.e_simaksi.model; // Sesuaikan dengan package kamu

import com.google.gson.annotations.SerializedName;

public class Promosi {

    // Nama variabel di Java (kiri) & nama kolom di Supabase (kanan)
    // Pastikan @SerializedName sama persis dengan nama kolom di Supabase

    @SerializedName("id_poster")
    private int idPoster;

    @SerializedName("url_gambar")
    private String urlGambar;

    @SerializedName("judul_poster")
    private String judulPoster;

    @SerializedName("is_aktif")
    private boolean isAktif;

    @SerializedName("urutan")
    private int urutan;


    // --- Getters ---
    // (Dibutuhkan oleh Adapter)

    public int getIdPoster() {
        return idPoster;
    }

    public String getUrlGambar() {
        return urlGambar;
    }

    public String getJudulPoster() {
        return judulPoster;
    }

    public boolean isAktif() {
        return isAktif;
    }

    public int getUrutan() {
        return urutan;
    }
}