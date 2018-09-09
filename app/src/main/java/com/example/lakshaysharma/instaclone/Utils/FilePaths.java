package com.example.lakshaysharma.instaclone.Utils;

import android.os.Environment;

public class FilePaths {

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR +"/DCIM/camera";

    public String FIREBASE_STORAGE_PATH = "photos/users/";
}
