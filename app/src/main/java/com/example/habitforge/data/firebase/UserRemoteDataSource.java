//package com.example.habitforge.data.firebase;
//
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.example.habitforge.application.model.User;
//import android.util.Log;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.Timestamp;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class UserRemoteDataSource {
//    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    public void registerUser(String email, String password, String username, String avatar,
//                             OnCompleteListener<Void> callback) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser fbUser = mAuth.getCurrentUser();
//                        if (fbUser != null) {
//                            fbUser.sendEmailVerification()
//                                    .addOnCompleteListener(verifyTask -> {
//                                        if (verifyTask.isSuccessful()) {
//                                            String userId = fbUser.getUid();
//                                            User newUser = new User(
//                                                    userId,
//                                                    email,
//                                                    username,
//                                                    avatar,
//                                                    false
//                                            );
//                                            db.collection("users").document(userId)
//                                                    .set(newUser)
//                                                    .addOnCompleteListener(callback);
//                                        } else {
//                                            Log.e("Auth", "Greška pri slanju email verifikacije", verifyTask.getException());
//                                        }
//                                    });
//                        }
//                    } else {
//                        Log.e("Auth", "Registracija nije uspela", task.getException());
//                    }
//                });
//    }
//
//    public void checkUserActivation(FirebaseUser fbUser, OnCompleteListener<Boolean> callback) {
//        fbUser.reload().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                boolean verified = fbUser.isEmailVerified();
//                callback.onComplete(Task.forResult(verified));
//            } else {
//                callback.onComplete(Task.forException(task.getException()));
//            }
//        });
//    }
//
//    public void deleteUser(FirebaseUser fbUser) {
//        if (fbUser != null) {
//            db.collection("users").document(fbUser.getUid()).delete();
//            fbUser.delete();
//        }
//    }
//}


package com.example.habitforge.data.firebase;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.habitforge.application.model.User;

public class UserRemoteDataSource {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserRemoteDataSource() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    // --- AUTH REGISTRACIJA ---
    public void createUserAuth(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    // --- AUTH LOGIN ---
    public void signInUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    // --- FIRESTORE: ČUVANJE KORISNIKA ---
    public void saveUserDocument(User user) {
        if (user.getUserId() != null) {
            db.collection("users")
                    .document(user.getUserId())
                    .set(user);
        }
    }

    // --- FIRESTORE: UZIMANJE KORISNIKA PO ID ---
    public void fetchUserDocument(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    // --- FIRESTORE: UPDATE AKTIVACIJE ---
    public void updateUserActivation(String userId, boolean activated, OnCompleteListener<Void> listener) {
        db.collection("users")
                .document(userId)
                .update("isActive", activated)
                .addOnCompleteListener(listener);
    }

    // --- FIRESTORE: BRISANJE KORISNIKA ---
    public void deleteUserDocument(String userId, OnCompleteListener<Void> listener) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // --- FIRESTORE: SVI KORISNICI ---
    public void fetchAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .get()
                .addOnCompleteListener(listener);
    }

    // --- SLANJE VERIFIKACIONOG MAILA ---
    public void sendActivationMail() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.sendEmailVerification();
        }
    }

    // --- PROVERA AKTIVACIJE ---
    public boolean checkEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload(); // osveži podatke
            return user.isEmailVerified();
        }
        return false;
    }
    // --- GET FIRESTORE INSTANCE ---
    public FirebaseFirestore getFirestore() {
        return db;
    }

}

