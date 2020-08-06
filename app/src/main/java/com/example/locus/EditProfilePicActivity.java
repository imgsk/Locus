package com.example.locus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfilePicActivity extends AppCompatActivity {

    private FirebaseAuth editAuth;
    private String editUserID;
    private DatabaseReference editDatabaseRef;
    private StorageReference editProfileRef;
    private LoadindDialog loadindDialog;

    private CircleImageView editProfilepic;

    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_pic);

        editProfilepic = findViewById(R.id.editProfileprofile);

        editAuth = FirebaseAuth.getInstance();
        editUserID = editAuth.getCurrentUser().getUid();
        editDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(editUserID);
        editProfileRef = FirebaseStorage.getInstance().getReference().child("profileImages");

        loadindDialog = new LoadindDialog(EditProfilePicActivity.this);
    }

    public void editImageView(View view){
        Intent getImage = new Intent();
        getImage.setAction(Intent.ACTION_GET_CONTENT);
        getImage.setType("image/*");
        startActivityForResult(getImage, GALLERY_PICK);
    }

    public void selectNewProfile(View view){
        Intent getImage = new Intent();
        getImage.setAction(Intent.ACTION_GET_CONTENT);
        getImage.setType("image/*");
        startActivityForResult(getImage, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null ){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            try{
                loadindDialog.startLoading();
                Uri resultUri = result.getUri();
                final StorageReference filePath = editProfileRef.child(editUserID + ".jpg");
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
                            editDatabaseRef.child("profileImage").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        loadindDialog.stopLoading();
                                        Intent setupIntent = new Intent(EditProfilePicActivity.this,ProfileActivity.class);
                                        startActivity(setupIntent);
                                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                        Toast.makeText(EditProfilePicActivity.this, "Profile edited successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadindDialog.stopLoading();
                                        String imgMsg = task.getException().getMessage();
                                    }
                                }
                            });
                        }
                    }
                });
            } catch (Exception e){
                loadindDialog.stopLoading();
                Toast.makeText(this, "ERROR: Please try again!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void removeProfile(View view){
        loadindDialog.startLoading();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(editUserID).child("profileImage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages").child(editUserID + ".jpg");
        storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadindDialog.stopLoading();
                            Toast.makeText(EditProfilePicActivity.this, "Profile picture removed successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                        }
                    });
                } else {
                    loadindDialog.stopLoading();
                    String error = task.getException().getMessage();
                    Toast.makeText(EditProfilePicActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cancelEditProfile(View view){
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadindDialog.startLoading();

        editDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("profileImage").exists()){
                    String image = snapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(editProfilepic);
                    loadindDialog.stopLoading();
                } else {
                    loadindDialog.stopLoading();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}