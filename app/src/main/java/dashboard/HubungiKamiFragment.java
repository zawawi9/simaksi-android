package dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.zawww.e_simaksi.R;

import java.util.ArrayList;
import java.util.List;

public class HubungiKamiFragment extends Fragment {

    private LinearLayout faqContainer;
    private List<FaqItem> faqList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hubungi_kami, container, false);

        faqContainer = view.findViewById(R.id.faq_container);
        
        // Initialize FAQ data
        initializeFAQData();
        
        // Populate FAQ items
        populateFAQItems();

        return view;
    }

    private void initializeFAQData() {
        faqList = new ArrayList<>();
        
        // Pertanyaan dan jawaban tentang pendakian Gunung Butak
        faqList.add(new FaqItem(
            "Apa saja persyaratan untuk mendaki Gunung Butak?",
            "Untuk mendaki Gunung Butak, Anda perlu membawa:\n" +
            "• Surat keterangan sehat dari dokter\n" +
            "• Tiket pendakian yang telah dibeli melalui aplikasi E-Simaksi\n" +
            "• Perlengkapan pendakian lengkap (sleeping bag, tenda, peralatan memasak)\n" +
            "• Pakaian yang sesuai dengan cuaca pegunungan\n" +
            "• Identitas diri (KTP/SIM/Paspor)"
        ));
        
        faqList.add(new FaqItem(
            "Berapa lama waktu yang dibutuhkan untuk mencapai puncak?",
            "Waktu pendakian dari basecamp menuju puncak Gunung Butak sekitar 6-8 jam tergantung kondisi fisik pendaki dan cuaca. Rata-rata pendaki membutuhkan waktu sekitar 7 jam untuk mencapai puncak."
        ));
        
        faqList.add(new FaqItem(
            "Apakah diperlukan pemandu pendakian?",
            "Untuk pendaki yang belum berpengalaman, sangat disarankan untuk menggunakan jasa pemandu pendakian. Pemandu akan membantu Anda menavigasi jalur pendakian dan memberikan informasi penting seputar pendakian."
        ));
        
        faqList.add(new FaqItem(
            "Apakah ada batas usia untuk mendaki Gunung Butak?",
            "Tidak ada batas usia bawah dan atas secara resmi, tetapi pendaki harus dalam kondisi sehat dan siap secara fisik. Anak-anak di bawah usia 12 tahun sebaiknya didampingi orang dewasa."
        ));
        
        faqList.add(new FaqItem(
            "Apa saja rute pendakian di Gunung Butak?",
            "Saat ini tersedia dua rute pendakian utama:\n" +
            "• Rute Kucur: Rute standar dengan medan sedang, jarak sekitar 4,5 km\n" +
            "• Rute Selo: Rute alternatif dengan medan lebih menantang, jarak sekitar 5,2 km"
        ));
        
        faqList.add(new FaqItem(
            "Berapa biaya pendakian Gunung Butak?",
            "Biaya pendakian saat ini:\n" +
            "• Weekday (Senin-Jumat): Rp 75.000/orang\n" +
            "• Weekend (Sabtu-Minggu): Rp 100.000/orang\n" +
            "• Libur Nasional: Rp 125.000/orang\n" +
            "Harga sudah termasuk tiket masuk kawasan dan asuransi selama pendakian."
        ));
        
        faqList.add(new FaqItem(
            "Apa yang harus dilakukan saat cuaca buruk?",
            "Jika terjadi perubahan cuaca yang membahayakan, pendaki harus mengikuti instruksi dari petugas. Pendakian bisa ditunda atau dibatalkan demi keselamatan. Selalu periksa prakiraan cuaca sebelum berangkat."
        ));
        
        faqList.add(new FaqItem(
            "Apakah ada fasilitas di pos pendakian?",
            "Tersedia beberapa pos pemeriksaan dengan fasilitas dasar:\n" +
            "• Pos 1 (Basecamp): Pusat informasi dan registrasi pendakian\n" +
            "• Pos 2: Area peristirahatan dengan toilet umum\n" +
            "• Pos 3: Area peristirahatan dan tempat berkemah sementara"
        ));
        
        faqList.add(new FaqItem(
            "Apakah bisa memesan tiket secara langsung di tempat?",
            "Kami menganjurkan untuk memesan tiket secara online melalui aplikasi E-Simaksi untuk menghindari kehabisan kuota. Tiket bisa dibeli maksimal H-1 sebelum pendakian. Pembelian di tempat tersedia tergantung ketersediaan."
        ));
        
        faqList.add(new FaqItem(
            "Bagaimana dengan kebijakan pembatalan?",
            "Kebijakan pembatalan:\n" +
            "• Pembatalan H-3 sebelum pendakian: Dapat pengembalian 100%\n" +
            "• Pembatalan H-2 sebelum pendakian: Dapat pengembalian 50%\n" +
            "• Pembatalan H-1 atau hari H: Tidak ada pengembalian dana"
        ));
    }

    private void populateFAQItems() {
        if (faqList != null) {
            faqContainer.removeAllViews();
            for (FaqItem faqItem : faqList) {
                View faqItemView = createFAQItemView(faqItem);
                faqContainer.addView(faqItemView);
            }
        }
    }

    private View createFAQItemView(FaqItem faqItem) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_faq, faqContainer, false);
        
        TextView questionTextView = view.findViewById(R.id.faq_question);
        TextView answerTextView = view.findViewById(R.id.faq_answer);
        ImageView arrowImageView = view.findViewById(R.id.faq_arrow);
        LinearLayout headerLayout = view.findViewById(R.id.faq_header);

        questionTextView.setText(faqItem.getQuestion());
        answerTextView.setText(faqItem.getAnswer());

        headerLayout.setOnClickListener(v -> {
            boolean isExpanded = answerTextView.getVisibility() == View.VISIBLE;
            
            if (isExpanded) {
                answerTextView.setVisibility(View.GONE);
                arrowImageView.setRotation(0);
            } else {
                answerTextView.setVisibility(View.VISIBLE);
                arrowImageView.setRotation(180);
            }
        });

        return view;
    }

    // Inner class for FAQ items
    private static class FaqItem {
        private String question;
        private String answer;

        public FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}