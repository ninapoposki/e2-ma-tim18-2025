package com.example.habitforge.presentation.activity.ui.equipment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habitforge.application.service.UserService;
import com.example.habitforge.databinding.FragmentEquipmentBinding;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.presentation.adapter.UserEquipmentAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class EquipmentFragment extends Fragment {

    private FragmentEquipmentBinding binding;
    private User currentUser;
    private  FirebaseAuth auth;
    private UserEquipmentAdapter adapter;
    private UserService userService;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();

        userService = new UserService(requireContext());
        binding = FragmentEquipmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerView setup
        binding.recyclerEquipment.setLayoutManager(new LinearLayoutManager(getContext()));

        // Učitaj korisnika iz Firebase
        loadCurrentUser();

        return root;
    }

    private void loadCurrentUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            currentUser = documentSnapshot.toObject(User.class);
                            if (currentUser != null) {
                                displayEquipment(currentUser.getEquipment());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // loguj grešku
                        e.printStackTrace();
                    });
        }
    }

    private void displayEquipment(List<UserEquipment> equipmentList){
        adapter = new UserEquipmentAdapter(equipmentList, (item, position) -> {
            // Aktiviraj item
            item.setActive(true);

            // Update u Firebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUser.getUserId())
                    .update("equipment", currentUser.getEquipment())
                    .addOnSuccessListener(aVoid -> {
                        // Osveži samo taj item u RecyclerView-u
                        adapter.notifyItemChanged(position);
                    });
//         OVAKO CE SE ISKORISTITI ODECA I SMANJICE TRAJANJE ZA 1
//            userService.useAllActiveClothing(currentUser, () -> {
//                adapter.notifyDataSetChanged();
//            });

            // OVAKO CE SE U BORBI POTROSITI SVI AKTIVIRANI NAPICI "
//            userService.useAllActivePotions(currentUser, () -> {
//                adapter.notifyDataSetChanged(); // osveži RecyclerView
//            });

        });

        // Postavi adapter na RecyclerView
        binding.recyclerEquipment.setAdapter(adapter);
    }



//    private void activateEquipment(UserEquipment item, int position){
//        // Ovdje logika za aktivaciju
//        item.setActive(true);
//        // Update korisnika u Firebase
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(currentUser.getUserId())
//                .update("equipment", currentUser.getEquipment()).addOnSuccessListener(aVoid -> {
//                    // Osveži samo taj item u RecyclerView-u
//                    adapter.notifyItemChanged(position);
//                });
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
