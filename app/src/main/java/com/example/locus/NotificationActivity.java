package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {
    
    private RecyclerView notificationList;
    private ProgressBar progressBar;
    
    private FirebaseAuth nAuth;
    private DatabaseReference notiRef, userRef;
    private String currentNotiID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationList = findViewById(R.id.notificationList);
        progressBar = findViewById(R.id.notificationProgressBar);
        notificationList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        notificationList.setLayoutManager(linearLayoutManager);
        
        nAuth = FirebaseAuth.getInstance();
        currentNotiID = nAuth.getCurrentUser().getUid();
        notiRef = FirebaseDatabase.getInstance().getReference().child("Received").child(currentNotiID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    displayNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void displayNotification() {
        progressBar.setVisibility(View.VISIBLE);
        notificationList.setVisibility(View.VISIBLE);
        FirebaseRecyclerAdapter<searchUser,NotificationViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<searchUser, NotificationViewHolder>(
                        searchUser.class,
                        R.layout.search_user,
                        NotificationViewHolder.class,
                        notiRef
        ) {
            @Override
            protected void populateViewHolder(final NotificationViewHolder notificationViewHolder, searchUser searchUser, int i) {
                progressBar.setVisibility(View.GONE);
                final String friendID = getRef(i).getKey();
                userRef.child(friendID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            final String username = "has sent you an friend request. Tap to open.";
                            final String name = snapshot.child("name").getValue().toString();
                            if (snapshot.child("profileImage").exists()){
                                final String profilepic = snapshot.child("profileImage").getValue().toString();
                                notificationViewHolder.setProfilePicture(getApplicationContext(),profilepic);
                            }
                            notificationViewHolder.setProfileName(name);
                            notificationViewHolder.setProfileUsername(username);
                            notificationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent friendsInten = new Intent(getApplicationContext(),RequestActivity.class);
                                    friendsInten.putExtra("SearchUser",friendID);
                                    startActivity(friendsInten);
                                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            };
        notificationList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfilePicture(Context applicationContext, String profileImage){
            CircleImageView myImageView = mView.findViewById(R.id.findUserProfile);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImageView);
        }

        public void setProfileUsername(String username){
            TextView myUsername = mView.findViewById(R.id.findUserusername);
            myUsername.setText(username);
        }

        public void setProfileName(String nameee){
            TextView myName = mView.findViewById(R.id.findUsername);
            myName.setText(nameee);
        }
    }

    public void notiToHome(View view){
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(0,0);
    }

    public void notiToProfile(View view){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(0,0);
    }
}