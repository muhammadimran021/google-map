package com.example.muhammadimran.androidservices;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.muhammadimran.androidservices.R.id.latitude;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private Location mLaLocation;
    public double Latitude, Longitude;
    TextView Lat, Long, Time_update;
    private LocationRequest mLocationRequest;
    private GoogleMap mgoogleMap;
    Marker newMarker, newMa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Lat = (TextView) findViewById(latitude);
        Long = (TextView) findViewById(R.id.longitude);
        Time_update = (TextView) findViewById(R.id.time_update);

        try {
            LaunchMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        buildGoogleApiClient();

    }


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(getBaseContext(), MyService.class));
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    //for launch map
    public void LaunchMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLaLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLaLocation != null) {
            Latitude = mLaLocation.getLatitude();
            Longitude = mLaLocation.getLongitude();
            Toast.makeText(this, "Latitude: " + mLaLocation.getLatitude() + " \n" + "Longitude: " + mLaLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            CameraPosition googlePlace = CameraPosition.builder()
                    .target(new LatLng(Latitude, Longitude))
                    .zoom(16)
                    .bearing(0)
                    .tilt(45)
                    .build();

            mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlace));
        }

        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (newMarker != null) {
            newMarker.remove();

        }

        mLaLocation = location;
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();


        Calendar cal = Calendar.getInstance();
        Lat.setText("Latitude: " + Latitude);
        Long.setText("Longitude: " + Longitude);
        Time_update.setText("Time: " + cal.getTime());


        //for address

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(Latitude, Longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0);
        newMarker = mgoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(Latitude, Longitude))
                .title(address));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;
        mgoogleMap.setMyLocationEnabled(true);
        mgoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


    }

    //onMapSearch
    public void onMapSearch(View view) {


        EditText locationSearch = (EditText) findViewById(R.id.search);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            if (newMa != null) {
                newMa.remove();
            }
            newMa = mgoogleMap.addMarker(new MarkerOptions().position(latLng).title(location));
            mgoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

}
