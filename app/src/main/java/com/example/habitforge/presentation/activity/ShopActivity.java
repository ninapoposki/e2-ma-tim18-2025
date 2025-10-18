package com.example.habitforge.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout equipmentContainer;
    private UserRepository userRepository;
    private User currentUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

/// /
        userRepository = new UserRepository(this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

/// ////////
        equipmentContainer = findViewById(R.id.equipment_container);
        db = FirebaseFirestore.getInstance();
        loadEquipment();
    }

    private void loadEquipment() {
        db.collection("equipment")
                .whereNotEqualTo("type", "weapon") // izbacujemo oružje
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Equipment> equipmentList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Equipment eq = new Equipment();
                            eq.setId(doc.getId());
                            eq.setName(doc.getString("name"));
                            eq.setDescription(doc.getString("description"));
                            eq.setImage(doc.getString("image"));
                            eq.setBonus(doc.getDouble("bonus") != null ? doc.getDouble("bonus") : 0.0);
                            eq.setPermanent(doc.getBoolean("permanent") != null && doc.getBoolean("permanent"));
                            eq.setDuration(doc.getLong("duration") != null ? doc.getLong("duration").intValue() : 0);
                            eq.setPriceMultiplier(doc.getDouble("priceMultiplier") != null ? doc.getDouble("priceMultiplier") : 1.0);

                            String typeString = doc.getString("type");
                            if (typeString != null) {
                                try {
                                    EquipmentType type = EquipmentType.valueOf(typeString.toUpperCase());
                                    eq.setType(type);
                                    if (type != EquipmentType.WEAPON) equipmentList.add(eq);
                                } catch (IllegalArgumentException e) {
                                    equipmentList.add(eq);
                                }
                            } else {
                                equipmentList.add(eq);
                            }
                        }
                        displayEquipment(equipmentList);
                    }
                });
    }

    private void displayEquipment(List<Equipment> equipmentList) {
        equipmentContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Equipment eq : equipmentList) {
            View itemView = inflater.inflate(R.layout.item_equipment, equipmentContainer, false);

            TextView name = itemView.findViewById(R.id.equipment_name);
            TextView type = itemView.findViewById(R.id.equipment_type);
            ImageView image = itemView.findViewById(R.id.equipment_image);
            TextView description = itemView.findViewById(R.id.equipment_description);
            Button buyButton = itemView.findViewById(R.id.equipment_buy_button);

            name.setText(eq.getName());
            type.setText(eq.getType() != null ? eq.getType().toString() : "");
            description.setText(eq.getDescription());

            int imageRes = getResources().getIdentifier(eq.getImage(), "drawable", getPackageName());
            if (imageRes != 0) image.setImageResource(imageRes);

            buyButton.setOnClickListener(v -> {
                if (currentUser == null) return;

                // 1️⃣ Proveri da li korisnik ima dovoljno novčića
                long cena = (long) (currentUser.getCoins() * eq.getPriceMultiplier());
                if (currentUser.getCoins() < cena) {
                    Toast.makeText(this, "Nemaš dovoljno novčića!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2️⃣ Napravi UserEquipment objekat iz Equipment-a, prema tipu
                UserEquipment newItem;
                switch (eq.getType()) {
                    case POTION:
                        newItem = new UserEquipment(
                                UUID.randomUUID().toString(),   // id
                                eq.getId(),
                                eq.getType()   // EquipmentType
                        );
                        break;

                    case CLOTHING:
                        newItem = new UserEquipment(
                                UUID.randomUUID().toString(),    // id
                                eq.getId(),
                                eq.getType(),    // EquipmentType
                                eq.getBonus(),   // effect
                                eq.getDuration() // duration
                        );
                        break;

                    case WEAPON:
                        newItem = new UserEquipment(
                                UUID.randomUUID().toString(),    // id
                                eq.getId(),
                                eq.getType(),    // EquipmentType
                                eq.getBonus(),   // effect
                                1                // level (početni)
                        );
                        break;

                    default:
                        throw new IllegalStateException("Nepoznat tip opreme: " + eq.getType());
                }

                // 3️⃣ Dodaj opremu korisniku
                userRepository.addEquipmentToUser(currentUser, newItem);

                // 4️⃣ Oduzmi novčiće i sačuvaj
                currentUser.setCoins((int) (currentUser.getCoins() - cena));
                userRepository.updateUser(currentUser);

                Toast.makeText(this, "Kupio si " + eq.getName() + "!", Toast.LENGTH_SHORT).show();
            });


//            buyButton.setOnClickListener(v -> {
//                // TODO: logika kupovine
//                if (currentUser == null) return;
//
//                // 1️⃣ Proveri da li korisnik ima dovoljno novčića
//                long cena = (long) (currentUser.getCoins() * eq.getPriceMultiplier());
//                if (currentUser.getCoins() < cena) {
//                    Toast.makeText(this, "Nemaš dovoljno novčića!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // 2️⃣ Napravi UserEquipment objekat iz Equipment-a
//                UserEquipment newItem = new UserEquipment(
//                        eq.getName(),              // id opreme
//                        eq.getType(),            // EquipmentType
//                        1,                       // amount (početna količina)
//                        false,                   // active (nije aktivna odmah po kupovini)
//                        eq.getDuration(),        // trajanje ako postoji
//                        1,           // level opreme
//                        eq.getBonus()            // effect (bonus koji daje)
//                );
//
//                // 3️⃣ Dodaj opremu korisniku
//                userRepository.addEquipmentToUser(currentUser, newItem);
//
//                // 4️⃣ Oduzmi novčiće i sačuvaj u bazi
//                currentUser.setCoins((int) (currentUser.getCoins() - cena));
//
//                userRepository.updateUser(currentUser);
//
//                Toast.makeText(this, "Kupio si " + eq.getName() + "!", Toast.LENGTH_SHORT).show();
//            });

            equipmentContainer.addView(itemView);
        }
    }
}
