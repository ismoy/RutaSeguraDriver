package cl.rutasegura.rutaseguradriver.activities.historybooking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import cl.rutasegura.rutaseguradriver.Model.ClientProvider;
import cl.rutasegura.rutaseguradriver.Model.HistoryBooking;
import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.databinding.ActivityHistoryBookingDetailDriverBinding;
import cl.rutasegura.rutaseguradriver.provider.HistoryBookingProvider;

public class HistoryBookingDetailDriverActivity extends AppCompatActivity {
    private ActivityHistoryBookingDetailDriverBinding binding;
    private String mExtraId;
    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBookingDetailDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialization();
    }

    private void initialization() {
        mClientProvider = new ClientProvider();
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        getHistoryBooking();

        binding.circleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HistoryBooking historyBooking = dataSnapshot.getValue(HistoryBooking.class);
                    binding.textViewOriginHistoryBookingDetail.setText(historyBooking.getOrigin());
                    binding.textViewDestinationHistoryBookingDetail.setText(historyBooking.getDestination());
                    binding.textViewCalificationHistoryBookingDetail.setText("Tu calificacion:" + historyBooking.getCalificationDriver());

                    if (dataSnapshot.hasChild("calificationClient")) {
                        binding.ratingBarHistoryBookingDetail.setRating(historyBooking.getCalificationClient());

                    }

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String name = dataSnapshot.child("firstname").getValue().toString();
                                binding.textViewNameBookingDetail.setText(name.toUpperCase());
                                if (dataSnapshot.hasChild("image")) {
                                    String image = dataSnapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriverActivity.this).load(image).into(binding.circleImageHistoryBookingDetail);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}