package dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.zawww.e_simaksi.R;
import com.zawww.e_simaksi.model.Promosi; // Ganti model

import java.util.List;

public class PromosiSliderAdapter extends RecyclerView.Adapter<PromosiSliderAdapter.SliderViewHolder> {

    private List<Promosi> promosiList;
    private Context context;

    public PromosiSliderAdapter(List<Promosi> promosiList, Context context) {
        this.promosiList = promosiList;
        this.context = context;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poster_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Promosi promosi = promosiList.get(position);

        // Pakai Glide untuk memuat gambar dari URL ke ImageView
        Glide.with(context)
                .load(promosi.getUrlGambar())
                .placeholder(R.drawable.placeholder_loading) // Buat gambar placeholder
                .error(R.drawable.placeholder_error) // Buat gambar error
                .into(holder.imageViewPoster);
    }

    @Override
    public int getItemCount() {
        return promosiList.size();
    }

    public void updateData(List<Promosi> newList) {
        this.promosiList.clear();
        this.promosiList.addAll(newList);
        notifyDataSetChanged();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPoster;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPoster = itemView.findViewById(R.id.image_view_poster);
        }
    }
}