package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private EditText editName, editUserName, editBio;

    private DatabaseReference editRef;
    private FirebaseAuth editAuth;
    private String editCurrentUserID;

    private LoadindDialog loadindDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_details);

        editBio = findViewById(R.id.editdetailsBio);
        editUserName = findViewById(R.id.editdeatilsUserName);
        editName = findViewById(R.id.editdetailsName);

        loadindDialog = new LoadindDialog(EditPersonalDetailsActivity.this);

        editAuth = FirebaseAuth.getInstance();
        editCurrentUserID = editAuth.getCurrentUser().getUid();
        editRef = FirebaseDatabase.getInstance().getReference().child("Users").child(editCurrentUserID);

    }

    public void saveChanges(View view){
        loadindDialog.startLoading();
        String updateName = editName.getText().toString();
        String updateUserName = editUserName.getText().toString();
        String updateBio = editBio.getText().toString();

        if (updateBio.length() < 40 && updateBio.length() > 0){
            editRef.child("bio").setValue(updateBio);
        } else {
            Toast.makeText(this, "Bio should be less than 40 characters!", Toast.LENGTH_SHORT).show();
            loadindDialog.stopLoading();
            return;
        }

        if (!updateName.isEmpty()){
            editRef.child("name").setValue(updateName);
        }

        if (!updateUserName.isEmpty()){
            editRef.child("userName").setValue(updateUserName);
        }

        loadindDialog.stopLoading();
        Toast.makeText(this, "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }

    public void cancelChanges(View view){
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadindDialog.startLoading();
        editRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String namee = snapshot.child("name").getValue().toString();
                String bioo = snapshot.child("bio").getValue().toString();
                String userNamee = snapshot.child("userName").getValue().toString();

                editBio.setText(bioo);
                editName.setText(namee);
                editUserName.setText(userNamee);
                loadindDialog.stopLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}