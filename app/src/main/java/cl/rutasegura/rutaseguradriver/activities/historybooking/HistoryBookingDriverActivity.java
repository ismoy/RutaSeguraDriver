package cl.rutasegura.rutaseguradriver.activities.historybooking;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cl.rutasegura.rutaseguradriver.Model.HistoryBooking;
import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsActivity;
import cl.rutasegura.rutaseguradriver.adapters.HistoryBookingDriverAdapter;
import cl.rutasegura.rutaseguradriver.databinding.ActivityHistoryBookingDriverBinding;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.HistoryBookingProvider;

public class HistoryBookingDriverActivity extends AppCompatActivity {
    private ActivityHistoryBookingDriverBinding binding;
    private HistoryBookingDriverAdapter mAdapter;
    private AuthProvider mAuthProvider;
    private HistoryBookingProvider mHistoryBookingProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBookingDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialization();
    }

    private void initialization() {
        mAuthProvider = new AuthProvider();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerViewHistoryBooking.setLayoutManager(linearLayoutManager);
        mHistoryBookingProvider = new HistoryBookingProvider();
        binding.arrowBack.setOnClickListener(v->{
            startActivity(new Intent(HistoryBookingDriverActivity.this, MapsActivity.class));
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("HistoryBooking")
                .orderByChild("idDriver")
                .equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query, HistoryBooking.class)
                .build();
        mAdapter = new HistoryBookingDriverAdapter(options, HistoryBookingDriverActivity.this);

        binding.recyclerViewHistoryBooking.setAdapter(mAdapter);
        mAdapter.startListening();
    }



    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}