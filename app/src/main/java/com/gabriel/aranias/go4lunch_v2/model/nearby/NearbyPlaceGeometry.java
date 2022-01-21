package com.gabriel.aranias.go4lunch_v2.model.nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NearbyPlaceGeometry implements Serializable {

    @SerializedName("location")
    @Expose
    private NearbyPlaceLocation location;

    public NearbyPlaceLocation getLocation() {
        return location;
    }

    public void setLocation(NearbyPlaceLocation location) {
        this.location = location;
    }
}
