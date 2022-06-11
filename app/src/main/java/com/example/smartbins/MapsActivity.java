package com.example.smartbins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    NavigationView navigationView;
    DrawerLayout drawer;
    Button menubtn;
    TextView name;
    LocationRequest locationRequest;
    Button request;
    EditText searchEntry;
    Marker locationMarker;
    private int count = 0;
    String username = " ";
    ImageView search;
    String nearby = "dustbin";
    private double latitude, longitude;
    String url;
    Object transferData[] = new Object[2];
    private int proximityRadius = 1000;

    MarkerOptions userMarkerOptions = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        searchEntry = findViewById(R.id.i_search);

        username = getIntent().getStringExtra("USERNAME");

        request = findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // search nearby dustbins
                url = getUrl(latitude, longitude, nearby);
                transferData[0] = mMap;
                transferData[1] = url;

                Toast.makeText(MapsActivity.this, "Showing nearby Dustbins...", Toast.LENGTH_SHORT).show();
                (new Handler(Looper.getMainLooper())).postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        ArrayList<Pair<Double, Double>> locationArrayList = new ArrayList<>();
                        locationArrayList.add(new Pair<>(28.620193456112457, 77.07167208194734));
                        locationArrayList.add(new Pair<>(28.62316006049698, 77.07201004028322));
                        locationArrayList.add(new Pair<>(28.62283985961478, 77.06870555877687));
                        locationArrayList.add(new Pair<>(28.622934036446203, 77.06651687622072));
                        locationArrayList.add(new Pair<>(28.62089979809256, 77.06602334976196));
                        locationArrayList.add(new Pair<>(28.62195459326995, 77.06379175186159));
                        locationArrayList.add(new Pair<>(28.620504247169105, 77.06668853759767));
                        locationArrayList.add(new Pair<>(28.619336421462958, 77.06982135772706));

                        for (Pair<Double, Double> item : locationArrayList) {
                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(new LatLng(item.first, item.second))
                                            .title("Dustbin ")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            );
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.619336421462958, 77.06982135772706), 15f));
                    }
                }, 3000);
                request.setClickable(false);
            }
        });

        search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String string = searchEntry.getText().toString().trim();
                List<Address> addressList = null;

                if(!TextUtils.isEmpty(string)) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());

                    try {
                        addressList = geocoder.getFromLocationName(string, 6);

                        if(addressList != null) {

                            for(int i=0; i<addressList.size(); i++) {
                                Address userAddress = addressList.get(i);
                                LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());

                                userMarkerOptions.position(latLng);
                                userMarkerOptions.title(string);
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                mMap.addMarker(userMarkerOptions);

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomBy(16));
                                mMap.getUiSettings().setRotateGesturesEnabled(false);

                            }
                        }
                        else {
                            Toast.makeText(MapsActivity.this, "Location not found...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    Toast.makeText(MapsActivity.this, "Please enter a location..", Toast.LENGTH_SHORT).show();
                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Navigation drawer

        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        menubtn = findViewById(R.id.menu2);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });

        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        name.setText(username);

        navigationView.setNavigationItemSelectedListener(this);

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }




    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        lastLocation = location;

        if(locationMarker != null) {
            locationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        locationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        if(googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }



    @Override
    public void onConnectionSuspended(int i) {

    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onResume() {
        count = 0;
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            if(count==1){
                finishAffinity();
            } else
                Toast.makeText(MapsActivity.this,"Press BACK again to exit", Toast.LENGTH_SHORT).show();
            count++;
        }
    }


    public void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();
        if (id == R.id.nav_logout) {
            new AlertDialog.Builder(MapsActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onStop();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MapsActivity.this, MainActivity.class));
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getUrl(double latitude, double longitude, String nearby) {
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location=" + latitude + "," + longitude);
        googleUrl.append("&radius=" + proximityRadius);
        googleUrl.append("&type=" + nearby);
        googleUrl.append("&sensor=true");
        googleUrl.append("&key=" + "AIzaSyCDq46OsSy1sVZxD8UJMA8XK40are-RdUY");

        return googleUrl.toString();
    }
}
