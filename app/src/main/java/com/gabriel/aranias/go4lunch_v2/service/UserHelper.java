package com.gabriel.aranias.go4lunch_v2.service;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class UserHelper {

    private static volatile UserHelper instance;
    private final UserRepository userRepository;

    private UserHelper() {
        userRepository = UserRepository.getInstance();
    }

    public static UserHelper getInstance() {
        UserHelper result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserRepository.class) {
            if (instance == null) {
                instance = new UserHelper();
            }
            return instance;
        }
    }

    public FirebaseUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public Boolean isCurrentUserLoggedIn() {
        return (this.getCurrentUser() != null);
    }

    public Task<Void> signOut(Context context){
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context){
        return userRepository.deleteUser(context);
    }
}
