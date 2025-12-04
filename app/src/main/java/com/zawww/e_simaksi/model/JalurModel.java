package com.zawww.e_simaksi.model;

public class JalurModel {
    private String nama, tanah, hambatan;

    public JalurModel(String nama, String tanah, String hambatan) {
        this.nama = nama;
        this.tanah = tanah;
        this.hambatan = hambatan;
    }

    public String getNama() { return nama; }
    public String getTanah() { return tanah; }
    public String getHambatan() { return hambatan;}
}