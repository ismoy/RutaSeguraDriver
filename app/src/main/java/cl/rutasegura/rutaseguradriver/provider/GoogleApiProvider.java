package cl.rutasegura.rutaseguradriver.provider;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.retrofit.IGoogleApi;
import cl.rutasegura.rutaseguradriver.retrofit.RetrofitClient;
import retrofit2.Call;

public class GoogleApiProvider {

    private Context context;

    public GoogleApiProvider(Context context) {
        this.context = context;
    }

    public Call<String> getDirections(LatLng originLatLng, LatLng destinationLatLng) {
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                     + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                     + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                     + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
