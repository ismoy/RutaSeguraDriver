package cl.rutasegura.rutaseguradriver.activities.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.historybooking.HistoryBookingDriverActivity;
import cl.rutasegura.rutaseguradriver.activities.login.LoginActivity;
import cl.rutasegura.rutaseguradriver.activities.metricsdrivers.MetricsDriversActivity;
import cl.rutasegura.rutaseguradriver.databinding.ActivityMapsBinding;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.DriverProvider;
import cl.rutasegura.rutaseguradriver.provider.DriversFoundProvider;
import cl.rutasegura.rutaseguradriver.provider.GeofireProvider;
import cl.rutasegura.rutaseguradriver.provider.TokenProvider;
import cl.rutasegura.rutaseguradriver.utils.CarMoveAnim;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityMapsBinding binding;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private DriversFoundProvider mDriversFoundProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarker;
    private boolean mIsConnect = false;
    private LatLng mCurrentLatLng;
    private ValueEventListener mListener;
    private boolean mExtraConnect;
    SharedPreferences mPref;
    private GoogleApiClient mGoogleApiClient;
    private final int REQUEST_CHECK_SETTINGS = 0x1;
    private boolean mIsStartLocation = false;
    LatLng mStartLatLng;
    LatLng mEndLatLng;
    LocationManager mLocationManager;
    private Button btnconnect ;
    Toolbar toolbar;
    Dialog dialog;
    private int role;
    private DriverProvider mDriverProvider;

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

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
                    // SIGNIFICA QUE YA RECONOCIO LA UBICACION POR PRIMERA VEZ
                    if (!mIsStartLocation) {

                        mMap.clear();

                        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mIsStartLocation = true;
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(16f)
                                        .build()
                        ));
                        mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car))
                        );
                        updateLocation();

                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                2000,
                                10,
                                locationListenerGPS
                        );
                        stopLocation();
                    }

                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       initialization();

    }

    private void initialization() {
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mDriversFoundProvider = new DriversFoundProvider();
        mDriverProvider= new DriverProvider();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dialog = new Dialog(this);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        btnconnect = findViewById(R.id.btn_connect);
        toolbar = findViewById(R.id.toolbar);

        mExtraConnect = getIntent().getBooleanExtra("connect", false);

        binding.getRoot().findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsConnect) {
                    disconnect();
                } else {
                    startLocation();
                }
            }

        });

        mGoogleApiClient = getAPIClientInstance();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        mPref = getApplicationContext().getSharedPreferences("RideStatus", MODE_PRIVATE);
        String status = mPref.getString("status", "");
        String idClient = mPref.getString("idClient", "");

        if (status.equals("start") || status.equals("ride")) {
            goToMapDriverActivity(idClient);
        }
        else {
            generateToken();
            deleteDriverWorking();
            deleteDriverFound();
        }

        DrawerLayout drawer = binding.drawerLayout;
        toolbar=findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,
                R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        senDataInDrawable();
        toggle.syncState();
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.nav_incio:
                    break;
                case R.id.history:
                    startActivity(new Intent(MapsActivity.this, HistoryBookingDriverActivity.class));
                    break;
                    case  R.id.nav_logout:
                    logout();
                    break;
                case R.id.nav_metrics:
                    startActivity(new Intent(MapsActivity.this, MetricsDriversActivity.class));
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
       if (role!=2){
           ShowAlertDialog();
       }
    }

    private void goToMapDriverActivity(String idClient) {
        Intent intent = new Intent(MapsActivity.this, MapsDriverBookingActivity.class);
        intent.putExtra("idClient", idClient);
        startActivity(intent);
    }

    private void senDataInDrawable() {
        View mHeaderView = binding.navView.getHeaderView(0);
        TextView username = mHeaderView.findViewById(R.id.textviewnamedrwawerclient);
        TextView emails = mHeaderView.findViewById(R.id.textviewcorreodrawerclient);
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String firstname =snapshot.child("firstname").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    role = Integer.parseInt(snapshot.child("role").getValue().toString());
                    username.setText(firstname);
                    emails.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteDriverFound() {
        mDriversFoundProvider.delete(mAuthProvider.getId());
    }

    private void deleteDriverWorking() {
        mGeofireProvider.deleteDriverWorking(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isDriverWorking();
                if (mExtraConnect) {
                    startLocation();
                }
            }
        });
    }

    private void checkIfDriverIsActived() {
        mGeofireProvider.getDriverLocation(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    startLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void stopLocation() {
        if (mLocationCallback != null && mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        removeLocation();
        if (mListener != null) {
            if (mAuthProvider.existSession()) {
                mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
            }
        }
    }

    private void isDriverWorking() {
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    disconnect();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if (mCurrentLatLng != null) {
            /*
            Log.d("PRUEBA", "Si esta ingresando al metodo");
            Log.d("PRUEBA", "Id de la sesion: " + mAuthProvider.getId());
            Log.d("PRUEBA", "Lat:" + mCurrentLatLng.latitude);
            Log.d("PRUEBA", "Lat:" + mCurrentLatLng.longitude);

             */
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if (googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    private GoogleApiClient getAPIClientInstance() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        return  googleApiClient;
    }

    private void requestGPSSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(locationSettingsResult -> {
            Status status = locationSettingsResult.getStatus();

            if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                Toast.makeText(MapsActivity.this, "El GPS ya esta activado", Toast.LENGTH_SHORT).show();
            }
            else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(false);
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(MapsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
            else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                Toast.makeText(MapsActivity.this, "La configuracion del GPS tiene algun error o esta disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        checkIfDriverIsActived();
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
                        //showAlertDialogNOGPS();
                        requestGPSSettings();
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
        removeLocation();
        btnconnect.setText("Conectarse");
        mIsConnect = false;
        mIsStartLocation = false;
        //mFusedLocation.removeLocationUpdates(mLocationCallback);
        mGeofireProvider.removelocation(mAuthProvider.getId());
    }

    private void startLocation() {



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    btnconnect.setText("Desconectarse");
                    mIsConnect = true;
                }
                else {
                    //showAlertDialogNOGPS();
                    requestGPSSettings();
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
                //showAlertDialogNOGPS();
                requestGPSSettings();
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
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }


    void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }

    private void removeLocation() {
        if (locationListenerGPS != null) {
            mLocationManager.removeUpdates(locationListenerGPS);
        }
    }
    private void ShowAlertDialog(){
        dialog.setContentView(R.layout.alert_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Button btncontinue = dialog.findViewById(R.id.acceptreport);
        btncontinue.setOnClickListener(v -> {
            startActivity(new Intent(this,LoginActivity.class));
            dialog.dismiss();
            mAuthProvider.logout();
        });
        dialog.show();
    }

}