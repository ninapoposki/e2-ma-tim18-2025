package com.example.habitforge.presentation.activity.ui.store;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout equipmentContainer;

    public ShopFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        equipmentContainer = view.findViewById(R.id.equipment_container);
        db = FirebaseFirestore.getInstance();
        loadEquipment();
        return view;
    }

    private void loadEquipment() {
        db.collection("equipment")
                .whereNotEqualTo("type", "weapon") // izbacujemo oružje
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Equipment> equipmentList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // RUČNO mapiranje iz Firestore-a u Equipment
                            Equipment eq = new Equipment();
                            eq.setId(doc.getId());
                            eq.setName(doc.getString("name"));
                            eq.setDescription(doc.getString("description"));
                            eq.setImage(doc.getString("image"));
                            eq.setBonus(doc.getDouble("bonus") != null ? doc.getDouble("bonus") : 0.0);
                            eq.setPermanent(doc.getBoolean("permanent") != null && doc.getBoolean("permanent"));
                            eq.setDuration(doc.getLong("duration") != null ? doc.getLong("duration").intValue() : 0);
                            eq.setPriceMultiplier(doc.getDouble("priceMultiplier") != null ? doc.getDouble("priceMultiplier") : 1.0);

                            // MAPIRANJE STRING u ENUM
                            String typeString = doc.getString("type");
                            if (typeString != null) {
                                try {
                                    EquipmentType type = EquipmentType.valueOf(typeString.toUpperCase());
                                    eq.setType(type);

                                    // filtriramo oružje
                                    if (type != EquipmentType.WEAPON) {
                                        equipmentList.add(eq);
                                    }

                                } catch (IllegalArgumentException e) {
                                    // nepoznat tip, možeš staviti default ako želiš
                                    // eq.setType(EquipmentType.OTHER);
                                    equipmentList.add(eq); // ako želiš da prikažeš sve osim weapon
                                }
                            } else {
                                // tip nije definisan, dodaj po želji
                                equipmentList.add(eq);
                            }
                        }
                        displayEquipment(equipmentList);
                    }
                });
    }

    private void displayEquipment(List<Equipment> equipmentList) {
        equipmentContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Equipment eq : equipmentList) {
            View itemView = inflater.inflate(R.layout.item_equipment, equipmentContainer, false);

            TextView name = itemView.findViewById(R.id.equipment_name);
            TextView type = itemView.findViewById(R.id.equipment_type);
            ImageView image = itemView.findViewById(R.id.equipment_image);
            TextView description = itemView.findViewById(R.id.equipment_description);
            Button buyButton = itemView.findViewById(R.id.equipment_buy_button);

            name.setText(eq.getName());
            type.setText(eq.getType() != null ? eq.getType().toString() : ""); // prikaz tipa
            description.setText(eq.getDescription());

            // Postavimo drawable resurs na osnovu imena slike
            int imageRes = getResources().getIdentifier(eq.getImage(), "drawable", getContext().getPackageName());
            if (imageRes != 0) image.setImageResource(imageRes);

            buyButton.setOnClickListener(v -> {
                // TODO: logika kupovine (provera korisnikovih novčića)
            });

            equipmentContainer.addView(itemView);
        }
    }

}
