package cl.rutasegura.rutaseguradriver.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import cl.rutasegura.rutaseguradriver.activities.maps.MapsActivity;
import cl.rutasegura.rutaseguradriver.activities.maps.MapsDriverBookingActivity;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.ClientBookingProvider;
import cl.rutasegura.rutaseguradriver.provider.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removelocation(mAuthProvider.getId());

        String idClient = intent.getExtras().getString("idClient");
        String searchById = intent.getExtras().getString("searchById");
        mClientBookingProvider = new ClientBookingProvider();

        if (searchById.equals("true")) {
            mClientBookingProvider.updateStatus(idClient, "accept");
            Intent intent1 = new Intent(context, MapsDriverBookingActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.setAction(Intent.ACTION_RUN);
            intent1.putExtra("idClient", idClient);
            context.startActivity(intent1);
        }
        else {
            checkIfClientBookignWasAccept(idClient, context);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

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

}
