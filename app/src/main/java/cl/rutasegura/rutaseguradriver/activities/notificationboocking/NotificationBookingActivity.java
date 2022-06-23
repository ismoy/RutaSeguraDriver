package cl.rutasegura.rutaseguradriver.activities.notificationboocking;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsActivity;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsDriverBookingActivity;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.ClientBookingProvider;
import cl.rutasegura.rutaseguradriver.provider.DriversFoundProvider;
import cl.rutasegura.rutaseguradriver.provider.GeofireProvider;

public class NotificationBookingActivity extends AppCompatActivity {

    private TextView mTextViewDestination;
    private TextView mTextViewOrigin;
    private TextView mTextViewMin;
    private TextView mTextViewDistance;
    private TextView mTextViewCounter;
    private Button mbuttonAccept;
    private Button mbuttonCancel;

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    private String mExtraIdClient;
    private String mExtraOrigin;
    private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;
    private String mExtraSearchById;

    private MediaPlayer mMediaPlayer;

    private int mCounter = 25;
    private Handler mHandler;

    private DriversFoundProvider mDriversFoundProvider;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter = mCounter -1;
            mTextViewCounter.setText(String.valueOf(mCounter));
            if (mCounter > 0) {
                initTimer();
            }
            else {
                cancelBooking();
            }
        }
    };
    private ValueEventListener mListener;

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewMin = findViewById(R.id.textViewMin);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        mTextViewCounter = findViewById(R.id.textViewCounter);
        mbuttonAccept = findViewById(R.id.btnAcceptBooking);
        mbuttonCancel = findViewById(R.id.btnCancelBooking);

        mAuthProvider = new AuthProvider();
        mDriversFoundProvider = new DriversFoundProvider();

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");
        mExtraSearchById = getIntent().getStringExtra("searchById");

        mTextViewDestination.setText(mExtraDestination);
        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewMin.setText(mExtraMin);
        mTextViewDistance.setText(mExtraDistance);

        mMediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mMediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();

        checkIfClientCancelBooking();

        mbuttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        mbuttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });
    }

    private void checkIfClientBookignWasAccept(final String idClient, final Context context) {
        mClientBookingProvider.getClientBooking(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")) {
                        String status = snapshot.child("status").getValue().toString();
                        String idDriver = snapshot.child("idDriver").getValue().toString();

                        if (status.equals("create") && idDriver.equals("")) {
                            mClientBookingProvider.updateStatusAndIdDriver(idClient, "accept", mAuthProvider.getId());
                            Intent intent1 = new Intent(context, MapsDriverBookingActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.setAction(Intent.ACTION_RUN);
                            intent1.putExtra("idClient", idClient);
                            context.startActivity(intent1);
                        }
                        else {
                            goToMapDriverActivity(context);
                        }
                    }
                    else {
                        goToMapDriverActivity(context);
                    }
                }
                else {
                    goToMapDriverActivity(context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void goToMapDriverActivity(Context context) {
        Toast.makeText(context, "Otro conductor ya acepto el viaje", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(context, MapsActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);
    }

    private void checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    goToMapDriverActivity();
                }
                // SIGNIFICA QUE EL CLIENT BOOKING SI EXISTE
                else if (dataSnapshot.hasChild("idDriver") && dataSnapshot.hasChild("status")){
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    if ((status.equals("accept") || status.equals("cancel")) && !idDriver.equals(mAuthProvider.getId())) {
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(2);
                        goToMapDriverActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToMapDriverActivity() {
        Toast.makeText(NotificationBookingActivity.this, "El cliente ya no esta disponible", Toast.LENGTH_LONG).show();
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        Intent intent = new Intent(NotificationBookingActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void cancelBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mExtraSearchById.equals("true")) {
            mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");
        }
        Log.d("CLIENTE", "ID: " + mExtraIdClient);

        mDriversFoundProvider.delete(mAuthProvider.getId());

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
        Intent intent = new Intent(NotificationBookingActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void acceptBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removelocation(mAuthProvider.getId());
        mClientBookingProvider = new ClientBookingProvider();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        if (mExtraSearchById.equals("true")) {
            mClientBookingProvider.updateStatus(mExtraIdClient, "accept");
            Intent intent1 = new Intent(NotificationBookingActivity.this, MapsDriverBookingActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.setAction(Intent.ACTION_RUN);
            intent1.putExtra("idClient", mExtraIdClient);
            startActivity(intent1);
        }
        else {
            checkIfClientBookignWasAccept(mExtraIdClient, NotificationBookingActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.release();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}
