package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText searchUsername;
    private CircleImageView searchButton;
    private RecyclerView searchRecyclerView;
    private ProgressBar progressBar;

    private DatabaseReference searchref;
    private LoadindDialog loadindDialog;
    private boolean RUNNING = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchButton = findViewById(R.id.searchUserButton);
        searchUsername = findViewById(R.id.searchUserName);
        progressBar = findViewById(R.id.searchProgressBar);
        searchRecyclerView = findViewById(R.id.searchUserRecyclerView);
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchref = FirebaseDatabase.getInstance().getReference().child("Users");
        loadindDialog = new LoadindDialog(SearchActivity.this);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String searchname = searchUsername.getText().toString();
                searchFriends(searchname);
            }
        });
    }

    private void searchFriends(final String searchInput) {
        searchRecyclerView.setVisibility(View.VISIBLE);

        Query finduserQuery = searchref.orderByChild("userName")
                .startAt(searchInput).endAt(searchInput + "\uf8ff");

        FirebaseRecyclerAdapter<searchUser, SearchFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<searchUser, SearchFriendsViewHolder>(
                        searchUser.class,
                        R.layout.search_user,
                        SearchFriendsViewHolder.class,
                        finduserQuery
        ) {
            @Override
            protected void populateViewHolder(SearchFriendsViewHolder searchFriendsViewHolder, searchUser searchUser, final int i) {
                progressBar.setVisibility(View.GONE);
                searchFriendsViewHolder.setProfileName(searchUser.getName());
                searchFriendsViewHolder.setProfilePicture(getApplicationContext(),searchUser.getProfileImage());
                searchFriendsViewHolder.setProfileUsername(searchUser.getUserName());
                searchFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String searchUserID = getRef(i).getKey();
                        Intent searchIntent = new Intent(getApplicationContext(),RequestActivity.class);
                        searchIntent.putExtra("SearchUser",searchUserID);
                        startActivity(searchIntent);
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                });
            }
        };
        searchRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class SearchFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public SearchFriendsViewHolder(@NonNull View itemView) {
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
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}