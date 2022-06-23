package cl.rutasegura.rutaseguradriver.retrofit;


import cl.rutasegura.rutaseguradriver.Model.FCMBody;
import cl.rutasegura.rutaseguradriver.Model.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAuMewWU0:APA91bEbURsH0K7jIlVyYJhfB1nU3j6cketXtfcca_J5ZGyUfAi_AfmRi_21jPs0u_MggoIbRcpB7GUIJPk_mAaG4lub35Z8b3FuyumVimKJpAsx7UGtlXvNR_dN4nDKcS7ubNkifeo_"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
