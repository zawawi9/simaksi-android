package informasi_gunung;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zawww.e_simaksi.R;

import java.util.ArrayList;

public class JalurAdapter extends RecyclerView.Adapter<JalurAdapter.ViewHolder> {

    private final ArrayList<JalurModel> list;

    public JalurAdapter(ArrayList<JalurModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jalur, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JalurModel data = list.get(position);
        holder.tvNama.setText(data.getNama());
        holder.tvTanah.setText("Kondisi Tanah: " + data.getTanah());
        holder.tvHambatan.setText("Hambatan: " + data.getHambatan());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTanah, tvHambatan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaJalur);
            tvTanah = itemView.findViewById(R.id.tvTanah);
            tvHambatan = itemView.findViewById(R.id.tvHambatan);
        }
    }
}