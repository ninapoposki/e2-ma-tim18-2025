package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.application.session.SessionManager;
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
//                cloudUser.setEquipment(
//                        snapshot.contains("userEquipment")
//                                ? (List<UserEquipment>) snapshot.get("userEquipment")
//                                : new ArrayList<>()
//                );

                if (snapshot.contains("equipment")) {
                    List<Map<String, Object>> rawList = (List<Map<String, Object>>) snapshot.get("equipment");
                    List<UserEquipment> parsedList = new ArrayList<>();

                    if (rawList != null) {
                        for (Map<String, Object> map : rawList) {
                            try {
                                UserEquipment eq = new UserEquipment();
                                eq.setId((String) map.get("id"));
                                eq.setEquipmentId((String) map.get("equipmentId"));
                                eq.setType(EquipmentType.valueOf((String) map.get("type")));
                                if (map.get("duration") != null)
                                    eq.setDuration(((Long) map.get("duration")).intValue());
                                if (map.get("effect") != null)
                                    eq.setEffect((Double) map.get("effect"));
                                if (map.get("level") != null)
                                    eq.setLevel(((Long) map.get("level")).intValue());
                                if (map.get("active") != null)
                                    eq.setActive((Boolean) map.get("active"));
                                if (map.get("usedInNextBossFight") != null)
                                    eq.setUsedInNextBossFight((Boolean) map.get("usedInNextBossFight"));
                                parsedList.add(eq);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    cloudUser.setEquipment(parsedList);
                } else {
                    cloudUser.setEquipment(new ArrayList<>());
                }

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
        //user.getEquipment().add(item);
       // user.setEquipment(equipmentList);
        remoteDb.addEquipmentItem(user, item);
        //updateUser(user);
    }

    public void useAllActiveClothing(User user, Runnable onSuccess) {
        if (user == null || user.getEquipment() == null) return;

        List<UserEquipment> equipmentList = user.getEquipment();
        boolean changed = false;

        // prolazimo kroz sve aktivne CLOTHING
        for (int i = 0; i < equipmentList.size(); i++) {
            UserEquipment item = equipmentList.get(i);

            if (item.getType() == EquipmentType.CLOTHING && item.isActive()) {
                // umanji trajanje
                item.setDuration(item.getDuration() - 1);
                changed = true;

                // ako je duration pao na 0 ili manje, ukloni
                if (item.getDuration() <= 0) {
                    equipmentList.remove(i);
                    i--; // obavezno smanji i, jer se lista pomerila
                }
            }
        }

        if (changed) {
            try {
                remoteDb.saveUserDocument(user); // snimi izmene
                if (onSuccess != null) onSuccess.run(); // callback za UI osveženje
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void useAllActivePotions(User user, Runnable onSuccess) {
        if (user == null || user.getEquipment() == null) return;

        boolean changed = false;

        for (UserEquipment item : user.getEquipment()) {
            if (item.getType() == EquipmentType.POTION
                    && item.isActive()
                    && !item.isUsedInNextBossFight()) {
                item.setUsedInNextBossFight(true);
                changed = true;
            }
        }

        if (changed) {
            try {
                remoteDb.saveUserDocument(user); // snimi sve izmene u Firestore
                if (onSuccess != null) onSuccess.run(); // callback da fragment osveži UI
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Metoda koja dodaje oružje od bossa
    public void receiveEquipmentByBoss(User user, UserEquipment newItem) {
        if (user == null || newItem == null) return;

        List<UserEquipment> equipmentList = user.getEquipment();
        boolean found = false;

        switch (newItem.getType()) {
            case POTION:
                // Dodaj napitak kao novi item, nema posebnih pravila
                equipmentList.add(newItem);
                break;

            case CLOTHING:
                // Trenutno dodajemo kao novi item
                equipmentList.add(newItem);
                break;

            case WEAPON:
                // Proveri da li korisnik već ima ovo oružje
                for (UserEquipment eq : equipmentList) {
                    if (eq.getEquipmentId().equals(newItem.getEquipmentId())) {
                        // Već ima ovo oružje → povećaj efekat za 0.02%
                        eq.setEffect(eq.getEffect() + 0.0002);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Dodaj novo oružje
                    equipmentList.add(newItem);
                }
                break;
        }
        //user.setEquipment(equipmentList);

        // Sačuvaj promene u Firebase
        remoteDb.addEquipmentItem(user, newItem);
    }


    // Metoda za unapređenje oružja
    public void upgradeWeapon(User user, UserEquipment weapon, int potentialCoinsFromBoss) {
        if (user == null || weapon == null) return;

        int cost = (int)(0.6 * potentialCoinsFromBoss);

        if (user.getCoins() < cost) {
            // Nema dovoljno novčića
            //Toast.makeText(context, "Nemaš dovoljno novčića za unapređenje!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Oduzmi novčiće
        user.setCoins(user.getCoins() - cost);

        // Povećaj efekat i level
        weapon.setEffect(weapon.getEffect() + 0.0001);
        weapon.setLevel(weapon.getLevel() + 1);

        // Sačuvaj promene
        remoteDb.addEquipmentItem(user, weapon);
      //  userRepository.updateUser(user); // ili update cele liste
    }





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

    // --- DODAJ XP KORISNIKU ---
    public void addExperienceToUser(Context context, int gainedXp, OnCompleteListener<Void> callback) {
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();

        if (userId == null) {
            Log.e("UserRepo", "No logged user found for XP update.");
            return;
        }

        remoteDb.fetchUserDocument(userId, remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
                DocumentSnapshot doc = remoteTask.getResult();
                int currentXp = doc.contains("experiencePoints") ? doc.getLong("experiencePoints").intValue() : 0;
                int newXp = currentXp + gainedXp;

                // Ažuriraj XP u Firestore
                remoteDb.getFirestore()
                        .collection("users")
                        .document(userId)
                        .update("experiencePoints", newXp)
                        .addOnSuccessListener(aVoid -> {
                            Log.i("UserRepo", "✅ XP updated: " + newXp);
                            callback.onComplete(null);
                        })
                        .addOnFailureListener(e -> Log.e("UserRepo", "❌ XP update failed", e));
            } else {
                Log.e("UserRepo", "User not found in Firestore for XP update.");
            }
        });
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }
}

