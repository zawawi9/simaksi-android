package com.zawww.e_simaksi.model;

import com.google.gson.annotations.SerializedName;

// Pastikan untuk mengimplementasikan Serializable jika Anda ingin
// mengirim objek ini antar fragment melalui Bundle.
import java.io.Serializable;

public class Profile implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("nama_lengkap")
    private String namaLengkap;

    @SerializedName("email")
    private String email;

    @SerializedName("nomor_telepon")
    private String nomorTelepon;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("nik")
    private String nik;

    @SerializedName("tanggal_lahir")
    private String tanggalLahir; // String untuk tipe data 'date'

    @SerializedName("peran")
    private String peran;

    // Constructor kosong (diperlukan untuk deserializer Gson)
    public Profile() {
    }

    // Constructor untuk membuat objek update (hanya field yang bisa diedit)
    public Profile(String namaLengkap, String nik, String tanggalLahir, String nomorTelepon, String alamat) {
        this.namaLengkap = namaLengkap;
        this.nik = nik;
        this.tanggalLahir = tanggalLahir;
        this.nomorTelepon = nomorTelepon;
        this.alamat = alamat;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getEmail() { return email; }
    public String getNomorTelepon() { return nomorTelepon; }
    public String getAlamat() { return alamat; }
    public String getNik() { return nik; }
    public String getTanggalLahir() { return tanggalLahir; }
    public String getPeran() { return peran; }

    // --- SETTERS ---
    // (Setters ini digunakan oleh Gson saat mengambil data
    // dan oleh kita saat membuat objek untuk 'update')
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPeran(String peran) { this.peran = peran; }

    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setNik(String nik) { this.nik = nik; }
    public void setTanggalLahir(String tanggalLahir) { this.tanggalLahir = tanggalLahir; }
}