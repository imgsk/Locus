package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomeActivity";

    private CardView cardView;
    private ProgressBar homeBar;

    private TextView activeTV, statusTV;
    private FloatingActionButton refresh;
    private Button goOnline, goOffline;

    private DatabaseReference usersRef, friendsListRef;
    private FirebaseAuth hAuth;
    private String currentUSERID;

    GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Double lat, lang;

    private String ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        goOffline = findViewById(R.id.goOffline);
        goOnline = findViewById(R.id.goOnline);
        activeTV = findViewById(R.id.activeTextView);
        statusTV = findViewById(R.id.statusTextView);
        refresh = findViewById(R.id.refreshButton);
        cardView = findViewById(R.id.cardView);
        homeBar = findViewById(R.id.homeprogressbar);

        hAuth = FirebaseAuth.getInstance();
        currentUSERID = hAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsListRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUSERID);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        supportMapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshFunction();
            }
        });

        goOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOnline();
            }
        });

        goOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOffline.setVisibility(View.GONE);
                goOnline.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.GONE);
                statusTV.setText("STATUS: Offline");
                offlineFunction();
            }
        });

    }

    private void goOnline(){
        friendsListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    checkPermissions();
                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("No Friends");
                    builder.setMessage("Sorry but you have zero friends, please add friends to get notified when they come nearby. Thank you!");
                    builder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void onlineFunction() {
        goOnline.setVisibility(View.GONE);
        goOffline.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.VISIBLE);
        statusTV.setText("STATUS: Online");
        getUpdateLocation();
        onMapReady(map);
        usersRef.child(currentUSERID).child("status").setValue(1);
        Toast.makeText(this, "Going ONLINE", Toast.LENGTH_SHORT).show();
        startJobOne();
    }

    private void offlineFunction() {
        Toast.makeText(this, "Going OFFLINE", Toast.LENGTH_SHORT).show();
        usersRef.child(currentUSERID).child("status").setValue(0);
        stopJobOne();
    }

    private void startJobOne() {
        ComponentName componentName = new ComponentName(this, JobServiceOne.class);
        JobInfo info = new JobInfo.Builder(111, componentName)
                .setPersisted(true)
                .setPeriodic(15*60*1000)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job Scheduled");
        } else {
            Log.d(TAG, "Failed");
        }
    }

    private void stopJobOne() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(111);
        Log.d(TAG, "Job Cancelled");
    }

    private void refreshFunction() {
        goOnline();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final List<String> markerList = new ArrayList<>();
        map = googleMap;
        map.clear();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lat = snapshot.child(currentUSERID).child("latitude").getValue().toString();
                String log = snapshot.child(currentUSERID).child("longitude").getValue().toString();

                Double latitude = Double.parseDouble(lat);
                Double longitude = Double.parseDouble(log);

                LatLng currentUserPos = new LatLng(latitude,longitude);
                map.addMarker(new MarkerOptions().position(currentUserPos).title("ME"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPos,15));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(HomeActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            checkLocationInternet();
        } else {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    private void checkLocationInternet() {
        if (isNetworkAvailable() || checkWifiOnAndConnected()){
            if (isLocationEnabled(getApplicationContext())){
                onlineFunction();
            } else {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("LOCATION NOT AVAILABLE");
                builder.setMessage("Please check if your location is turned on and try again.");
                builder.create().show();
            }
        } else {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("INTERNET NOT AVAILABLE");
            builder.setMessage("Please check your internet connection and try again.");
            builder.create().show();
        }
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    private void getUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    lat = location.getLatitude();
                    lang = location.getLongitude();
                    usersRef.child(currentUSERID).child("latitude").setValue(lat);
                    usersRef.child(currentUSERID).child("longitude").setValue(lang);
                }
            }
        });
    }

    public void homeToProfile(View view){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(0,0);
    }

    public void homeToNoti(View view){
        startActivity(new Intent(getApplicationContext(),NotificationActivity.class));
        overridePendingTransition(0,0);
    }

    public void homeToSearch(View view){
        startActivity(new Intent(getApplicationContext(),SearchActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        finishAffinity();
    }

    @Override
    protected void onStart() {
        homeBar.setVisibility(View.VISIBLE);
        super.onStart();
        if(hAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }

        usersRef.child(currentUSERID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int statusCU = snapshot.child("status").getValue(int.class);
                if (statusCU == 1){
                    homeBar.setVisibility(View.GONE);
                    cardView.setVisibility(View.VISIBLE);
                    statusTV.setText("STATUS: Online");
                    statusTV.setVisibility(View.VISIBLE);
                    goOffline.setVisibility(View.VISIBLE);
                    refresh.setVisibility(View.VISIBLE);
                    goOnline();
                } else {
                    homeBar.setVisibility(View.GONE);
                    cardView.setVisibility(View.VISIBLE);
                    statusTV.setText("STATUS: Offline");
                    statusTV.setVisibility(View.VISIBLE);
                    goOnline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}