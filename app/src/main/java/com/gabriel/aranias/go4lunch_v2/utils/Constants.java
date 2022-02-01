package com.gabriel.aranias.go4lunch_v2.utils;

import com.gabriel.aranias.go4lunch_v2.BuildConfig;

public class Constants {

    public static final String API_KEY = BuildConfig.MAPS_API_KEY;

    public static final String EXTRA_RESTAURANT = "restaurant";
    public final static String EXTRA_WORKMATE = "workmate";

    public static final String USER_COLLECTION = "users";
    public static final String MESSAGE_COLLECTION = "messages";
    public static final String USER_ID_FIELD = "uid";
    public static final String RECEIVER_ID_FIELD = "receiverId";
    public static final String USERNAME_FIELD = "username";
    public static final String CONTENT_FIELD = "content";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String FAV_FIELD = "favoriteRestaurants";
    public static final String LUNCH_SPOT_ID_FIELD = "lunchSpotId";
    public static final String LUNCH_SPOT_NAME_FIELD = "lunchSpotName";

    public static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";
    public static final String ARGUMENT_FINISH_ACTIVITY = "finish";

    public static final String SHARED_PREFERENCES = "sharedPreferences";
    public static final String SAVED_LUNCH_SPOT = "savedLunchSpot";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 42;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
}
