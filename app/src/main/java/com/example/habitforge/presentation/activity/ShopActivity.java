package com.example.habitforge.presentation.activity;

import android.os.Bundle;
import android.util.Log;
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
import com.example.habitforge.application.model.AllianceMission;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.application.service.EquipmentService;
import com.example.habitforge.data.repository.EquipmentRepository;
import com.example.habitforge.application.service.AllianceMissionService;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout equipmentContainer;
    private UserRepository userRepository;
    private User currentUser;
    private AllianceMissionService missionService;
    final int SHOP_HIT_DAMAGE = 2; // koliko HP se skida po kupovini
    final int MAX_SHOP_HITS = 5;   // max 5 puta može da se doda doprinos
    private EquipmentService equipmentService;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        equipmentService = new EquipmentService(this);


/// /
        userRepository = new UserRepository(this);
        missionService = new AllianceMissionService(this);

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
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                userRepository.getUserById(uid, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        currentUser = user;

                        equipmentService.calculateEquipmentPriceFromBoss(uid, eq.getId(), true, new EquipmentService.PriceCallback() {
                            @Override
                            public void onSuccess(double price) {
                                long cena = Math.round(price);

                                if (currentUser == null) {
                                    Toast.makeText(ShopActivity.this, "Korisnik nije učitan!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (cena <= 0) {
                                    Toast.makeText(ShopActivity.this, "Boss nije aktivan — cena je 0.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (currentUser.getCoins() < cena) {
                                    Toast.makeText(ShopActivity.this, "Nemaš dovoljno novčića!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Napravi UserEquipment
                                UserEquipment newItem;
                                switch (eq.getType()) {
                                    case POTION:
                                        newItem = new UserEquipment(
                                                UUID.randomUUID().toString(),
                                                eq.getId(),
                                                eq.getType()
                                        );
                                        break;

                                    case CLOTHING:
                                        newItem = new UserEquipment(
                                                UUID.randomUUID().toString(),
                                                eq.getId(),
                                                eq.getType(),
                                                eq.getBonus(),
                                                eq.getDuration()
                                        );
                                        break;

                                    case WEAPON:
                                        newItem = new UserEquipment(
                                                UUID.randomUUID().toString(),
                                                eq.getId(),
                                                eq.getType(),
                                                eq.getBonus(),
                                                1
                                        );
                                        break;

                                    default:
                                        throw new IllegalStateException("Nepoznat tip opreme: " + eq.getType());
                                }

                                // Dodaj opremu i oduzmi novčiće
                                userRepository.addEquipmentToUser(currentUser, newItem);
                                currentUser.setCoins((int) (currentUser.getCoins() - cena));
                                userRepository.updateUser(currentUser);

                                Toast.makeText(ShopActivity.this, "Kupio si " + eq.getName() + "!", Toast.LENGTH_SHORT).show();

                                // Dodaj napredak u misiji saveza
                                missionService.getAllianceMissions(currentUser.getAllianceId(), new AllianceMissionService.MissionListCallback() {
                                    @Override
                                    public void onSuccess(List<AllianceMission> missions) {
                                        if (missions == null || missions.isEmpty()) return;

                                        AllianceMission mission = missions.get(0);
                                        Map<String, Integer> progress = mission.getProgress();
                                        int currentDamage = 0;
                                        if (progress != null && progress.containsKey(currentUser.getUserId())) {
                                            currentDamage = progress.get(currentUser.getUserId());
                                        }

                                        int currentShopHits = currentDamage / SHOP_HIT_DAMAGE;
                                        if (currentShopHits >= MAX_SHOP_HITS) {
                                            Log.i("ShopActivity", "Korisnik je već iskoristio svih 5 shop bonusa. Ne skidamo više HP.");
                                            return;
                                        }

                                        missionService.addMemberProgress(
                                                mission.getId(),
                                                currentUser.getUserId(),
                                                SHOP_HIT_DAMAGE,
                                                () -> Log.i("ShopActivity", "Bossu je oduzeto −2 HP za kupovinu!"),
                                                () -> Log.e("ShopActivity", "Greška pri ažuriranju misije!")
                                        );
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("ShopActivity", "Greška pri dohvatanju misije: " + e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ShopActivity.this, "Greška pri računanju cene: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ShopActivity.this, "Greška pri učitavanju korisnika!", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            equipmentContainer.addView(itemView);
        }
    }


//    private void displayEquipment(List<Equipment> equipmentList) {
//        equipmentContainer.removeAllViews();
//        LayoutInflater inflater = LayoutInflater.from(this);
//
//        for (Equipment eq : equipmentList) {
//            View itemView = inflater.inflate(R.layout.item_equipment, equipmentContainer, false);
//
//            TextView name = itemView.findViewById(R.id.equipment_name);
//            TextView type = itemView.findViewById(R.id.equipment_type);
//            ImageView image = itemView.findViewById(R.id.equipment_image);
//            TextView description = itemView.findViewById(R.id.equipment_description);
//            Button buyButton = itemView.findViewById(R.id.equipment_buy_button);
//
//            name.setText(eq.getName());
//            type.setText(eq.getType() != null ? eq.getType().toString() : "");
//            description.setText(eq.getDescription());
//
//            int imageRes = getResources().getIdentifier(eq.getImage(), "drawable", getPackageName());
//            if (imageRes != 0) image.setImageResource(imageRes);
//
//            buyButton.setOnClickListener(v -> {
//                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                userRepository.getUserById(uid, new UserRepository.UserCallback() {
//                    @Override
//                    public void onSuccess(User user) {
//                        currentUser = user;
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        // obradi grešku
//                    }
//                });
//               // if (currentUser == null) return;
//
//                // 1️⃣ Proveri da li korisnik ima dovoljno novčića
//               // long cena = (long) (currentUser.getCoins() * eq.getPriceMultiplier());
//
//
//
//                equipmentService.calculateEquipmentPriceFromBoss(uid, eq.getId(), true, new EquipmentService.PriceCallback() {
//                    @Override
//                    public void onSuccess(double price) {
//                        long cena = Math.round(price);
//
//                        if (currentUser == null) {
//                            Toast.makeText(ShopActivity.this, "Korisnik nije učitan!", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        if (cena <= 0) {
//                            Toast.makeText(ShopActivity.this, "Boss nije aktivan — cena je 0.", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//
//                        if (currentUser.getCoins() < cena) {
//                            Toast.makeText(ShopActivity.this, "Nemaš dovoljno novčića!", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//
//                        // 2️⃣ Napravi UserEquipment objekat iz Equipment-a, prema tipu
//                        UserEquipment newItem;
//                        switch (eq.getType()) {
//                            case POTION:
//                                newItem = new UserEquipment(
//                                        UUID.randomUUID().toString(),   // id
//                                        eq.getId(),
//                                        eq.getType()   // EquipmentType
//                                );
//                                break;
//
//                            case CLOTHING:
//                                newItem = new UserEquipment(
//                                        UUID.randomUUID().toString(),    // id
//                                        eq.getId(),
//                                        eq.getType(),    // EquipmentType
//                                        eq.getBonus(),   // effect
//                                        eq.getDuration() // duration
//                                );
//                                break;
//
//                            case WEAPON:
//                                newItem = new UserEquipment(
//                                        UUID.randomUUID().toString(),    // id
//                                        eq.getId(),
//                                        eq.getType(),    // EquipmentType
//                                        eq.getBonus(),   // effect
//                                        1                // level (početni)
//                                );
//                                break;
//
//                            default:
//                                throw new IllegalStateException("Nepoznat tip opreme: " + eq.getType());
//                        }
//
//                        //Dodaj opremu korisniku
//                        userRepository.addEquipmentToUser(currentUser, newItem);
//                        // userRepository.receiveEquipmentByBoss(currentUser, newItem);
//
//                        //Oduzmi novčiće i sačuvaj
//                        currentUser.setCoins((int) (currentUser.getCoins() - cena));
//                        userRepository.updateUser(currentUser);
//
//                        Toast.makeText(ShopActivity.this, "Kupio si " + eq.getName() + "!", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        Toast.makeText(ShopActivity.this, "Greška pri računanju cene: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                });
//                    });
//                Toast.makeText(this, "Kupio si " + eq.getName() + "!", Toast.LENGTH_SHORT).show();
//                // Dodaj napredak u specijalnoj misiji saveza (−2 HP)
//                missionService.getAllianceMissions(currentUser.getAllianceId(), new AllianceMissionService.MissionListCallback() {
//                    @Override
//                    public void onSuccess(List<AllianceMission> missions) {
//                        if (missions == null || missions.isEmpty()) return;
//                        AllianceMission mission = missions.get(0);
//
//                        Map<String, Integer> progress = mission.getProgress();
//                        int currentDamage = 0;
//                        if (progress != null && progress.containsKey(currentUser.getUserId())) {
//                            currentDamage = progress.get(currentUser.getUserId());
//                        }
//
//                        // Računamo koliko puta je korisnik već dobio HP damage bonuse iz SHOP-a
//                        int currentShopHits = currentDamage / SHOP_HIT_DAMAGE;
//
//                        if (currentShopHits >= MAX_SHOP_HITS) {
//                            // I dalje može da kupuje, ali se više ne dodaje HP damage
//                            Log.i("ShopActivity", "Korisnik je već iskoristio svih 5 shop bonusa. Ne skidamo više HP.");
//                            return;
//                        }
//
//                        // ⚔️ Dodaj 2 HP damage bossa
//                        missionService.addMemberProgress(
//                                mission.getId(),
//                                currentUser.getUserId(),
//                                SHOP_HIT_DAMAGE,
//                                () -> Log.i("ShopActivity", "Bossu je oduzeto −2 HP za kupovinu!"),
//                                () -> Log.e("ShopActivity", "Greška pri ažuriranju misije!")
//                        );
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        Log.e("ShopActivity", "Greška pri dohvatanju misije: " + e.getMessage());
//                    }
//                });
//
//
//            });
//
//            equipmentContainer.addView(itemView);
//        }
//    }
}
