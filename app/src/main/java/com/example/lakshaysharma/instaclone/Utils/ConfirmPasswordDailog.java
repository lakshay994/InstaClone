package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.R;

public class ConfirmPasswordDailog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDailog";

    public interface OnConfirmPasswordListener{

        public void onConfirmPasswordListener(String password);

    }

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    private TextView mPassword;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        Log.d(TAG, "onCreateView: Started");

        TextView confirm = view.findViewById(R.id.dialogConfirm);
        TextView cancel = view.findViewById(R.id.dialogCancel);
        mPassword = view.findViewById(R.id.dialogPassword);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Captured Password");

                String password = mPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(password)){
                    mOnConfirmPasswordListener.onConfirmPasswordListener(password);
                    getDialog().dismiss();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Canceled");
                getDialog().dismiss();
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();

        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastExcp" + e.getMessage());
        }
    }
}
