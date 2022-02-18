package com.gabriel.aranias.go4lunch_v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.place.PlaceHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;

public class PlaceUnitTest {

    private static final String docId = "docId";
    private static final String placeId = "placeId";
    private static final String placeName = "placeName";
    private static final String address = "address";
    private static final String photoUrl = "photoUrl";
    private static final Double lat = 42.2;
    private static final Double lng = 2.24;
    private static final Double rating = 3.5;
    private PlaceHelper placeHelper;
    private NearbyPlaceModel place;

    @Before
    public void setUp() {
        placeHelper = mock(PlaceHelper.class, RETURNS_DEEP_STUBS);
        CollectionReference collectionReference = mock(CollectionReference.class, RETURNS_DEEP_STUBS);
        DocumentReference documentReference = mock(DocumentReference.class);

        when(placeHelper.getPlaceCollection()).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);

        place = mock(NearbyPlaceModel.class, RETURNS_DEEP_STUBS);
    }

    @Test
    public void createPlaceInFirestore() {
        placeHelper.getPlaceCollection().document(docId).set(place);
        verify(placeHelper.getPlaceCollection().document(docId), times(1)).set(place);
    }

    @Test
    public void deletePlaceFromFirestore() {
        createPlaceInFirestore();
        placeHelper.getPlaceCollection().document(docId).delete();
        verify(placeHelper.getPlaceCollection().document(docId), times(1)).delete();
    }

    @Test
    public void getPlaceDetails() {
        when(place.getPlaceId()).thenReturn(placeId);
        assertEquals(placeId, place.getPlaceId());

        when(place.getName()).thenReturn(placeName);
        assertEquals(placeName, place.getName());

        when(place.getVicinity()).thenReturn(address);
        assertEquals(address, place.getVicinity());

        when(place.getPhotos().get(0).getPhotoReference()).thenReturn(photoUrl);
        assertEquals(photoUrl, place.getPhotos().get(0).getPhotoReference());

        when(place.getOpeningHours().getOpenNow()).thenReturn(true);
        assertTrue(place.getOpeningHours().getOpenNow());

        when(place.getGeometry().getLocation().getLat()).thenReturn(lat);
        when(place.getGeometry().getLocation().getLng()).thenReturn(lng);
        assertEquals(lat, place.getGeometry().getLocation().getLat());
        assertEquals(lng, place.getGeometry().getLocation().getLng());

        when(place.getRating()).thenReturn(rating);
        assertEquals(rating, place.getRating());
    }
}
