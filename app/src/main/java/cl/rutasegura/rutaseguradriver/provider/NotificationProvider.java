package cl.rutasegura.rutaseguradriver.provider;
import cl.rutasegura.rutaseguradriver.Model.FCMBody;
import cl.rutasegura.rutaseguradriver.Model.FCMResponse;
import cl.rutasegura.rutaseguradriver.retrofit.IFCMApi;
import cl.rutasegura.rutaseguradriver.retrofit.RetrofitClient;
import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }
    
    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
