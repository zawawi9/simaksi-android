package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;

public class Pengumuman {
    @SerializedName("id_pengumuman")
    private long id_pengumuman;
    @SerializedName("judul")
    private String judul;
    @SerializedName("konten")
    private String konten;
    @SerializedName("start_date")
    private String start_date;
    @SerializedName("end_date")
    private String end_date;
    @SerializedName("telah_terbit")
    private boolean telah_terbit;

    // Getters and setters
    public long getIdPengumuman() {
        return id_pengumuman;
    }

    public void setIdPengumuman(long id_pengumuman) {
        this.id_pengumuman = id_pengumuman;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getKonten() {
        return konten;
    }

    public void setKonten(String konten) {
        this.konten = konten;
    }

    public String getStartDate() {
        return start_date;
    }

    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }

    public boolean isTelahTerbit() {
        return telah_terbit;
    }

    public void setTelahTerbit(boolean telah_terbit) {
        this.telah_terbit = telah_terbit;
    }
}
