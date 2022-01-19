package com.gabriel.aranias.go4lunch_v2.utils;

import com.gabriel.aranias.go4lunch_v2.BuildConfig;

public class Constants {

    public static final String API_KEY = BuildConfig.MAPS_API_KEY;
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    public static final String USER_COLLECTION = "users";
    public static final String USERNAME_FIELD = "username";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 42;
    public static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";
    public static final String ARGUMENT_FINISH_ACTIVITY = "finish";
    public static boolean permissionDenied = false;

    public static int radius = 5000;
}