package com.gabriel.aranias.go4lunch_v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class UserUnitTest {

    private static final String uid = "uid";
    private static final String username = "username";
    private static final String newUsername = "newUsername";
    private static final String pictureUrl = "pictureUrl";
    private static final String lunchSpotId = "lunchSpotId";
    private static final String lunchSpotName = "lunchSpotName";
    private static final String lunchSpotAddress = "lunchSpotAddress";
    private UserHelper userHelper;
    private User user;

    @Before
    public void setUp() {
        userHelper = mock(UserHelper.class, RETURNS_DEEP_STUBS);
        CollectionReference collectionReference = mock(CollectionReference.class, RETURNS_DEEP_STUBS);
        DocumentReference documentReference = mock(DocumentReference.class);
        FirebaseUser firebaseUser = mock(FirebaseUser.class, RETURNS_DEEP_STUBS);

        when(userHelper.getUserCollection()).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);

        when(firebaseUser.getUid()).thenReturn(uid);
        when(firebaseUser.getDisplayName()).thenReturn(username);
        when(Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString()).thenReturn(pictureUrl);

        user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),
                Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString(),
                null, null, null);
    }

    @Test
    public void createUserInFirestore() {
        userHelper.getUserCollection().document(uid).set(user);
        assertEquals(uid, user.getUid());
        assertEquals(username, user.getUsername());
        assertEquals(pictureUrl, user.getPictureUrl());
        verify(userHelper.getUserCollection().document(uid), times(1)).set(user);
    }

    @Test
    public void deleteUserFromFirestore() {
        createUserInFirestore();
        userHelper.getUserCollection().document(uid).delete();
        verify(userHelper.getUserCollection().document(uid), times(1)).delete();
    }

    @Test
    public void updateUsername() {
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(newUsername, arg0);
            return null;
        }).when(userHelper).updateUsername(anyString());
        userHelper.updateUsername(newUsername);
    }

    @Test
    public void updateLunchSpotId() {
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(lunchSpotId, arg0);
            return null;
        }).when(userHelper).updateLunchSpotId(anyString());
        userHelper.updateLunchSpotId(lunchSpotId);
    }

    @Test
    public void updateLunchSpotName() {
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(lunchSpotName, arg0);
            return null;
        }).when(userHelper).updateLunchSpotName(anyString());
        userHelper.updateLunchSpotName(lunchSpotName);
    }

    @Test
    public void updateLunchSpotAddress() {
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(lunchSpotAddress, arg0);
            return null;
        }).when(userHelper).updateLunchSpotAddress(anyString());
        userHelper.updateLunchSpotAddress(lunchSpotAddress);
    }
}