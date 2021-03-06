package com.gabriel.aranias.go4lunch_v2.model.nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class NearbyPlaceModel implements Serializable {

    @SerializedName("business_status")
    @Expose
    private String businessStatus;

    @SerializedName("geometry")
    @Expose
    private NearbyPlaceGeometry geometry;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("obfuscated_type")
    @Expose
    private List<Object> obfuscatedType = null;

    @SerializedName("photos")
    @Expose
    private List<NearbyPlacePhoto> photos = null;

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @SerializedName("rating")
    @Expose
    private Double rating;

    @SerializedName("reference")
    @Expose
    private String reference;

    @SerializedName("scope")
    @Expose
    private String scope;

    @SerializedName("types")
    @Expose
    private List<String> types = null;

    @SerializedName("user_ratings_total")
    @Expose
    private Integer userRatingsTotal;

    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    @SerializedName("opening_hours")
    @Expose
    private NearbyPlaceOpeningHours openingHours;

    private String docId;

    public NearbyPlaceModel() {
    }

    public NearbyPlaceModel(String businessStatus, NearbyPlaceGeometry geometry, String icon,
                            String name, List<Object> obfuscatedType, List<NearbyPlacePhoto> photos,
                            String placeId, Double rating, String reference, String scope,
                            List<String> types, Integer userRatingsTotal, String vicinity,
                            NearbyPlaceOpeningHours openingHours, String docId) {
        this.businessStatus = businessStatus;
        this.geometry = geometry;
        this.icon = icon;
        this.name = name;
        this.obfuscatedType = obfuscatedType;
        this.photos = photos;
        this.placeId = placeId;
        this.rating = rating;
        this.reference = reference;
        this.scope = scope;
        this.types = types;
        this.userRatingsTotal = userRatingsTotal;
        this.vicinity = vicinity;
        this.openingHours = openingHours;
        this.docId = docId;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public NearbyPlaceGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(NearbyPlaceGeometry geometry) {
        this.geometry = geometry;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getObfuscatedType() {
        return obfuscatedType;
    }

    public void setObfuscatedType(List<Object> obfuscatedType) {
        this.obfuscatedType = obfuscatedType;
    }

    public List<NearbyPlacePhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<NearbyPlacePhoto> photos) {
        this.photos = photos;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Integer getUserRatingsTotal() {
        return userRatingsTotal;
    }

    public void setUserRatingsTotal(Integer userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public NearbyPlaceOpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(NearbyPlaceOpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}