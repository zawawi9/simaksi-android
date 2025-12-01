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

import com.zawww.e_simaksi.util.DateUtil;
import android.os.Build;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder> {

    private List<Reservasi> transaksiList;
    private OnTransaksiClickListener listener;

    public interface OnTransaksiClickListener {
        void onDetailClick(Reservasi reservasi);
        void onBayarClick(Reservasi reservasi);
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
        private TextView tvTransaksiJenis, tvTransaksiTanggal, tvTransaksiHarga;
        private TextView tvTransaksiKode, tvTransaksiStatus;
        private Button btnTransaksiDetail, btnTransaksiBayar;

        public TransaksiViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransaksiIcon = itemView.findViewById(R.id.iv_transaksi_icon);
            tvTransaksiJenis = itemView.findViewById(R.id.tv_transaksi_jenis);
            tvTransaksiTanggal = itemView.findViewById(R.id.tv_transaksi_tanggal);
            tvTransaksiHarga = itemView.findViewById(R.id.tv_transaksi_harga);
            tvTransaksiKode = itemView.findViewById(R.id.tv_transaksi_kode);
            tvTransaksiStatus = itemView.findViewById(R.id.tv_transaksi_status);
            btnTransaksiDetail = itemView.findViewById(R.id.btn_transaksi_detail);
            btnTransaksiBayar = itemView.findViewById(R.id.btn_transaksi_bayar);
        }
        public void bind(Reservasi transaksi) {
            // Set transaction type and icon
            tvTransaksiJenis.setText("Pembelian Tiket");
            ivTransaksiIcon.setImageResource(R.drawable.ic_ticket);

            // Format date and time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tvTransaksiTanggal.setText(DateUtil.formatDate(transaksi.getDipesanPada()));
            } else {
                // Fallback for older Android versions
                tvTransaksiTanggal.setText(transaksi.getDipesanPada());
            }

            // Format price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            String formattedPrice = currencyFormat.format(transaksi.getTotalHarga());
            tvTransaksiHarga.setText(formattedPrice.replace("Rp", "Rp "));

            // Booking code
            tvTransaksiKode.setText(transaksi.getKodeReservasi());

            // Status with appropriate color
            tvTransaksiStatus.setText(transaksi.getStatus());
            setStatusColor(transaksi.getStatus());

            // Conditional visibility for Pay button
            if (transaksi.getStatus() != null && transaksi.getStatus().equalsIgnoreCase("menunggu_pembayaran")) {
                btnTransaksiBayar.setVisibility(View.VISIBLE);
            } else {
                btnTransaksiBayar.setVisibility(View.GONE);
            }

            // Set click listener for detail button
            btnTransaksiDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(transaksi);
                }
            });

            // Set click listener for pay button
            btnTransaksiBayar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBayarClick(transaksi);
                }
            });
        }

        private void setStatusColor(String status) {
            if (status != null) {
                int colorResId;
                switch (status.toLowerCase()) {
                    case "menunggu_pembayaran":
                        colorResId = R.color.status_menunggu_pembayaran;
                        break;
                    case "terkonfirmasi":
                        colorResId = R.color.status_terkonfirmasi;
                        break;
                    case "dibatalkan":
                        colorResId = R.color.status_dibatalkan;
                        break;
                    case "pengajuan_refund":
                        colorResId = R.color.status_pengajuan_refund;
                        break;
                    case "refund_selesai":
                        colorResId = R.color.status_refund_selesai;
                        break;
                    default:
                        colorResId = R.color.black; // Fallback color
                        break;
                }
                tvTransaksiStatus.setBackgroundResource(R.drawable.bg_status_badge); // Keep generic badge drawable
                tvTransaksiStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(itemView.getContext().getColor(colorResId)));
                tvTransaksiStatus.setTextColor(itemView.getContext().getColor(R.color.white)); // Ensure text is white for contrast
            }
        }
    }
}