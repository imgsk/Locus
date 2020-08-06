package com.example.locus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FirebaseAuth loginAuth;
    private LoginDialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginPassword = findViewById(R.id.loginPassword);
        loginEmail = findViewById(R.id.loginEmail);

        loginDialog = new LoginDialog(MainActivity.this);

        loginAuth = FirebaseAuth.getInstance();

    }

    public void goToSignup(View view){
        startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void loginUser(View view){
        loginDialog.startDialog();
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter your password!", Toast.LENGTH_LONG).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter a valid email id", Toast.LENGTH_LONG).show();
        } else if (password.length() < 6){
            loginDialog.stopDialog();
            Toast.makeText(this, "Password too short", Toast.LENGTH_LONG).show();
        } else if (!isNetworkAvailable() && !checkWifiOnAndConnected()){
            loginDialog.stopDialog();
            Toast.makeText(this, "INTERNET not available, please check and try again", Toast.LENGTH_SHORT).show();
        }
        else {
            loginAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  if (task.isSuccessful()){
                      loginDialog.stopDialog();
                      Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                      startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                      overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                  } else {
                      loginDialog.stopDialog();
                      String msg = task.getException().getMessage();
                      Toast.makeText(MainActivity.this, ""+msg, Toast.LENGTH_SHORT).show();
                  }
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (loginAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        finishAffinity();
    }
}