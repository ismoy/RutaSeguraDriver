package cl.rutasegura.rutaseguradriver.activities.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.rutasegura.rutaseguradriver.Model.ClientProvider;
import cl.rutasegura.rutaseguradriver.Model.FCMBody;
import cl.rutasegura.rutaseguradriver.Model.FCMResponse;
import cl.rutasegura.rutaseguradriver.Model.Info;
import cl.rutasegura.rutaseguradriver.Model.CreatePromoteQualification;
import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.calificationclient.CalificationClientActivity;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.ClientBookingProvider;
import cl.rutasegura.rutaseguradriver.provider.GeofireProvider;
import cl.rutasegura.rutaseguradriver.provider.GoogleApiProvider;
import cl.rutasegura.rutaseguradriver.provider.InfoProvider;
import cl.rutasegura.rutaseguradriver.provider.NotificationProvider;
import cl.rutasegura.rutaseguradriver.provider.CreatePromoteQualificationProvider;
import cl.rutasegura.rutaseguradriver.provider.TokenProvider;
import cl.rutasegura.rutaseguradriver.services.ForegroundService;
import cl.rutasegura.rutaseguradriver.utils.CarMoveAnim;
import cl.rutasegura.rutaseguradriver.utils.DecodePoints;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private TokenProvider mTokenProvider;
    private NotificationProvider mNotificationProvider;
    private CreatePromoteQualificationProvider mCreatePromoteQualificationProvider;
    private CreatePromoteQualification mResumeTrips;


    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;
    private LatLng mCurrentLatLng;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewTime;
    private ImageView mImageViewBooking;

    private String mExtraClientId;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;
    private InfoProvider mInfoProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private Info mInfo;

    private boolean mIsFirstTime = true;
    private boolean mIsCloseToClient = false;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;
    String distanceText;
    String durationText;
    double mDistanceInMeters = 1;
    int mMinutes = 0;
    int mSeconds = 0;
    boolean mSecondIsOver = false;
    boolean mRideStart = false;
    Handler mHandler = new Handler();
    Location mPreviusLocation = new Location("");

    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;

    boolean mIsFinishBooking = false;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;

            if (!mSecondIsOver) {
                mTextViewTime.setText(mSeconds + " Seg");
            } else {
                mTextViewTime.setText(mMinutes + " Min " + mSeconds);
            }

            if (mSeconds == 59) {
                mSeconds = 0;
                mSecondIsOver = true;
                mMinutes++;
            }

            mHandler.postDelayed(runnable, 1000);
        }
    };

    private boolean mIsStartLocation = false;
    LatLng mStartLatLng;
    LatLng mEndLatLng;
    LocationManager mLocationManager;


    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (mRideStart) {
                mDistanceInMeters = mDistanceInMeters + mPreviusLocation.distanceTo(location);
            }

            mPreviusLocation = location;

            if (mStartLatLng != null) {
                mEndLatLng = mStartLatLng;
            }

            mStartLatLng = new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude);

            if (mEndLatLng != null) {
                CarMoveAnim.carAnim(mMarker, mEndLatLng, mStartLatLng);
            }

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(18f)
                            .build()
            ));

            updateLocation();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!mIsStartLocation) {
                        mMap.clear();

                        mMarker = mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(location.getLatitude(), location.getLongitude()))
                                .title("Tu posicion")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car))
                        );
                        // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(16f)
                                        .build()
                        ));

                        updateLocation();

                        if (mIsFirstTime) {
                            mIsFirstTime = false;
                            getClientBooking();
                        }
                        mIsStartLocation = true;
                        if (ActivityCompat.checkSelfPermission(MapsDriverBookingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsDriverBookingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListenerGPS);

                        stopLocation();
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mNotificationProvider = new NotificationProvider();
        mInfoProvider = new InfoProvider();
        mCreatePromoteQualificationProvider = new CreatePromoteQualificationProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewClientBooking = findViewById(R.id.textViewClientBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        mButtonStartBooking = findViewById(R.id.btnStartBooking);
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking);
        mImageViewBooking = findViewById(R.id.imageViewClientBooking);
        mTextViewTime = findViewById(R.id.textViewTime);

        getInfo();

        mExtraClientId = getIntent().getStringExtra("idClient");
        mGoogleApiProvider = new GoogleApiProvider(MapsDriverBookingActivity.this);

        getClient();

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCloseToClient) {
                    startBooking();
                } else {
                    Toast.makeText(MapsDriverBookingActivity.this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });

    }

    private void removeLocation() {
        if (locationListenerGPS != null) {
            mLocationManager.removeUpdates(locationListenerGPS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocation();
        stopLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mIsFinishBooking) {
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
    }

    private void stopLocation() {
        if (mLocationCallback != null && mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
    }

    private void startService() {
        stopLocation();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(MapsDriverBookingActivity.this, serviceIntent);
    }

    private void stopService() {
        startLocation();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private void calculateRide() {
        if (mMinutes == 0) {
            mMinutes = 1;
        }
        double priceMin = mMinutes * mInfo.getMin();
        double priceKm = (mDistanceInMeters / 1000) * mInfo.getKm();

        final double total = priceMin + priceKm;
        mClientBookingProvider.updatePrice(mExtraClientId, total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mClientBookingProvider.updateStatus(mExtraClientId, "finish");
                Intent intent = new Intent(MapsDriverBookingActivity.this, CalificationClientActivity.class);
                intent.putExtra("idClient", mExtraClientId);
                intent.putExtra("price", total);
                intent.putExtra("distancetext",distanceText);
                intent.putExtra("durationtext",durationText);
                startActivity(intent);

                finish();
            }
        });
    }


    private void getInfo() {
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mInfo = dataSnapshot.getValue(Info.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void finishBooking() {
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // podemos limpiar todos los datos almacenados en shared preferences
                mIsFinishBooking = true;
                mEditor.clear().commit();
                sendNotification("Viaje finalizado");
                removeLocation();
                stopLocation();
                mGeofireProvider.removelocation(mAuthProvider.getId());
                if (mHandler != null) {
                    mHandler.removeCallbacks(runnable);
                }
                calculateRide();
            }
        });

    }

    private void startBooking() {
        mEditor.putString("status", "start");
        mEditor.putString("idClient", mExtraClientId);
        mEditor.apply();

        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        if (mCurrentLatLng != null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude))
                    .title("Tu posicion")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car))
            );
        }

        drawRoute(mDestinationLatLng);
        sendNotification("Viaje iniciado");
        mRideStart = true;
        mHandler.postDelayed(runnable, 1000);
    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        return distance;
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    mTextViewOriginClientBooking.setText("recoger en: " + origin);
                    mTextViewDestinationClientBooking.setText("destino: " + destination);

                    mPref = getApplicationContext().getSharedPreferences("RideStatus", MODE_PRIVATE);
                    mEditor = mPref.edit();
                    // ES OBTENER EL ULTIMO ESTADO ALMACENADO EN EL SHARED PREFERENCE
                    String status = mPref.getString("status", "");


                    if (status.equals("start")) {
                        startBooking();
                    }
                    else {
                        // Este valor se almacena cuando el conductor inicia el viaje por primera vez
                        mEditor.putString("status", "ride");
                        mEditor.putString("idClient", mExtraClientId);
                        mEditor.apply();
                        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                        drawRoute(mOriginLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void drawRoute(final LatLng latLng) {
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                     distanceText = distance.getString("text");
                     durationText = duration.getString("text");


                } catch (Exception e) {
                    // AQUI DEBES TENER EL TOAST PARA VERIFICAR EL ERROR
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    String name = dataSnapshot.child("firstname").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(MapsDriverBookingActivity.this).load(image).into(mImageViewBooking);
                    }
                    mTextViewClientBooking.setText(name);
                    mTextViewEmailClientBooking.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); // METROS
                    if (distance <= 200) {
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void disconnect() {

        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removelocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }


        }
    }

    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("token")) {

                        String token = dataSnapshot.child("token").getValue().toString();

                        Map<String, String> map = new HashMap<>();
                        map.put("title", "ESTADO DE TU VIAJE");
                        map.put("body",
                                "Tu estado del viaje es: " + status
                        );
                        FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                        mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                if (response.body() != null) {
                                    if (response.body().getSuccess() != 1) {
                                        Toast.makeText(MapsDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    Toast.makeText(MapsDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                Log.d("Error", "Error " + t.getMessage());
                            }
                        });
                    }
                    else {
                        Toast.makeText(MapsDriverBookingActivity.this, "El cliente no tiene token de notificaciones", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MapsDriverBookingActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
