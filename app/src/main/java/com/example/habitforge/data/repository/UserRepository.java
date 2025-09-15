package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.database.UserLocalDataSource;
import com.example.habitforge.data.firebase.UserRemoteDataSource;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final UserLocalDataSource localDb;
    private final UserRemoteDataSource remoteDb;

    public UserRepository(Context context) {
        this.localDb = new UserLocalDataSource(context);
        this.remoteDb = new UserRemoteDataSource();
    }

    // --- REGISTRACIJA ---
    public void signUpUser(String email, String password, User newUser, OnCompleteListener<AuthResult> callback) {
        remoteDb.createUserAuth(email, password, authTask -> {
            if (authTask.isSuccessful() &&
                    authTask.getResult() != null &&
                    authTask.getResult().getUser() != null) {

                String generatedId = authTask.getResult().getUser().getUid();
                newUser.setUserId(generatedId);

                // slanje email verifikacije
                remoteDb.sendActivationMail();

                // čuvanje korisnika lokalno i u Firestore
                remoteDb.saveUserDocument(newUser);
                localDb.insertUser(newUser);
            }
            callback.onComplete(authTask);
        });
    }

    // --- PRIJAVA ---
//    public void signInUser(String email, String password, OnCompleteListener<AuthResult> callback) {
//        remoteDb.signInUser(email, password, callback);
//    }
//    public void signInUser(String email, String password, OnCompleteListener<AuthResult> callback) {
//        remoteDb.signInUser(email, password, authTask -> {
//            if (authTask.isSuccessful() && authTask.getResult() != null) {
//                FirebaseUser firebaseUser = auth.getCurrentUser();
//                if (firebaseUser != null && firebaseUser.isEmailVerified()) {
//                    // aktivacija prošla -> upiši u Firestore da je active = true
//                    updateUserActivation(firebaseUser.getUid(), true, result -> {});
//                    callback.onComplete(authTask);
//                } else {
//                    // ako nije aktiviran -> izbriši korisnika i prijava ne prolazi
//                    removeUser(firebaseUser.getUid(), result -> {});
//                    callback.onComplete(Tasks.forException(
//                            new Exception("Nalog nije aktiviran! Proverite email.")));
//                }
//            } else {
//                callback.onComplete(authTask);
//            }
//        });
//    }
//


    // --- PREUZIMANJE KORISNIKA ---
//    public void getUserById(String userId, OnCompleteListener<User> callback) {
//        User cachedUser = localDb.getUserById(userId);
//
//        if (cachedUser != null) {
//            callback.onComplete(Task.forResult(cachedUser));
//        }
//
//        remoteDb.fetchUserDocument(userId, remoteTask -> {
//            if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
//                User cloudUser = remoteTask.getResult().toObject(User.class);
//                if (cloudUser != null) {
//                    localDb.insertUser(cloudUser); // update cache
//                    callback.onComplete(Task.forResult(cloudUser));
//                }
//            } else if (cachedUser == null) {
//                callback.onComplete(Task.forException(remoteTask.getException()));
//            }
//        });
//    }

    // --- AKTIVACIJA ---
    public void setUserActivated(String userId, boolean active) {
        // update lokalno
        User u = localDb.getUserById(userId);
        if (u != null) {
            u.setActive(active);
            localDb.updateUser(u);
        }

        // update u Firestore
        remoteDb.updateUserActivation(userId, active, result -> {
            if (!result.isSuccessful()) {
                Log.e("UserRepo", "Failed to update activation status", result.getException());
            }
        });
    }

    // --- SVI KORISNICI ---
//    public void getAllUsers(OnCompleteListener<List<User>> callback) {
//        List<User> cached = localDb.getAllUsers();
//
//        if (!cached.isEmpty()) {
//            callback.onComplete(Task.forResult(cached));
//        }
//
//        remoteDb.fetchAllUsers(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                List<User> remoteUsers = new ArrayList<>();
//                for (QueryDocumentSnapshot doc : task.getResult()) {
//                    User u = doc.toObject(User.class);
//                    remoteUsers.add(u);
//                    localDb.insertUser(u);
//                }
//                callback.onComplete(Task.forResult(remoteUsers));
//            } else if (cached.isEmpty()) {
//                callback.onComplete(Task.forException(task.getException()));
//            }
//        });
//    }

    // --- UPDATE ---
    public void updateUser(User user) {
        localDb.updateUser(user);
        remoteDb.saveUserDocument(user);
    }

    // --- BRISANJE ---
    public void removeUser(String userId, OnCompleteListener<Void> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && userId.equals(auth.getCurrentUser().getUid())) {
            auth.getCurrentUser().delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    remoteDb.deleteUserDocument(userId, firestoreTask -> {
                        localDb.deleteUser(userId, callback);
                        callback.onComplete(firestoreTask);
                    });
                } else {
                    callback.onComplete((Task<Void>) deleteTask);
                }
            });
        } else {
            // ako je neki drugi user, samo ga izbriši iz baza
            remoteDb.deleteUserDocument(userId, firestoreTask -> {
                localDb.deleteUser(userId, callback);
                callback.onComplete(firestoreTask);
            });
        }
    }
}
