package com.example.locus;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.locus.App.CHANNEL_ID;

public class JobServiceOne extends JobService {
    private static final String TAG = "JobServiceOne";
    private boolean JOB_CANCELLED = false;

    private DatabaseReference userRef, friendsRef;
    private String userID, activeUserID;

    private FusedLocationProviderClient client;
    private Double LATITUDE, LONGITUDE;

    private List<String> allActiveUserList = new ArrayList<>();

    private NotificationManagerCompat managerCompat;

    private GoogleMap googleMap;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started");
        client = LocationServices.getFusedLocationProviderClient(this);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(userID);
        managerCompat = NotificationManagerCompat.from(this);
        onCreate();
        backroundWorkOne(params);
        return true;
    }

    private void backroundWorkOne(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                everyThingFunction();
                jobFinished(params, false);
            }
        }).start();
    }

    private void everyThingFunction() {
        getUpdateLocation();
        allActiveUserList.clear();
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    activeUserID = ds.getKey();
                    allActiveUserList.add(activeUserID);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int size = allActiveUserList.size();
                        for (int i = 0; i < size; i++){
                            String id = allActiveUserList.get(i);
                            calculateFunction(id);
                            Log.d(TAG, "id sent" + id);
                        }

                        try {
                            Thread.sleep(1000*60*5);
                            everyThingFunction();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calculateFunction(final String friendID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        getUpdateLocation();

                        double latitude1, latitude2, longitude1, longitude2;
                        String friendName;
                        int status;

                        status = snapshot.child(friendID).child("status").getValue(int.class);

                        if (status == 1){
                            latitude1 = snapshot.child(userID).child("latitude").getValue(double.class);
                            longitude1 = snapshot.child(userID).child("longitude").getValue(double.class);
                            latitude2 = snapshot.child(friendID).child("latitude").getValue(double.class);
                            longitude2 = snapshot.child(friendID).child("longitude").getValue(double.class);

                            Log.d(TAG, "Someone is online: " + friendID);
                            
                            friendName = snapshot.child(friendID).child("name").getValue().toString();
                            double dist = distance(latitude1,latitude2,longitude1,longitude2,0.0,0.0);
                            int distance = (int) dist;
                            if (distance < 8){
                                sendNotificationFunction(friendName,friendID,distance);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).start();
    }

    private void sendNotificationFunction(String name, String id, int distance) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("ID",id);

        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("LOCUS")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.locuslogo)
                .setContentText( "Your friend "+name +" is nearby. Tap to contact!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        managerCompat.notify(1,notification);
        Log.d(TAG, "sendNotificationFunction: ");
    }

    private void getUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (location != null){
                            LATITUDE = location.getLatitude();
                            LONGITUDE = location.getLongitude();
                            userRef.child(userID).child("latitude").setValue(LATITUDE);
                            userRef.child(userID).child("longitude").setValue(LONGITUDE);
                        }
                    }
                }).start();
            }
        });
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c ;

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job Cancelled");
        JOB_CANCELLED = true;
        return true;
    }
}
