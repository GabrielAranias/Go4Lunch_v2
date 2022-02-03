package com.gabriel.aranias.go4lunch_v2.service.place;

import com.google.firebase.firestore.CollectionReference;

public class PlaceHelper {

    private static volatile PlaceHelper instance;
    private final PlaceRepository placeRepository;

    private PlaceHelper() {
        placeRepository = PlaceRepository.getInstance();
    }

    public static PlaceHelper getInstance() {
        PlaceHelper result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PlaceRepository.class) {
            if (instance == null) {
                instance = new PlaceHelper();
            }
            return instance;
        }
    }

    public CollectionReference getPlaceCollection() {
        return placeRepository.getPlaceCollection();
    }
}
