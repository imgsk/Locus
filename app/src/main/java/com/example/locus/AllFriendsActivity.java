package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class AllFriendsActivity extends AppCompatActivity {

    private RecyclerView allFriendsList;
    private DatabaseReference friendsRef, userRef;
    private FirebaseAuth fAuth;
    private String friendCurrentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friends);

        allFriendsList = findViewById(R.id.allFriendList);
        allFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        allFriendsList.setLayoutManager(linearLayoutManager);

        fAuth = FirebaseAuth.getInstance();
        friendCurrentID = fAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(friendCurrentID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        
        displayAllFriends();
    }

    private void displayAllFriends() {
        FirebaseRecyclerAdapter<searchUser,FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<searchUser, FriendsViewHolder>(
                        searchUser.class,
                        R.layout.search_user,
                        FriendsViewHolder.class,
                        friendsRef

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, searchUser searchUser, int i) {
                final String friendID = getRef(i).getKey();
                userRef.child(friendID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            final String username = snapshot.child("userName").getValue().toString();
                            final String name = snapshot.child("name").getValue().toString();
                            if (snapshot.child("profileImage").exists()){
                                final String profilepic = snapshot.child("profileImage").getValue().toString();
                                friendsViewHolder.setProfilePicture(getApplicationContext(),profilepic);
                            }

                            friendsViewHolder.setProfileName(name);
                            friendsViewHolder.setProfileUsername(username);
                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent friendsInten = new Intent(getApplicationContext(),RequestActivity.class);
                                    friendsInten.putExtra("SearchUser",friendID);
                                    startActivity(friendsInten);

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
        allFriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfilePicture(Context applicationContext, String profileImage){
            CircleImageView myImageView = mView.findViewById(R.id.findUserProfile);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImageView);
        }

        public void setProfileUsername(String username){
            TextView myUsername = mView.findViewById(R.id.findUserusername);
            myUsername.setText("@" + username);
        }

        public void setProfileName(String nameee){
            TextView myName = mView.findViewById(R.id.findUsername);
            myName.setText(nameee);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void allFriendsToProfile(View view){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}