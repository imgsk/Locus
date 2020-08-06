package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestActivity extends AppCompatActivity {

    private CircleImageView addfriendProfile;
    private TextView addFriendName, addFriendUserName, addFriendBio, addFriendtext;
    private Button addFriendsList, addFriendsNo, addFriendSEND, addFriendUNSEND, accept, reject, unfriend;

    private DatabaseReference addFriendsRef, sentRef, receiveREF, friendListRef, mainREF;
    private FirebaseAuth addFriendsAuth;
    private String currentID, receiveID;
    private LoadindDialog loadindDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        loadindDialog = new LoadindDialog(RequestActivity.this);
        loadindDialog.startLoading();

        addFriendBio = findViewById(R.id.addFriendsBio);
        addFriendName = findViewById(R.id.addFriendsFullName);
        addFriendUserName = findViewById(R.id.addFriendsUserName);
        addFriendtext = findViewById(R.id.addFriendText);

        addfriendProfile = findViewById(R.id.addFriendProfile);

        addFriendsList = findViewById(R.id.addFriendsFriends);
        addFriendsNo = findViewById(R.id.addFriendsNoofFriends);
        addFriendSEND = findViewById(R.id.addFriendsSEND);
        addFriendUNSEND = findViewById(R.id.addFriendsUNSEND);
        accept = findViewById(R.id.addFriendACCEPTbtn);
        reject = findViewById(R.id.addFriendREJECTbtn);
        unfriend = findViewById(R.id.unfriendBtn);

        addFriendsAuth = FirebaseAuth.getInstance();
        currentID = addFriendsAuth.getCurrentUser().getUid();
        receiveID = getIntent().getExtras().get("SearchUser").toString();
        addFriendsRef = FirebaseDatabase.getInstance().getReference().child("Users");
        sentRef = FirebaseDatabase.getInstance().getReference().child("Sent");
        receiveREF = FirebaseDatabase.getInstance().getReference().child("Received");
        friendListRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mainREF = FirebaseDatabase.getInstance().getReference();

        displayFunction();
        maintainButtons();
        setFriends();

        loadindDialog.stopLoading();


        addFriendSEND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        addFriendUNSEND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsendRequest();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectRequest();
            }
        });

        unfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfriendRequest();
            }
        });
    }

    private void confirmDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("UNFRIEND CONFORMATION");
        builder.setMessage("Are you sure you want to unfriend??");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                unfriendRequest();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void setFriends() {
        friendListRef.child(receiveID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long friends = snapshot.getChildrenCount();
                String list = String.valueOf(friends);
                addFriendsNo.setText(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void unfriendRequest() {
        loadindDialog.startLoading();
        friendListRef.child(currentID).child(receiveID).removeValue();
        friendListRef.child(receiveID).child(currentID).removeValue();
        unfriend.setVisibility(View.GONE);
        addFriendSEND.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Unfriended successfully!", Toast.LENGTH_SHORT).show();
        loadindDialog.stopLoading();
    }

    private void rejectRequest() {
        loadindDialog.startLoading();
        receiveREF.child(currentID).child(receiveID).removeValue();
        sentRef.child(receiveID).child(currentID).removeValue();
        accept.setVisibility(View.GONE);
        reject.setVisibility(View.GONE);
        addFriendtext.setVisibility(View.GONE);
        addFriendSEND.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Request Rejected successfully!", Toast.LENGTH_SHORT).show();
        loadindDialog.stopLoading();
    }

    private void acceptRequest() {
        loadindDialog.startLoading();
        friendListRef.child(currentID).child(receiveID).child("friends").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()){
                   friendListRef.child(receiveID).child(currentID).child("friends").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               receiveREF.child(currentID).child(receiveID).removeValue();
                               sentRef.child(receiveID).child(currentID).removeValue();
                               accept.setVisibility(View.GONE);
                               reject.setVisibility(View.GONE);
                               addFriendtext.setVisibility(View.GONE);
                               unfriend.setVisibility(View.VISIBLE);
                               loadindDialog.stopLoading();
                               Toast.makeText(RequestActivity.this, "Request accepted successfully!", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
               }
            }
        });
    }

    private void unsendRequest() {
        loadindDialog.startLoading();
        sentRef.child(currentID).child(receiveID).removeValue();
        receiveREF.child(receiveID).child(currentID).removeValue();
        addFriendUNSEND.setVisibility(View.GONE);
        addFriendSEND.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Request Unsent successfully!", Toast.LENGTH_SHORT).show();
        loadindDialog.stopLoading();
    }

    private void sendRequest() {
        loadindDialog.startLoading();
        sentRef.child(currentID).child(receiveID).child("type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    receiveREF.child(receiveID).child(currentID).child("type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            addFriendUNSEND.setVisibility(View.VISIBLE);
                            addFriendSEND.setVisibility(View.GONE);
                            loadindDialog.stopLoading();
                            Toast.makeText(RequestActivity.this, "Request sent successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void displayFunction() {
        addFriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(receiveID).exists()){
                    String addName, addBio, addUserName, addProfile;
                    addName = snapshot.child(receiveID).child("name").getValue().toString();
                    addUserName = snapshot.child(receiveID).child("userName").getValue().toString();
                    addBio = snapshot.child(receiveID).child("bio").getValue().toString();

                    addFriendBio.setText(addBio);
                    addFriendName.setText(addName);
                    addFriendUserName.setText("@"+addUserName);
                }

                if (snapshot.child(receiveID).child("profileImage").exists()) {
                    String image = snapshot.child(receiveID).child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(addfriendProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void maintainButtons() {
        mainREF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if already friends
                if (snapshot.child("Friends").child(currentID).child(receiveID).exists()){
                    unfriend.setVisibility(View.VISIBLE);
                }

                //if request is not sent
                if (!snapshot.child("Received").child(currentID).child(receiveID).exists()){
                    if (!snapshot.child("Sent").child(currentID).child(receiveID).exists()){
                        addFriendSEND.setVisibility(View.VISIBLE);
                    }
                }

                //if request is sent
                if (snapshot.child("Sent").child(currentID).child(receiveID).exists()){
                    addFriendUNSEND.setVisibility(View.VISIBLE);
                }

                //if request is received
                if (snapshot.child("Received").child(currentID).child(receiveID).exists()){
                    accept.setVisibility(View.VISIBLE);
                    reject.setVisibility(View.VISIBLE);
                    addFriendtext.setVisibility(View.VISIBLE);
                }

                //if same user
                if (currentID.equals(receiveID)){
                    accept.setVisibility(View.GONE);
                    reject.setVisibility(View.GONE);
                    addFriendtext.setVisibility(View.GONE);
                    unfriend.setVisibility(View.GONE);
                    addFriendSEND.setVisibility(View.GONE);
                    addFriendUNSEND.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}