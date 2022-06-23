package cl.rutasegura.rutaseguradriver.activities.calificationclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import cl.rutasegura.rutaseguradriver.Model.ClientBooking;
import cl.rutasegura.rutaseguradriver.Model.CreatePromoteQualification;
import cl.rutasegura.rutaseguradriver.Model.HistoryBooking;
import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsActivity;
import cl.rutasegura.rutaseguradriver.databinding.ActivityCalificationClientBinding;
import cl.rutasegura.rutaseguradriver.provider.ClientBookingProvider;
import cl.rutasegura.rutaseguradriver.provider.CreatePromoteQualificationProvider;
import cl.rutasegura.rutaseguradriver.provider.HistoryBookingProvider;

public class CalificationClientActivity extends AppCompatActivity {
    private ActivityCalificationClientBinding binding;
    private ClientBookingProvider mClientBookingProvider;
    private String mExtraClientId;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private String mdistanceextra;
    private String mdurationextra;
    private float mCalification = 0;
    private CreatePromoteQualificationProvider mCreatePromoteQualificationProvider;
    private CreatePromoteQualification mPromoteQualification;

    private double mExtraPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalificationClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialization();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void initialization() {
        mClientBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        mExtraClientId = getIntent().getStringExtra("idClient");
        mExtraPrice = getIntent().getDoubleExtra("price", 0);
        mdistanceextra = getIntent().getStringExtra("distancetext");
        mdurationextra = getIntent().getStringExtra("durationtext");
        mCreatePromoteQualificationProvider = new CreatePromoteQualificationProvider();


        binding.textViewPrice.setText(String.format("%.1f", mExtraPrice)  + "$");

        binding.ratingbarCalification.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {
                mCalification = calification;
            }
        });
        binding.btnCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });

        getClientBooking();
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);
                    binding.textViewOriginCalification.setText(clientBooking.getOrigin());
                    binding.textViewDestinationCalification.setText(clientBooking.getDestination());
                    mHistoryBooking =new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            mdurationextra,
                            mdistanceextra,
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng(),
                            Math.round(clientBooking.getPrice())
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void calificate() {
        if (mCalification  > 0) {
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mHistoryBookingProvider.updateCalificactionClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapsActivity.class);
                                intent.putExtra("connect", true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapsActivity.class);
                                intent.putExtra("connect", true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            Toast.makeText(this, "Debes ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }

}