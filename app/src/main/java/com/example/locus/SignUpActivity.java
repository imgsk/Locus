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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText suName, suUserName, suPassword, suConfirmPassword, suEmail;
    private DatabaseReference signUpReference;
    private FirebaseAuth signUpAuth;
    private LoginDialog loginDialog;
    String signUpUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        suConfirmPassword = findViewById(R.id.signupConfirmPassword);
        suName = findViewById(R.id.signupName);
        suPassword = findViewById(R.id.signupPassword);
        suUserName = findViewById(R.id.signupUserName);
        suEmail = findViewById(R.id.signupEmail);

        loginDialog = new LoginDialog(SignUpActivity.this);

        signUpAuth = FirebaseAuth.getInstance();
        signUpReference = FirebaseDatabase.getInstance().getReference("Users");

    }

    public void goToLogin(View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public void signupUser(View view){
        loginDialog.startDialog();
        final String email = suEmail.getText().toString();
        String password = suPassword.getText().toString();
        final String name = suName.getText().toString();
        final String username = suUserName.getText().toString();
        String confirmpassword = suConfirmPassword.getText().toString();
        final int Status = 0;
        final double Lat = 0;
        final double Lang = 0;
        final String Bio = "Your one line bio goes here!!";

        if (TextUtils.isEmpty(email)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Email cannot be empty!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            loginDialog.stopDialog();
            Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(confirmpassword)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_LONG).show();
        } else if (!password.equals(confirmpassword)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Passwords does not match!", Toast.LENGTH_LONG).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter a valid email id!", Toast.LENGTH_LONG).show();
        } else if (password.length() < 6){
            loginDialog.stopDialog();
            Toast.makeText(this, "Password should be 6 character or more!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(name)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(username)){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please enter your userName", Toast.LENGTH_SHORT).show();
        } else if (!isNetworkAvailable() && !checkWifiOnAndConnected()){
            loginDialog.stopDialog();
            Toast.makeText(this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
        } else{
            signUpAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        HashMap userMap = new HashMap();
                        userMap.put("name",name);
                        userMap.put("userName",username);
                        userMap.put("latitude",Lat);
                        userMap.put("longitude",Lang);
                        userMap.put("status",Status);
                        userMap.put("bio",Bio);

                        signUpUserID = signUpAuth.getCurrentUser().getUid();

                        signUpReference.child(signUpUserID).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    loginDialog.stopDialog();
                                    Toast.makeText(SignUpActivity.this, "Sign up Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),CreateProfileActivity.class));
                                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                } else {
                                    loginDialog.stopDialog();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SignUpActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    } else {
                        loginDialog.stopDialog();
                        String msg = task.getException().getMessage();
                        Toast.makeText(SignUpActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}