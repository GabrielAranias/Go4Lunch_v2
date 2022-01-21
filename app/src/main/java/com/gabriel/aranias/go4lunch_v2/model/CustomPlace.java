package com.gabriel.aranias.go4lunch_v2.model;

public class CustomPlace {

    int id, drawableId;
    String name;
    String placeType;

    public CustomPlace() {
    }

    public CustomPlace(int id, int drawableId, String name, String placeType) {
        this.id = id;
        this.drawableId = drawableId;
        this.name = name;
        this.placeType = placeType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }
}
