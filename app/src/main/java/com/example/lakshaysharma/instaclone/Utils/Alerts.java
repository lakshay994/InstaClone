package com.example.lakshaysharma.instaclone.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

public class Alerts {

    private static final String TAG = "Alerts";

    private Context mContext;

    public Alerts(Context mContext) {

        this.mContext = mContext;

    }

    public void setAlert(String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: Alert Dialog OK" + mContext);
            }
        });
        builder.create().show();
    }

    public void makeToast(String message){

        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void makeShortToast(String message){

        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
