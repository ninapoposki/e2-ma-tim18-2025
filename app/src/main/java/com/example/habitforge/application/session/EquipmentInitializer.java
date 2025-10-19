package com.example.habitforge.application.session;

import android.util.Log;

import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class EquipmentInitializer {

    private FirebaseFirestore db;

    public EquipmentInitializer() {
        db = FirebaseFirestore.getInstance();
    }

    public void initializeEquipment() {
        List<Equipment> equipmentList = Arrays.asList(
                new Equipment("potion1", "Napitak snage +20%", EquipmentType.POTION, 0.20, false, 1, 0.5, "potion1", "Jednokratni napitak: povećava snagu za 20%."),
                new Equipment("potion2", "Napitak snage +40%", EquipmentType.POTION, 0.40, false, 1, 0.7, "potion2", "Jednokratni napitak: povećava snagu za 40%."),
                new Equipment("potion3", "Trajni napitak +5%", EquipmentType.POTION, 0.05, true, 0, 2.0, "potion3", "Trajno povećava snagu za 5%."),
                new Equipment("potion4", "Trajni napitak +10%", EquipmentType.POTION, 0.10, true, 0, 10.0, "potion4", "Trajno povećava snagu za 10%."),
                new Equipment("clothing1", "Rukavice", EquipmentType.CLOTHING, 0.10, false, 2, 0.6, "clothing1", "Povećavaju snagu za 10% tokom 2 borbe."),
                new Equipment("clothing2", "Štit", EquipmentType.CLOTHING, 0.10, false, 2, 0.6, "clothing2", "Povećava šansu uspešnog napada za 10% tokom 2 borbe."),
                new Equipment("clothing3", "Čizme", EquipmentType.CLOTHING, 0.40, false, 2, 0.8, "clothing3", "Povećavaju šansu za dodatni napad za 40% tokom 2 borbe."),
                new Equipment("weapon1", "Mač", EquipmentType.WEAPON, 0.05, true, 0, 0.0, "weapon1", "Trajno povećava snagu za 5%."),
                new Equipment("weapon2", "Luk i strela", EquipmentType.WEAPON, 0.05, true, 0, 0.0, "weapon2", "Trajno povećava dobijeni novac za 5%.")
        );

        for (Equipment eq : equipmentList) {
            // Proveri da li već postoji po ID-u
            db.collection("equipment")
                    .whereEqualTo("id", eq.getId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            if (snapshot.isEmpty()) {
                                // Ako ne postoji, dodaj
                                db.collection("equipment")
                                        .document(eq.getId())
                                        .set(eq)
                                        .addOnSuccessListener(documentReference ->
                                                Log.d("Firestore", "Added equipment: " + eq.getName()))
                                        .addOnFailureListener(e ->
                                                Log.w("Firestore", "Error adding equipment", e));
                            } else {
                                Log.d("Firestore", "Equipment already exists: " + eq.getName());
                            }
                        } else {
                            Log.w("Firestore", "Error checking equipment", task.getException());
                        }
                    });
        }
    }
}
