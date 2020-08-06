package com.example.locus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class SignOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_out);
    }

    public void logoutUser(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void cancelLogout(View view){
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}