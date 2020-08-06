package com.example.locus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CreateProfileActivity extends AppCompatActivity {

    private Button selectImage, skip;
    private FirebaseAuth cpAuth;
    private DatabaseReference cpDatabaseRef;
    private StorageReference cpProfileRef;
    private String currentUserID;
    final static int GALLERY_PIC = 1;

    private LoginDialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        selectImage = findViewById(R.id.createProfileselectImage);
        skip = findViewById(R.id.createProfileSkip);

        loginDialog = new LoginDialog(CreateProfileActivity.this);

        cpAuth = FirebaseAuth.getInstance();
        currentUserID = cpAuth.getCurrentUser().getUid();
        cpDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        cpProfileRef = FirebaseStorage.getInstance().getReference().child("profileImages");

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImage = new Intent();
                getImage.setAction(Intent.ACTION_GET_CONTENT);
                getImage.setType("image/*");
                startActivityForResult(getImage, GALLERY_PIC);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loginDialog.startDialog();

        if (requestCode == GALLERY_PIC && resultCode == RESULT_OK && data != null ){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            try{
                Uri resultUri = result.getUri();
                final StorageReference filePath = cpProfileRef.child(currentUserID + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (! task.isSuccessful()){
                            throw  task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            final String imageUrl = downloadUri.toString();
                            cpDatabaseRef.child("profileImage").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        loginDialog.stopDialog();
                                        Intent setupIntent = new Intent(CreateProfileActivity.this,HomeActivity.class);
                                        startActivity(setupIntent);
                                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                        Toast.makeText(CreateProfileActivity.this, "Profile Created successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loginDialog.stopDialog();
                                        String imgMsg = task.getException().getMessage();
                                    }
                                }
                            });
                        }
                    }
                });
            } catch (Exception e){
                loginDialog.stopDialog();
                Toast.makeText(this, "ERROR: Please try again!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addProfilePic(View view){
        Intent getImage = new Intent();
        getImage.setAction(Intent.ACTION_GET_CONTENT);
        getImage.setType("image/*");
        startActivityForResult(getImage, GALLERY_PIC);
    }
}