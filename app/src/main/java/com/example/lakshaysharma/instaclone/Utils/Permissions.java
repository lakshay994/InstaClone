package com.example.lakshaysharma.instaclone.Utils;

import android.Manifest;

public class Permissions {

    public static final String[] PERMISSIONS = {

            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    public static final String[] CAMERA_PERSMISSION = {

            Manifest.permission.CAMERA,

    };

    public static final String[] WRITE_STORAGE_PERMISSIONS = {

            Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    public static final String[] READ_STORAGE_PERMISSIONS = {

            Manifest.permission.READ_EXTERNAL_STORAGE

    };
}
