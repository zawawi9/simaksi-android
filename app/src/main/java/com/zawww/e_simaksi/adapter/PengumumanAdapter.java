package com.zawww.e_simaksi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.Pengumuman;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PengumumanAdapter extends RecyclerView.Adapter<PengumumanAdapter.PengumumanViewHolder> {

    private List<Pengumuman> pengumumanList;

    public PengumumanAdapter(List<Pengumuman> pengumumanList) {
        this.pengumumanList = pengumumanList;
    }

    @NonNull
    @Override
    public PengumumanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pengumuman, parent, false);
        return new PengumumanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PengumumanViewHolder holder, int position) {
        Pengumuman pengumuman = pengumumanList.get(position);
        holder.judul.setText(pengumuman.getJudul());
        holder.konten.setText(pengumuman.getKonten());

        try {
            // Input format from Supabase (ISO 8601)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Output format for display
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));

            Date startDate = inputFormat.parse(pengumuman.getStartDate());
            Date endDate = inputFormat.parse(pengumuman.getEndDate());

            String tanggal = "Berlaku: " + outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
            holder.tanggal.setText(tanggal);

        } catch (ParseException e) {
            holder.tanggal.setText("Tanggal tidak valid");
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return pengumumanList.size();
    }

    public void updateData(List<Pengumuman> newList) {
        this.pengumumanList.clear();
        this.pengumumanList.addAll(newList);
        notifyDataSetChanged();
    }

    static class PengumumanViewHolder extends RecyclerView.ViewHolder {
        TextView judul, konten, tanggal;

        PengumumanViewHolder(@NonNull View itemView) {
            super(itemView);
            judul = itemView.findViewById(R.id.tv_pengumuman_judul);
            konten = itemView.findViewById(R.id.tv_pengumuman_konten);
            tanggal = itemView.findViewById(R.id.tv_pengumuman_tanggal);
        }
    }
}
