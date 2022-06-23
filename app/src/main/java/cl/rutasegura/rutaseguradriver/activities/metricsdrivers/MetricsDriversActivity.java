package cl.rutasegura.rutaseguradriver.activities.metricsdrivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsActivity;
import cl.rutasegura.rutaseguradriver.databinding.ActivityMetricsDriversBinding;
import cl.rutasegura.rutaseguradriver.provider.DriverProvider;
import cl.rutasegura.rutaseguradriver.provider.HistoryBookingProvider;

public class MetricsDriversActivity extends AppCompatActivity {
   private ActivityMetricsDriversBinding binding;
   private DriverProvider mDriverProvider;
   private HistoryBookingProvider mHistoryBookingProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityMetricsDriversBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mDriverProvider = new DriverProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        getCalification();
        binding.arrowBack.setOnClickListener(v->{
            startActivity(new Intent(MetricsDriversActivity.this, MapsActivity.class));
        });
    }

    private void getCalification(){
        mHistoryBookingProvider.getHistory().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    long valor =snapshot.getChildrenCount();
                    binding.totalviaje.setText(String.valueOf(valor));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}