package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileUserName, profileBio;
    private Button profileFriends, profileNoofFriends;
    private ImageView profileImage;

    private LoadindDialog loadindDialog;

    private DatabaseReference profileReference, listRef;
    private StorageReference profileStorage;
    private FirebaseAuth profileAuth;
    private String profileUserID;

    private RecyclerView suggestionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileProfile);
        profileBio = findViewById(R.id.profileBio);
        profileName = findViewById(R.id.profileFullName);
        profileUserName = findViewById(R.id.profileUserName);
        profileFriends = findViewById(R.id.profileFriends);
        profileNoofFriends = findViewById(R.id.profileNoofFriends);

        suggestionList = findViewById(R.id.suggestionsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        suggestionList.setLayoutManager(linearLayoutManager);

        loadindDialog = new LoadindDialog(ProfileActivity.this);

        profileAuth = FirebaseAuth.getInstance();
        profileUserID = profileAuth.getCurrentUser().getUid();
        profileReference = FirebaseDatabase.getInstance().getReference().child("Users").child(profileUserID);
        listRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(profileUserID);

        profileFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AllFriendsActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

    }

    public void suggestions(){
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadindDialog.startLoading();
        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("profileImage").exists()){
                    String image = snapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                }

                String userFullName = snapshot.child("name").getValue().toString();
                String userUserName = snapshot.child("userName").getValue().toString();
                String userBio = snapshot.child("bio").getValue().toString();

                profileBio.setText(userBio);
                profileName.setText(userFullName);
                profileUserName.setText("@"+userUserName);

                profileUserName.setVisibility(View.VISIBLE);
                profileName.setVisibility(View.VISIBLE);
                profileImage.setVisibility(View.VISIBLE);
                profileBio.setVisibility(View.VISIBLE);
                profileFriends.setVisibility(View.VISIBLE);
                profileNoofFriends.setVisibility(View.VISIBLE);
                setFriends();
                loadindDialog.stopLoading();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setFriends() {
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long friends = snapshot.getChildrenCount();
                String list = String.valueOf(friends);
                profileNoofFriends.setText(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void goToNoti(View view){
        startActivity(new Intent(getApplicationContext(),NotificationActivity.class));
        overridePendingTransition(0,0);
    }

    public void goToHome(View view){
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(0,0);
    }

    public void profileToSettings(View view){
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(0,0);
    }
}