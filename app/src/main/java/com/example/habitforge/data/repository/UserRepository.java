package com.example.habitforge.data.repository;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.habitforge.application.model.Alliance;
import com.example.habitforge.application.model.AllianceInvite;
import com.example.habitforge.application.model.AllianceMessage;
import com.example.habitforge.application.model.FriendRequest;
import androidx.annotation.Nullable;

import com.example.habitforge.application.model.Boss;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.utils.FcmSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.database.UserLocalDataSource;
import com.example.habitforge.data.firebase.UserRemoteDataSource;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class UserRepository {

    private final UserLocalDataSource localDb;
    private final UserRemoteDataSource remoteDb;

    private final TaskRepository taskRepository;

    public UserRepository(Context context) {
        this.localDb = new UserLocalDataSource(context);
        this.remoteDb = new UserRemoteDataSource();
        taskRepository = new TaskRepository(context);
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
    public void updateUserFcmToken(String userId, String fcmToken) {
        remoteDb.getFirestore()
                .collection("users")
                .document(userId)
                .update("fcmToken", fcmToken)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "FCM token updated for user: " + userId))
                .addOnFailureListener(e -> Log.e("FCM", "Failed to update FCM token for user: " + userId, e));
    }


//    public void loginAndSavePlayerId(String email, String password, GenericCallback callback) {
//        login(email, password, task -> {
//            if (task.isSuccessful()) {
//                String userId = task.getResult().getUser().getUid();
//
//                // ✅ Novi način - uzimanje OneSignal subscription ID-ja (playerId)
//                String playerId = OneSignal.getUser().getPushSubscription().getId();
//
//                if (playerId != null && !playerId.isEmpty()) {
//                    saveOneSignalPlayerId(userId, playerId, callback);
//                } else {
//                    callback.onComplete(false);
//                }
//            } else {
//                callback.onComplete(false);
//            }
//        });
//    }
//public void loginAndSavePlayerId(String email, String password, GenericCallback callback) {
//    login(email, password, task -> {
//        if (task.isSuccessful()) {
//            String userId = task.getResult().getUser().getUid();
//
//            // ⚠️ Stari Java SDK 5.1.8
//            // Dobijanje playerId kroz PermissionSubscriptionState
//            OneSignal.PermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
//            if (status != null && status.getSubscriptionStatus() != null) {
//                String playerId = status.getSubscriptionStatus().getUserId();
//
//                if (playerId != null && !playerId.isEmpty()) {
//                    saveOneSignalPlayerId(userId, playerId, callback);
//                    return;
//                }
//            }
//
//            // Ako playerId još nije spreman, čekaš da se registruje
//            // Opcija: mali delay i retry
//            new Handler(Looper.getMainLooper()).postDelayed(() -> loginAndSavePlayerId(email, password, callback), 1500);
//
//        } else {
//            callback.onComplete(false);
//        }
//    });
//}



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
                String allianceId = snapshot.getString("allianceId");
                cloudUser.setAllianceId(allianceId != null ? allianceId : "");
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
                List<String> friendIdsList = new ArrayList<>();
                if (snapshot.contains("friendIds") && snapshot.get("friendIds") != null) {
                    List<Object> rawList = (List<Object>) snapshot.get("friendIds");
                    for (Object o : rawList) {
                        if (o != null) friendIdsList.add(o.toString());
                    }
                }
                cloudUser.setFriendIds(friendIdsList);

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

        //int cost = (int)(0.6 * potentialCoinsFromBoss);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUserId())
                .get()
                .addOnSuccessListener(doc -> {
                            if (!doc.exists()) return;

                            User user_ = doc.toObject(User.class);
                            if (user_ == null) return;

                            int userLevel = user_.getLevel();
                            int potentialCoins = calculateCoinsReward(userLevel, true);
                            int cost = (int) (0.6 * potentialCoins);

                            if (user.getCoins() < cost) {
                                Log.d("UpgradeWeapon", "Nemaš dovoljno novčića za unapređenje!");
                                return;
                            }

        // Oduzmi novčiće
        user.setCoins(user.getCoins() - cost);
                    updateUser(user);

        // Povećaj efekat i level
        //weapon.setEffect(weapon.getEffect() + 0.0001);
       // weapon.setLevel(weapon.getLevel() + 1);

        // Sačuvaj promene
      //  remoteDb.addEquipmentItem(user, weapon);
        for (UserEquipment ue : user.getEquipment()) {
            if (ue.getEquipmentId().equals(weapon.getEquipmentId())) {
                ue.setLevel(ue.getLevel() + 1);
                ue.setEffect(ue.getEffect() + 0.0001);
                break;
            }
        }

        // Updatuj Firestore

        db.collection("users").document(user.getUserId())
                .update("equipment", user.getEquipment())
                .addOnSuccessListener(aVoid -> {
                    // opcionalno: callback ili log
                })
                .addOnFailureListener(e -> e.printStackTrace());
    });
    }

    // --- PROVERA I AZURIRANJE LEVELA ---
    public void checkAndUpdateLevel(String userId, String taskId) {
        if (userId == null || userId.isEmpty()) return;

        getUserById(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                int currentLevel = user.getLevel();
                int currentXP = user.getExperiencePoints();

                // XP potreban za prvi nivo
                int xpNeeded = 200;
                int ppReward = 40;

                // Ako je već iznad level 1, računamo XP za sledeće nivoe
                for (int i = 1; i < currentLevel; i++) {
                    xpNeeded = calculateNextLevelXP(xpNeeded);
                    ppReward = calculateNextLevelPP(ppReward);
                }

                boolean leveledUp = false;

                // Proveravamo da li korisnik može da pređe nivo više puta
                while (currentXP >= xpNeeded) {
                    currentLevel++;        // povecaj level
                    leveledUp = true;

                    // Dodaj nagradu za prelazak nivoa (može da bude coins)
                    int finalCurrentLevel = currentLevel;
                    int finalPpReward = ppReward;
                    calculateCoinsForPreviousLevelBoss(userId, currentLevel, coinsReward -> {
                                if (coinsReward > 0) {
                                    user.setCoins(user.getCoins() + coinsReward);
                                    Log.d("BossReward", "Korisnik nagrađen sa " + coinsReward + " novčića za bossa iz nivoa " + (finalCurrentLevel - 1));
                                } else {
                                    Log.d("BossReward", "Nema nagrade — boss iz prethodnog nivoa nije poražen ili već isplaćen.");
                                }


                                user.setLevel(finalCurrentLevel);
                                user.setPowerPoints(user.getPowerPoints() + finalPpReward);

                                // Dodeli novu titulu
                                user.setTitle(getTitleForLevel(finalCurrentLevel));

                        updateUser(user);
                            });

                    // Izračunaj XP potreban za sledeći nivo
                    xpNeeded = calculateNextLevelXP(xpNeeded);
                }

                if (leveledUp) {
                    // Sačuvaj promene u bazi
                    updateUser(user);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("UserRepo", "User not found for level check.", e);
            }
        });
    }


    // --- Pomoćne metode ---
    private int calculateNextLevelXP(int previousXP) {
        double nextXP = previousXP * 2 + previousXP / 2.0;
        return ((int) (Math.ceil(nextXP / 100))) * 100;  // zaokružuje na sledeću stotinu
    }

    private void calculateCoinsForPreviousLevelBoss(String userId, int currentLevel, CoinsCallback callback) {
        if (currentLevel <= 1) {
            // Nema prethodnog nivoa
            callback.onResult(0);
            return;
        }

        int previousLevel = currentLevel - 1;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("bosses")
                .whereEqualTo("level", previousLevel)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        // Nema bossa za prethodni nivo → nema nagrade
                        callback.onResult(0);
                        return;
                    }

                    var bossDoc = query.getDocuments().get(0);
                    Boolean defeated = bossDoc.getBoolean("defeated");
                    Boolean claimed = bossDoc.getBoolean("claimed"); // da ne dobije 2x

                    if (defeated != null && defeated && (claimed == null || !claimed)) {
                        int coinsReward =calculateCoinsReward(previousLevel, true);


                        // ✅ Obeleži da je nagrada preuzeta
                        bossDoc.getReference().update("claimed", true);

                        callback.onResult(coinsReward);
                    } else {
                        callback.onResult(0);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onResult(0);
                });
    }
    public interface CoinsCallback {
        void onResult(int coins);
    }

    private int calculateCoinsReward(int bossLevel, boolean fullVictory) {
        int baseReward = 200;
        int levelMultiplier = (int) Math.pow(1.2, bossLevel - 1);
        int coinsReward = baseReward * levelMultiplier;
        if (!fullVictory) coinsReward /= 2;
        return coinsReward;
    }

    private String getTitleForLevel(int level) {
        switch (level) {
            case 1: return "Beginner";
            case 2: return "Apprentice";
            case 3: return "Warrior";
            default: return "Hero Level " + level;
        }
    }

    private int calculateNextLevelPP(int previousPP) {
        return (int) Math.round(previousPP * 1.75);
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

    private void calculateTaskXp(String taskId, int userLevel, OnSuccessListener<Integer> callback) {
        taskRepository.getTaskById(taskId, result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                TaskDifficulty diff = result.getResult().getDifficulty();
                TaskPriority prio = result.getResult().getPriority();

                int xpPriority = prio.getXp();
                int xpDifficulty = diff.getXp();

                for (int i = 1; i < userLevel; i++) {
                    xpPriority = Math.round(xpPriority + xpPriority / 2.0f);
                    xpDifficulty = Math.round(xpDifficulty + xpDifficulty / 2.0f);
                }

                int totalXp = xpPriority + xpDifficulty;
                Log.i("UserRepo", "✅ Izračunat XP: " + totalXp + " (Level: " + userLevel + ")");
                callback.onSuccess(totalXp);

            } else {
                Log.e("UserRepo", "❌ Task not found for XP calculation.");
                callback.onSuccess(0); // fallback
            }
        });
    }


    // --- DODAJ XP KORISNIKU ---
    public void addExperienceToUser(Context context, int gainedXp,String taskId, OnCompleteListener<Void> callback) {
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();

        if (userId == null) {
            Log.e("UserRepo", "No logged user found for XP update.");
            return;
        }


        getUserById(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                int userLevel = user.getLevel();

                // ✅ Izračunaj XP za zadatak na osnovu levela
                calculateTaskXp(taskId, userLevel, totalXp -> {

                    // ✅ Kad dobiješ XP, dodaj ga korisniku
                    remoteDb.fetchUserDocument(userId, remoteTask -> {
                        if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
                            DocumentSnapshot doc = remoteTask.getResult();
                            int currentXp = doc.contains("experiencePoints") ? doc.getLong("experiencePoints").intValue() : 0;
                            int newXp = currentXp + totalXp;

                            remoteDb.getFirestore()
                                    .collection("users")
                                    .document(userId)
                                    .update("experiencePoints", newXp)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i("UserRepo", "✅ XP updated for " + user.getUsername() + ": " + newXp);
                                        checkAndUpdateLevel(userId, taskId);
                                        callback.onComplete(null);
                                    })
                                    .addOnFailureListener(e -> Log.e("UserRepo", "❌ XP update failed", e));

                        } else {
                            Log.e("UserRepo", "User not found in Firestore for XP update.");
                        }
                    });
                });
//        remoteDb.fetchUserDocument(userId, remoteTask -> {
//            if (remoteTask.isSuccessful() && remoteTask.getResult() != null && remoteTask.getResult().exists()) {
//                DocumentSnapshot doc = remoteTask.getResult();
//                int currentXp = doc.contains("experiencePoints") ? doc.getLong("experiencePoints").intValue() : 0;
//                int newXp = currentXp + gainedXp;
//
//                // Ažuriraj XP u Firestore
//                remoteDb.getFirestore()
//                        .collection("users")
//                        .document(userId)
//                        .update("experiencePoints", newXp)
//                        .addOnSuccessListener(aVoid -> {
//                            Log.i("UserRepo", "✅ XP updated: " + newXp);
//                            checkAndUpdateLevel(userId, taskId);
//                            callback.onComplete(null);
//                        })
//                        .addOnFailureListener(e -> Log.e("UserRepo", "❌ XP update failed", e));
//
//            } else {
//                Log.e("UserRepo", "User not found in Firestore for XP update.");
//            }
//        });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    // Šansa da korisnikov napad uspe se obračunava po procentu uspešnosti rešavanja zadataka
    public void getUserSuccessRate(String userId, SuccessRateCallback callback) {
        if (userId == null) {
            callback.onFailure(new Exception("User ID is null"));
            return;
        }

        remoteDb.getFirestore()
                .collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(0);
                        return;
                    }

                    int completed = 0;
                    int total = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");
                        Boolean exceedsQuota = doc.getBoolean("exceedsQuota");

                        // Ako nema status ili ako je task prešao kvotu — preskačemo ga
                        if (status == null || Boolean.TRUE.equals(exceedsQuota)) continue;

                        // Uračunavamo samo aktivne (nepauzirane/neotkazane)
                        if (!status.equals("PAUSED") && !status.equals("CANCELED")) {
                            total++;
                            if (status.equals("COMPLETED")) completed++;
                        }
                    }

                    int rate = total > 0 ? (completed * 100 / total) : 0;
                    callback.onSuccess(rate);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //za reward nakon borbe
    public void addCoinsAndReward(String userId, int currentCoins, int coinsReward, @Nullable String itemReward,
                                  Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int newCoins = currentCoins + coinsReward;

        db.collection("users").document(userId)
                .update("coins", newCoins)
                .addOnSuccessListener(aVoid -> {
                    if (itemReward != null) {
                        Map<String, Object> newItem = new HashMap<>();
                        newItem.put("name", itemReward);
                        newItem.put("acquiredAt", System.currentTimeMillis());
                        db.collection("users").document(userId)
                                .collection("inventory")
                                .add(newItem);
                    }
                    onSuccess.run();
                })
                .addOnFailureListener(e -> onFailure.run());
    }

//    public void getCurrentBoss(String userId, BossCallback callback) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("users").document(userId)
//                .collection("bosses")
//                .document("currentBoss")
//                .get()
//                .addOnSuccessListener(document -> {
//                    if (document.exists()) {
//                        Boss boss = document.toObject(Boss.class);
//                        callback.onSuccess(boss);
//                    } else {
//                        callback.onSuccess(null); // Boss ne postoji još
//                    }
//                })
//                .addOnFailureListener(callback::onFailure);
//    }
public void getCurrentBoss(String userId, BossCallback callback) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    db.collection("users").document(userId)
            .collection("bosses")
            .get()
            .addOnSuccessListener(query -> {
                Boss activeBoss = null;
                int maxLevelDefeated = 0;

                for (var doc : query.getDocuments()) {
                    Boss b = doc.toObject(Boss.class);
                    if (b == null) continue;

                    if (!b.isDefeated()) activeBoss = b;
                    else if (b.getLevel() > maxLevelDefeated) maxLevelDefeated = b.getLevel();
                }

                if (activeBoss != null) {
                    callback.onSuccess(activeBoss);
                    return;
                }

                // 🔹 Ako nema aktivnog bossa — kreiraj sledećeg
                int nextLevel = Math.max(1, maxLevelDefeated + 1);
                int bossHP = calculateBossHP(nextLevel);
                Boss newBoss = new Boss(nextLevel, bossHP, bossHP);
                db.collection("users").document(userId)
                        .collection("bosses")
                        .document("boss_" + nextLevel)
                        .set(newBoss)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(newBoss))
                        .addOnFailureListener(callback::onFailure);
            })
            .addOnFailureListener(callback::onFailure);
}



    public interface BossCallback {
        void onSuccess(@Nullable Boss boss);
        void onFailure(Exception e);
    }




    public interface SuccessRateCallback {
        void onSuccess(int rate);
        void onFailure(Exception e);
    }





    // --- FRIEND REQUESTS ---
    public void sendFriendRequest(String fromUserId, String toUserId, FriendRequestCallback  callback) {
        FriendRequest request = new FriendRequest(fromUserId, toUserId, System.currentTimeMillis());
        remoteDb.getFirestore()
                .collection("friendRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    List<FriendRequest> list = new ArrayList<>();
                    list.add(request);  // stavi u listu
                    callback.onSuccess(list);  // uspeh
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);  // greška
                });
    }

    public void getFriendRequestsForUser(String userId, FriendRequestCallback callback) {
        remoteDb.getFirestore()
                .collection("friendRequests")
                .whereEqualTo("toUserId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<FriendRequest> requests = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            FriendRequest fr = doc.toObject(FriendRequest.class);
                            fr.setId(doc.getId());
                            requests.add(fr);
                        }
                        callback.onSuccess(requests);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void acceptFriendRequest(FriendRequest request, OnCompleteListener<Void> callback) {
        // 1. Dodaj u friend list oba korisnika
        getUserById(request.getFromUserId(), new UserCallback() {
            @Override
            public void onSuccess(User fromUser) {
                getUserById(request.getToUserId(), new UserCallback() {
                    @Override
                    public void onSuccess(User toUser) {
                        fromUser.getFriendIds().add(toUser.getUserId());
                        toUser.getFriendIds().add(fromUser.getUserId());
                        updateUser(fromUser);
                        updateUser(toUser);

                        // 2. Obriši zahtev
                        remoteDb.getFirestore()
                                .collection("friendRequests")
                                .document(request.getId())
                                .delete()
                                .addOnCompleteListener(callback);
                    }
                    @Override
                    public void onFailure(Exception e) { callback.onComplete(null); }
                });
            }
            @Override
            public void onFailure(Exception e) { callback.onComplete(null); }
        });
    }

    public void declineFriendRequest(FriendRequest request, OnCompleteListener<Void> callback) {
        remoteDb.getFirestore()
                .collection("friendRequests")
                .document(request.getId())
                .delete()
                .addOnCompleteListener(callback);
    }

    public void getCurrentUserFriends(String currentUserId, UserIdsCallback callback) {
        getUserById(currentUserId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onResult(user.getFriendIds());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onResult(new ArrayList<>());
            }
        });
    }

    public void getPendingFriendRequests(String currentUserId, UserIdsCallback callback) {
        remoteDb.getFirestore()
                .collection("friendRequests")
                .whereEqualTo("fromUserId", currentUserId) // zahtevi koje je poslao trenutni user
                .get()
                .addOnCompleteListener(task -> {
                    List<String> pendingIds = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            FriendRequest fr = doc.toObject(FriendRequest.class);
                            pendingIds.add(fr.getToUserId()); // UID korisnika kome je poslat zahtev
                        }
                    }
                    callback.onResult(pendingIds);
                });
    }
    public void getPendingAndIncomingFriendRequests(String currentUserId, BiConsumer<List<String>, List<String>> callback) {
        // Lista za zahteve koje smo poslali
        remoteDb.getFirestore()
                .collection("friendRequests")
                .whereEqualTo("fromUserId", currentUserId)
                .get()
                .addOnCompleteListener(taskSent -> {
                    List<String> pendingIds = new ArrayList<>();
                    if (taskSent.isSuccessful() && taskSent.getResult() != null) {
                        for (DocumentSnapshot doc : taskSent.getResult()) {
                            FriendRequest fr = doc.toObject(FriendRequest.class);
                            pendingIds.add(fr.getToUserId());
                        }
                    }

                    // Sada uzmemo i incoming zahteve
                    remoteDb.getFirestore()
                            .collection("friendRequests")
                            .whereEqualTo("toUserId", currentUserId)
                            .get()
                            .addOnCompleteListener(taskReceived -> {
                                List<String> incomingIds = new ArrayList<>();
                                if (taskReceived.isSuccessful() && taskReceived.getResult() != null) {
                                    for (DocumentSnapshot doc : taskReceived.getResult()) {
                                        FriendRequest fr = doc.toObject(FriendRequest.class);
                                        incomingIds.add(fr.getFromUserId());
                                    }
                                }

                                // prosleđujemo obe liste callback-u
                                callback.accept(pendingIds, incomingIds);
                            });
                });
    }


    public interface UserIdsCallback {
        void onResult(List<String> ids);
    }


    // Callback interface
    public interface FriendRequestCallback {
        void onSuccess(List<FriendRequest> requests);
        void onFailure(Exception e);
    }
    public void getUsernameById(String userId, UserCallback callback) {
        getUserById(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    public void hasAlliance(String userId, AllianceCheckCallback callback) {
        remoteDb.getFirestore().collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String allianceId = document.getString("allianceId");
                        boolean hasAlliance = allianceId != null && !allianceId.isEmpty();
                        callback.onResult(hasAlliance);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public interface AllianceCheckCallback {
        void onResult(boolean hasAlliance);
    }




    public interface AllianceCallback {
        void onSuccess(String allianceId);
        void onFailure(Exception e);
    }

    public interface GenericCallback {
        void onComplete(boolean success);
    }

    // Kreiranje saveza
    public void createAlliance(String name, String leaderId, AllianceCallback callback) {
        Alliance alliance = new Alliance(name, leaderId);
        remoteDb.getFirestore()
                .collection("alliances")
                .add(alliance)
                .addOnSuccessListener(docRef -> {
                    alliance.setId(docRef.getId());
                    docRef.update("id", docRef.getId());
                    callback.onSuccess(docRef.getId());
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Update korisnika sa allianceId
    public void updateUserAlliance(String userId, String allianceId, GenericCallback callback) {
        remoteDb.getFirestore().collection("users")
                .document(userId)
                .update("allianceId", allianceId)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // Poziv prijatelju ona koja radi
//    public void sendAllianceInvite(String fromUserId, String toUserId, String allianceId, GenericCallback callback) {
//        AllianceInvite invite = new AllianceInvite(fromUserId, toUserId, allianceId);
//        remoteDb.getFirestore()
//                .collection("allianceInvites")
//                .add(invite)
//                .addOnCompleteListener(task ->{
//                    if (task.isSuccessful()) {
//
//
//                        Log.d("AllianceDebug", "Invite added: " + toUserId);
//                    } else {
//                        Log.e("AllianceDebug", "Failed to add invite: " + toUserId, task.getException());
//                    }
//                    callback.onComplete(task.isSuccessful());
//
//                });
//    }
    public void sendAllianceInvite(Context context, String fromUserId, String toUserId, String allianceId, GenericCallback callback) {
        AllianceInvite invite = new AllianceInvite(fromUserId, toUserId, allianceId);

        remoteDb.getFirestore()
                .collection("allianceInvites")
                .add(invite)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AllianceDebug", "Invite added: " + toUserId);

                        // Dohvati username pošiljaoca
                        getUsernameById(fromUserId, new UserCallback() {
                            @Override
                            public void onSuccess(User fromUser) {
                                String senderName = fromUser.getUsername();

                                // Dohvati FCM token primaoca
                                remoteDb.getFirestore()
                                        .collection("users")
                                        .document(toUserId)
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            if (doc.exists() && doc.contains("fcmToken")) {
                                                String fcmToken = doc.getString("fcmToken");
                                                // Pošalji notifikaciju
                                                FcmSender.sendFcmNotification(context, fcmToken,
                                                        "Poziv u savez",
                                                        senderName + " te je pozvao/la u savez!");
                                            }
                                        });

                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("AllianceDebug", "Failed to get sender username", e);
                            }
                        });

                    } else {
                        Log.e("AllianceDebug", "Failed to add invite: " + toUserId, task.getException());
                    }
                    callback.onComplete(task.isSuccessful());
                });
    }



    public void getAllianceById(String allianceId, AlliancePageCallback callback) {
        remoteDb.getFirestore().collection("alliances")
                .document(allianceId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Alliance alliance = document.toObject(Alliance.class);
                        callback.onSuccess(alliance);
                    } else {
                        callback.onFailure(new Exception("Alliance not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface AlliancePageCallback {
        void onSuccess(Alliance alliance);
        void onFailure(Exception e);
    }


    public void disbandAlliance(String allianceId, GenericCallback callback) {
        // Prvo nađi sve korisnike koji su u tom savezu
        remoteDb.getFirestore().collection("users")
                .whereEqualTo("allianceId", allianceId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("allianceId", null);
                    }

                    // Sada obriši savez
                    remoteDb.getFirestore().collection("alliances")
                            .document(allianceId)
                            .delete()
                            .addOnSuccessListener(unused -> //callback.onComplete(true))
                            {
                                // Nakon brisanja saveza, obriši i sve pozive
                                remoteDb.getFirestore().collection("allianceInvites")
                                        .whereEqualTo("allianceId", allianceId)
                                        .get()
                                        .addOnSuccessListener(inviteQuery -> {
                                            WriteBatch batch = remoteDb.getFirestore().batch();
                                            for (DocumentSnapshot inviteDoc : inviteQuery.getDocuments()) {
                                                batch.delete(inviteDoc.getReference());
                                            }
                                            batch.commit()
                                                    .addOnSuccessListener(aVoid -> callback.onComplete(true))
                                                    .addOnFailureListener(e -> callback.onComplete(false));
                                        })
                                        .addOnFailureListener(e -> callback.onComplete(false));
                            })
                            .addOnFailureListener(e -> callback.onComplete(false));
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }
    public void getAllianceInvites(String toUserId, InvitesCallback callback) {
        remoteDb.getFirestore().collection("allianceInvites")
                .whereEqualTo("toUserId", toUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<AllianceInvite> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        AllianceInvite invite = doc.toObject(AllianceInvite.class);
                        invite.setId(doc.getId());

                        // Dohvati username pošiljaoca
                        String fromUserId = invite.getFromUserId();
                        getUsernameById(fromUserId, new UserCallback() {
                            @Override
                            public void onSuccess(User user) {
                               // invite.s(user.getUsername());
                                // notify može biti pozvan u adapteru nakon loadInvites
                            }

                            @Override
                            public void onFailure(Exception e) {
                               //// invite.setFromUsername("Unknown");
                            }
                        });

                        list.add(invite);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void onDeclineInvite(String inviteId, GenericCallback callback) {
        remoteDb.getFirestore().collection("allianceInvites")
                .document(inviteId)
                .delete()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }
    public void removeMemberFromAlliance(String allianceId, String userId, GenericCallback callback) {
        DocumentReference allianceRef = remoteDb.getFirestore().collection("alliances").document(allianceId);
        allianceRef.update("memberIds", FieldValue.arrayRemove(userId))
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }
    public void onAcceptInvite(String allianceId, String userId, GenericCallback callback) {
        DocumentReference allianceRef = remoteDb.getFirestore().collection("alliances").document(allianceId);
        allianceRef.update("memberIds", FieldValue.arrayUnion(userId))
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }
    // Dodavanje korisnika u savez
    public void addMemberToAlliance(String allianceId, String userId, GenericCallback callback) {
        remoteDb.getFirestore()
                .collection("alliances")
                .document(allianceId)
                .update("memberIds", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }
    // Brisanje poziva
    public void deleteInvite(String inviteId, GenericCallback callback) {
        remoteDb.getFirestore()
                .collection("allianceInvites")
                .document(inviteId)
                .delete()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void getAllianceByUserId(String userId, AllianceCheckCallback callback) {
        remoteDb.getFirestore().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.contains("allianceId")) {
                        String allianceId = doc.getString("allianceId");
                        remoteDb.getFirestore().collection("alliances")
                                .document(allianceId)
                                .get()
                                .addOnSuccessListener(allianceDoc -> {
                                    if (allianceDoc.exists()) {
                                        Alliance alliance = allianceDoc.toObject(Alliance.class);
                                        callback.onResult(true);
                                    } else callback.onResult(false);
                                });
                    } else callback.onResult(false);
                });
    }




    public interface InvitesCallback {
        void onSuccess(List<AllianceInvite> invites);
        void onFailure(Exception e);
    }



    public void sendAllianceMessage(AllianceMessage message, GenericCallback callback) {
        remoteDb.getFirestore()
                .collection("allianceMessages")
                .add(message)
                .addOnSuccessListener(docRef -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    // Listener za dobijanje poruka za saveza
    public ListenerRegistration listenAllianceMessages(String allianceId, AllianceMessageCallback callback) {
        return remoteDb.getFirestore()
                .collection("allianceMessages")
                .whereEqualTo("allianceId", allianceId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }

                    List<AllianceMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        AllianceMessage msg = doc.toObject(AllianceMessage.class);
                        msg.setId(doc.getId());
                        messages.add(msg);
                    }
                    callback.onSuccess(messages);
                });
    }



    public interface AllianceMessageCallback {
        void onSuccess(List<AllianceMessage> messages);
        void onFailure(Exception e);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }
    public int calculateBossHP(int level) {
        if (level == 1) return 200;
        int prevHP = calculateBossHP(level - 1);
        return prevHP * 2 + prevHP / 2;
    }
}

