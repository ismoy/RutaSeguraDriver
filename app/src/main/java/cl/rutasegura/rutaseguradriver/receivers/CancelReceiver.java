package cl.rutasegura.rutaseguradriver.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.ClientBookingProvider;
import cl.rutasegura.rutaseguradriver.provider.DriversFoundProvider;


public class CancelReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    private DriversFoundProvider mDriversFoundProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient = intent.getExtras().getString("idClient");
        String searchById = intent.getExtras().getString("searchById");
        mClientBookingProvider = new ClientBookingProvider();
        mDriversFoundProvider = new DriversFoundProvider();
        mAuthProvider = new AuthProvider();

        if (searchById.equals("true")) {
            mClientBookingProvider.updateStatus(idClient, "cancel");
        }
        mDriversFoundProvider.delete(mAuthProvider.getId());

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
