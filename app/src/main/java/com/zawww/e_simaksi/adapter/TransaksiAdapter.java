package com.zawww.e_simaksi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.Reservasi;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder> {

    private List<Reservasi> transaksiList;
    private OnTransaksiClickListener listener;

    public interface OnTransaksiClickListener {
        void onDetailClick(Reservasi reservasi);
    }

    public TransaksiAdapter(List<Reservasi> transaksiList, OnTransaksiClickListener listener) {
        this.transaksiList = transaksiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransaksiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new TransaksiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiViewHolder holder, int position) {
        Reservasi transaksi = transaksiList.get(position);
        holder.bind(transaksi);
    }

    @Override
    public int getItemCount() {
        return transaksiList != null ? transaksiList.size() : 0;
    }

    public void updateData(List<Reservasi> newData) {
        this.transaksiList = newData;
        notifyDataSetChanged();
    }

    public class TransaksiViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTransaksiIcon;
        private TextView tvTransaksiJenis, tvTransaksiTanggal, tvTransaksiWaktu, tvTransaksiHarga;
        private TextView tvTransaksiKode, tvTransaksiStatus;
        private Button btnTransaksiDetail;

        public TransaksiViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransaksiIcon = itemView.findViewById(R.id.iv_transaksi_icon);
            tvTransaksiJenis = itemView.findViewById(R.id.tv_transaksi_jenis);
            tvTransaksiTanggal = itemView.findViewById(R.id.tv_transaksi_tanggal);
            tvTransaksiWaktu = itemView.findViewById(R.id.tv_transaksi_waktu);
            tvTransaksiHarga = itemView.findViewById(R.id.tv_transaksi_harga);
            tvTransaksiKode = itemView.findViewById(R.id.tv_transaksi_kode);
            tvTransaksiStatus = itemView.findViewById(R.id.tv_transaksi_status);
            btnTransaksiDetail = itemView.findViewById(R.id.btn_transaksi_detail);
        }
        public void bind(Reservasi transaksi) {
            // Set transaction type and icon
            tvTransaksiJenis.setText("Pembelian Tiket");
            ivTransaksiIcon.setImageResource(R.drawable.ic_ticket);

            // Format date and time
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            String formattedDate = transaksi.getTanggalPendakian();
            try {
                formattedDate = outputFormat.format(inputFormat.parse(transaksi.getTanggalPendakian()));
            } catch (Exception e) {
                // If parsing fails, use the original date string
            }

            tvTransaksiTanggal.setText(formattedDate);

            // Format time from dipesan_pada field (timestamp format)
            String waktu = transaksi.getDipesanPada();
            if (waktu != null && waktu.length() > 16) {
                waktu = waktu.substring(11, 16) + " WIB"; // Extract time part
            } else {
                waktu = "Waktu tidak tersedia";
            }
            tvTransaksiWaktu.setText(waktu);

            // Format price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            String formattedPrice = currencyFormat.format(transaksi.getTotalHarga());
            tvTransaksiHarga.setText(formattedPrice.replace("Rp", "Rp "));

            // Booking code
            tvTransaksiKode.setText(transaksi.getKodeReservasi());

            // Status with appropriate color
            tvTransaksiStatus.setText(transaksi.getStatus());
            setStatusColor(transaksi.getStatus());

            // Set click listener for detail button
            btnTransaksiDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(transaksi);
                }
            });
        }

        private void setStatusColor(String status) {
            if (status != null) {
                int color;
                switch (status.toLowerCase()) {
                    case "terkonfirmasi":
                    case "berhasil":
                    case "diterima":
                        color = R.color.hijau_tua_brand;
                        break;
                    case "menunggu pembayaran":
                    case "pending":
                        color = R.color.purple_500;
                        break;
                    case "dibatalkan":
                    case "gagal":
                        color = R.color.menu_card_red;
                        break;
                    default:
                        color = R.color.purple_500;
                        break;
                }
                tvTransaksiStatus.setBackgroundResource(R.drawable.bg_status_badge);
                tvTransaksiStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(itemView.getContext().getColor(color)));
            }
        }
    }
}