package com.gabriel.aranias.go4lunch_v2.service;

import static com.gabriel.aranias.go4lunch_v2.utils.Constants.USERNAME_FIELD;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.USER_COLLECTION;

import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public final class UserRepository {

    private static volatile UserRepository instance;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return AuthUI.getInstance().delete(context);
    }

    // Get collection reference
    private CollectionReference getUserCollection() {
        return FirebaseFirestore.getInstance().collection(USER_COLLECTION);
    }

    // Create user in Firestore
    public void createUser() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String pictureUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
            String username = user.getDisplayName();
            String uid = user.getUid();

            User userToCreate = new User(uid, username, pictureUrl);

            Task<DocumentSnapshot> userData = getUserData();
            // If user already exists in Firestore, get their data
            Objects.requireNonNull(userData).addOnSuccessListener(documentSnapshot ->
                    this.getUserCollection().document(uid).set(userToCreate));
        }
    }

    // Get user data from Firestore
    public Task<DocumentSnapshot> getUserData() {
        String uid = this.getCurrentUserId();
        if (uid != null) {
            return this.getUserCollection().document(uid).get();
        } else {
            return null;
        }
    }

    // Update username
    public Task<Void> updateUsername(String username) {
        String uid = this.getCurrentUserId();
        if (uid != null) {
            return this.getUserCollection().document(uid).update(USERNAME_FIELD, username);
        } else {
            return null;
        }
    }

    // Delete user from Firestore
    public void deleteUserFromFirestore() {
        String uid = this.getCurrentUserId();
        if (uid != null) {
            this.getUserCollection().document(uid).delete();
        }
    }

    private String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
}
