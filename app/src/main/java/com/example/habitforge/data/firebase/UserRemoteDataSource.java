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
}

