package com.gabriel.aranias.go4lunch_v2.utils;

public class RatingUtils {

    public static float transformFiveStarsIntoThree(float rating) {
        return ((rating * 3) / 5);
    }
}
