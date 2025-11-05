package dashboard;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zawww.e_simaksi.model.BarangBawaanSampah;
import com.zawww.e_simaksi.model.PendakiRombongan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReservasiSharedViewModel extends ViewModel {

    // Lulus / Gagal di Langkah 1
    private final MutableLiveData<Boolean> _isStep1Valid = new MutableLiveData<>(false);
    public LiveData<Boolean> isStep1Valid = _isStep1Valid;

    // Data dari Langkah 1
    public final MutableLiveData<String> tanggalMasuk = new MutableLiveData<>();
    public final MutableLiveData<String> tanggalKeluar = new MutableLiveData<>();
    public final MutableLiveData<Integer> jumlahPendaki = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> totalHarga = new MutableLiveData<>(0);
    public final HashMap<String, Uri> mapSuratSehat = new HashMap<>();
    public final List<PendakiRombongan> listPendaki = new ArrayList<>();
    public final List<BarangBawaanSampah> listBarang = new ArrayList<>();
    public final MutableLiveData<Integer> jumlahParkir = new MutableLiveData<>(0);

    public void setStep1Valid(boolean isValid) {
        _isStep1Valid.setValue(isValid);
    }
}