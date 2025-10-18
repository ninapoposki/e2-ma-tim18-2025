package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.database.UserLocalDataSource;
import com.example.habitforge.data.firebase.UserRemoteDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean isUserActivated() {
        return remoteDb.checkEmailVerified();
    }

    public void activateUser(String userId, OnCompleteListener<Void> listener) {
        remoteDb.updateUserActivation(userId, true, listener);
    }

    // --- PRIJAVA ---
    public void login(String email, String password, OnCompleteListener<AuthResult> callback) {
        remoteDb.signInUser(email, password, callback);
    }
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


   //  --- PREUZIMANJE KORISNIKA ---
   // --- GET USER BY ID ---
//   public void getUserById(String userId, UserCallback callback) {
//       // prvo iz lokalne baze
//       User cachedUser = localDb.getUserById(userId);
//       if (cachedUser != null) {
//           callback.onSuccess(cachedUser);
//           return;
//       }
//
//       // iz Firestore
//       remoteDb.fetchUserDocument(userId, remoteTask -> {
//           if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
//               User cloudUser = remoteTask.getResult().toObject(User.class);
//               if (cloudUser != null) {
//                   localDb.insertUser(cloudUser);
//                   callback.onSuccess(cloudUser);
//               } else {
//                   callback.onFailure(new Exception("User not found"));
//               }
//           } else {
//               callback.onFailure(remoteTask.getException());
//           }
//       });
//   }

    public void getUserById(String userId, UserCallback callback) {

        // iz Firestore
        remoteDb.fetchUserDocument(userId, remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
                DocumentSnapshot snapshot = remoteTask.getResult();

                User cloudUser = new User();
                cloudUser.setUserId(snapshot.getId());
                cloudUser.setUsername(snapshot.getString("username"));
                cloudUser.setEmail(snapshot.getString("email"));

                // Avatar
                String avatar = snapshot.getString("avatar");
                cloudUser.setAvatar(avatar != null && !avatar.isEmpty() ? avatar : "default_avatar.png");

                // Level, XP, PP, Coins
                cloudUser.setLevel(snapshot.contains("level") ? snapshot.getLong("level").intValue() : 1);
                cloudUser.setExperiencePoints(snapshot.contains("experiencePoints") ? snapshot.getLong("experiencePoints").intValue() : 0);
                cloudUser.setPowerPoints(snapshot.contains("powerPoints") ? snapshot.getLong("powerPoints").intValue() : 0);
                cloudUser.setCoins(snapshot.contains("coins") ? snapshot.getLong("coins").intValue() : 0);

                // Title
                String title = snapshot.getString("title");
                cloudUser.setTitle(title != null && !title.isEmpty() ? title : "Beginner");

                // Badges i Equipment
                cloudUser.setBadges(snapshot.contains("badges") ? (List<String>) snapshot.get("badges") : new ArrayList<>());
                cloudUser.setEquipment(
                        snapshot.contains("userEquipment")
                                ? (List<UserEquipment>) snapshot.get("userEquipment")
                                : new ArrayList<>()
                );

                // Sačuvaj u lokalnu bazu
               // localDb.insertUser(cloudUser);

                // Vrati callback
                callback.onSuccess(cloudUser);
            } else {
                callback.onFailure(remoteTask.getException() != null ? remoteTask.getException() : new Exception("User not found"));
            }
        });
    }

    public void addEquipmentToUser(User user, UserEquipment item) {
        List<UserEquipment> equipmentList = user.getEquipment();

        switch (item.getType()) {
            case POTION:
                // Trenutno ništa posebno, samo dodajemo napitak
                break;
            case CLOTHING:
                // Trenutno ništa posebno, ali ovde možemo dodati buduće unapređenje
                break;
            case WEAPON:
                // Trenutno ništa posebno, ali ovde možemo dodati buduće unapređenje
                break;
        }

        // Svaki predmet dodajemo kao poseban objekat
        equipmentList.add(item);

        user.setEquipment(equipmentList);
        updateUser(user);
    }
    public void useWeapon(User user, String equipmentId, Runnable onSuccess) {
        if (user == null || user.getEquipment() == null) return;

        List<UserEquipment> equipmentList = user.getEquipment();

        for (int i = 0; i < equipmentList.size(); i++) {
            UserEquipment item = equipmentList.get(i);

            if (item.getEquipmentId().equals(equipmentId) && item.getType() == EquipmentType.WEAPON) {
                // Umanji trajanje
                item.setDuration(item.getDuration() - 1);

                // Ako je duration 0, izbriši
                if (item.getDuration() <= 0) {
                    equipmentList.remove(i);
                }

                // Snimi izmenjenog korisnika u Firestore
                try {
                    remoteDb.saveUserDocument(user);

                    // Pokreni callback da fragment može da osveži UI
                    if (onSuccess != null) {
                        onSuccess.run();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break; // prekid jer smo našli oružje
            }
        }
    }


//    public void addEquipmentToUser(User user, UserEquipment item) {
//        Map<String, UserEquipment> eqMap = user.getUserEquipment();
//
//        if (eqMap.containsKey(item.getId())) {
//            UserEquipment existing = eqMap.get(item.getId());
//
//            switch (item.getType()) {
//                case POTION:
//                    // 👉 samo povećaj količinu
//                    existing.setAmount(existing.getAmount() + item.getAmount());
//                    break;
//
//                case CLOTHING:
//                    // ako korisnik ima istu odeću, efekat se sabira
//                    existing.setEffect(existing.getEffect() + item.getEffect());
//                    existing.setDuration(Math.max(existing.getDuration(), item.getDuration()));
//                    break;
//
//                case WEAPON:
//                    // unapređenje oružja — povećaj level i malo efekat
//                    existing.setLevel(existing.getLevel() + 1);
//                    existing.setEffect(existing.getEffect() + item.getEffect() * 0.01);
//                    break;
//            }
//
//            eqMap.put(existing.getId(), existing);
//
//        } else {
//            eqMap.put(item.getId(), item);
//        }
//        user.setUserEquipment(eqMap);
//        updateUser(user);
//
//    }


    // --- GET USER BY EMAIL ---
//    public void getUserByEmail(String email, UserCallback callback) {
//        // prvo iz lokalne baze
//        User cachedUser = localDb.getUserByEmail(email);
//        if (cachedUser != null) {
//            callback.onSuccess(cachedUser);
//            return;
//        }
//
//        // iz Firestore
//        remoteDb.fetchUserByEmail(email, remoteTask -> {
//            if (remoteTask.isSuccessful() && remoteTask.getResult() != null && !remoteTask.getResult().isEmpty()) {
//                User cloudUser = remoteTask.getResult().getDocuments().get(0).toObject(User.class);
//                if (cloudUser != null) {
//                    localDb.insertUser(cloudUser);
//                    callback.onSuccess(cloudUser);
//                } else {
//                    callback.onFailure(new Exception("User not found"));
//                }
//            } else {
//                callback.onFailure(remoteTask.getException());
//            }
//        });
//    }


    // --- AKTIVACIJA ---
//    public void setUserActivated(String userId, boolean active) {
//        // update lokalno
//        User u = localDb.getUserById(userId);
//        if (u != null) {
//            u.setActive(active);
//            localDb.updateUser(u);
//        }
//
//        // update u Firestore
//        remoteDb.updateUserActivation(userId, active, result -> {
//            if (!result.isSuccessful()) {
//                Log.e("UserRepo", "Failed to update activation status", result.getException());
//            }
//        });
//    }

    // --- SVI KORISNICI ---
    public void getAllUsers(UserListCallback callback) {
        // prvo iz lokalne baze
//        List<User> cached = localDb.getAllUsers();
//        if (!cached.isEmpty()) {
//            callback.onSuccess(new ArrayList<>(cached));
//            return;
//        }

        // iz Firestore
        remoteDb.fetchAllUsers(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> remoteUsers = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User u = doc.toObject(User.class);
                    remoteUsers.add(u);
                 //   localDb.insertUser(u);
                }
                callback.onSuccess(remoteUsers);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onFailure(Exception e);
    }

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
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }
}

