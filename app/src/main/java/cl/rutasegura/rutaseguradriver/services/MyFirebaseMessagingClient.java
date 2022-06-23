package cl.rutasegura.rutaseguradriver.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.notificationboocking.NotificationBookingActivity;
import cl.rutasegura.rutaseguradriver.channel.NotificationHelper;
import cl.rutasegura.rutaseguradriver.receivers.AcceptReceiver;
import cl.rutasegura.rutaseguradriver.receivers.CancelReceiver;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    String idClient = data.get("idClient");
                    Log.d("NOTIFICACION", "id client: " + idClient);
                    String origin = data.get("origin");
                    String destination  = data.get("destination");
                    String min = data.get("min");
                    String distance = data.get("distance");
                    String searchById = data.get("searchById");
                    showNotificationApiOreoActions(title, body, idClient, searchById);
                    showNotificationActivity(idClient, origin, destination, min, distance, searchById);
                }
                else if (title.contains("RUTASEGURA CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // eliminando la notificacion de solicitud de viaje
                    manager.cancel(2);
                    //showNotificationApiOreo(title, body);
                }
                else {
                    showNotificationApiOreo(title, body);
                }
            }
            else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    String idClient = data.get("idClient");
                    String origin = data.get("origin");
                    String destination  = data.get("destination");
                    String min = data.get("min");
                    String distance = data.get("distance");
                    String searchById = data.get("searchById");
                    showNotificationActions(title, body, idClient, searchById);
                    showNotificationActivity(idClient, origin, destination, min, distance, searchById);
                }
                else if (title.contains("RUTASEGURA CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    //showNotification(title, body);
                }
                else {
                    showNotification(title, body);
                }
            }
        }
    }

    private void showNotificationActivity(String idClient, String origin, String destination, String min, String distance, String searchById) {
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                             PowerManager.ACQUIRE_CAUSES_WAKEUP |
                             PowerManager.ON_AFTER_RELEASE,
                             "AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("idClient", idClient);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        intent.putExtra("min", min);
        intent.putExtra("distance", distance);
        intent.putExtra("searchById", searchById);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);




        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPI(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    private void showNotificationActions(String title, String body, String idClient, String searchById) {

        // ACEPTAR
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        acceptIntent.putExtra("searchById", searchById);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        // CANCELAR

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        cancelIntent.putExtra("searchById", searchById);

        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPIActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_MUTABLE);
        }
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoActions(String title, String body, String idClient, String searchById) {

        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        acceptIntent.putExtra("searchById", searchById);
        PendingIntent acceptPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_MUTABLE);
        }

        Notification.Action acceptAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        cancelIntent.putExtra("searchById", searchById);
        PendingIntent cancelPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_MUTABLE);
        }

        Notification.Action cancelAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }
}
