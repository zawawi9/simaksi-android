package com.zawww.e_simaksi.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.KuotaHarian;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class KuotaAdapter extends RecyclerView.Adapter<KuotaAdapter.KuotaViewHolder> {

    private List<KuotaHarian> kuotaList;

    public KuotaAdapter(List<KuotaHarian> kuotaList) {
        this.kuotaList = kuotaList;
    }

    @NonNull
    @Override
    public KuotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kuota, parent, false);
        return new KuotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KuotaViewHolder holder, int position) {
        KuotaHarian kuota = kuotaList.get(position);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(kuota.getTanggalKuota());

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            holder.tanggal.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.tanggal.setText(kuota.getTanggalKuota()); // Fallback to raw string
            e.printStackTrace();
        }


        int sisaKuota = kuota.getKuotaMaksimal() - kuota.getKuotaTerpesan();
        holder.sisa.setText("Sisa " + sisaKuota);

        if (sisaKuota == 0) {
            holder.sisa.setTextColor(Color.RED);
        } else {
            holder.sisa.setTextColor(Color.parseColor("#004D40")); // hijau_tua_brand
        }
    }

    @Override
    public int getItemCount() {
        return kuotaList.size();
    }

    public void updateData(List<KuotaHarian> newList) {
        this.kuotaList.clear();
        this.kuotaList.addAll(newList);
        notifyDataSetChanged();
    }

    static class KuotaViewHolder extends RecyclerView.ViewHolder {
        TextView tanggal, sisa;

        KuotaViewHolder(@NonNull View itemView) {
            super(itemView);
            tanggal = itemView.findViewById(R.id.tv_kuota_tanggal);
            sisa = itemView.findViewById(R.id.tv_kuota_sisa);
        }
    }
}
