package com.example.locus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    private Button goToLogout, editPersonalDetails, editProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        goToLogout = findViewById(R.id.settingesLogout);
        editPersonalDetails = findViewById(R.id.settingeditPersonalDetails);
        editProfilePic = findViewById(R.id.settingeditPersonalpicture);

        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),EditProfilePicActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        editPersonalDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),EditPersonalDetailsActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        goToLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignOutActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}