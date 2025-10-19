package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.habitforge.application.model.Equipment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EquipmentRepository {

    private final FirebaseFirestore db;

    public EquipmentRepository(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    // 📦 Callback interfejs
    public interface EquipmentCallback {
        void onSuccess(Equipment equipment);
        void onFailure(Exception e);
    }

    // 🔍 Dohvatanje opreme po ID-ju
    public void getEquipmentById(String equipmentId, @NonNull EquipmentCallback callback) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Equipment ID is null or empty"));
            return;
        }

        db.collection("equipment")
                .document(equipmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Equipment equipment = documentSnapshot.toObject(Equipment.class);
                        callback.onSuccess(equipment);
                    } else {
                        callback.onFailure(new Exception("Equipment not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EquipmentRepo", "Greška pri učitavanju opreme", e);
                    callback.onFailure(e);
                });
    }
}
