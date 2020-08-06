package com.example.locus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    public void chatToHome(View view){
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(0,0);
    }

    public void chatToProfile(View view){
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