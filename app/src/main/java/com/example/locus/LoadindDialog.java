package com.example.locus;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import java.lang.reflect.AccessibleObject;

public class LoadindDialog {
    private Activity activity;
    private AlertDialog loadingDialog;

    LoadindDialog(Activity myActivity){
        activity = myActivity;
    }

    void  startLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    void stopLoading(){
        loadingDialog.dismiss();
    }
}
