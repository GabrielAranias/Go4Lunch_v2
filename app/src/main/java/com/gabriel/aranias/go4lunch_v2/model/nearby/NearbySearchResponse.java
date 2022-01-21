package com.gabriel.aranias.go4lunch_v2.model.nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class NearbySearchResponse implements Serializable {

    @SerializedName("results")
    @Expose
    private List<NearbyPlaceModel> nearbyPlaceModelList;

    @SerializedName("error_message")
    @Expose
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<NearbyPlaceModel> getNearbyPlaceModelList() {
        return nearbyPlaceModelList;
    }

    public void setGooglePlaceModelList(List<NearbyPlaceModel> nearbyPlaceModelList) {
        this.nearbyPlaceModelList = nearbyPlaceModelList;
    }
}
