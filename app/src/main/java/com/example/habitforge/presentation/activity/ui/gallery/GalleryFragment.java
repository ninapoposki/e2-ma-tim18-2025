package com.example.habitforge.presentation.activity.ui.gallery;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.service.EquipmentService;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.databinding.FragmentGalleryBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private UserRepository userRepository;
   private EquipmentService equipmentService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userRepository = new UserRepository(requireContext());
        equipmentService = new EquipmentService(requireContext());

        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        // 1️⃣ Proveravamo da li dolazi userId iz argumenta (kliknuti korisnik)
        String userId = null;
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        // 2️⃣ Ako nema argumenta, uzimamo trenutno prijavljenog korisnika
        if (userId == null) {
            SessionManager sessionManager = new SessionManager(requireContext());
            userId = sessionManager.getUserId();
        }

        // 3️⃣ Ako imamo userId, učitavamo korisnika
        if (userId != null) {
            loadUser(userId, galleryViewModel);
        } else {
            binding.textUsername.setText("Korisnik nije prijavljen!");
        }

        // 4️⃣ Posmatramo LiveData i prikazujemo u UI
        observeUser(galleryViewModel);

        return root;
    }

    private void loadUser(String userId, GalleryViewModel galleryViewModel) {
        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                galleryViewModel.setUser(user);
            }

            @Override
            public void onFailure(Exception e) {
                binding.textUsername.setText("Greška pri učitavanju korisnika");
            }
        });
    }

    private void observeUser(GalleryViewModel galleryViewModel) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String currentUserId = sessionManager.getUserId();

        galleryViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                boolean isOwnProfile = user.getUserId().equals(currentUserId);

                Log.d("GalleryFragment", "User LiveData updated: " + user.toString());

                binding.textUsername.setText(user.getUsername());
                binding.textLevelTitle.setText("Level " + user.getLevel() + " • " + user.getTitle());
                binding.textXp.setText(String.valueOf(user.getExperiencePoints()));
                binding.textPp.setText(String.valueOf(user.getPowerPoints()));
                binding.textCoins.setText(String.valueOf(user.getCoins()));
                binding.textBadges.setText(user.getBadges() != null ? user.getBadges().toString() : "Nema bedževa");
               // binding.textEquipment.setText(user.getEquipment() != null ? user.getEquipment().toString() : "Nema opreme");
                displayUserBadges(user);
                setAvatarImage(user.getAvatar());
                generateQrCode(user.getUserId());

                if (!isOwnProfile) {
                    binding.textEquipmentNotice.setVisibility(View.VISIBLE);
                } else {
                    binding.textEquipmentNotice.setVisibility(View.GONE);
                }

                displayUserEquipment(user, isOwnProfile);

                binding.textPp.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);
                binding.labelPp.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);

                binding.textCoins.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);
                binding.labelCoins.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);


                LinearLayout layoutChangePassword = binding.layoutChangePassword;
                layoutChangePassword.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);

                if (isOwnProfile) {
                    binding.buttonChangePassword.setOnClickListener(v -> {
                        String oldPass = binding.editOldPassword.getText().toString().trim();
                        String newPass = binding.editNewPassword.getText().toString().trim();
                        String confirmPass = binding.editConfirmNewPassword.getText().toString().trim();

                        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                            Toast.makeText(getContext(), "Popunite sva polja", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!newPass.equals(confirmPass)) {
                            Toast.makeText(getContext(), "Lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        changePassword(oldPass, newPass);
                    });
                }
            }
        });
    }

    private void setAvatarImage(String avatarName) {
        if (avatarName != null && !avatarName.isEmpty()) {
            int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
            binding.imageAvatar.setImageResource(resId != 0 ? resId : R.drawable.avatar1);
        } else {
            binding.imageAvatar.setImageResource(R.drawable.avatar1);
        }
    }

    private void generateQrCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            binding.imageQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        if (email == null) return;

        // Re-authenticate
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Lozinka uspešno promenjena", Toast.LENGTH_SHORT).show();
                        binding.editOldPassword.setText("");
                        binding.editNewPassword.setText("");
                        binding.editConfirmNewPassword.setText("");
                    } else {
                        Toast.makeText(getContext(), "Greška: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Stara lozinka nije tačna", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displayUserEquipment(User user, boolean isOwnProfile) {
        LinearLayout equipmentContainer = binding.layoutEquipmentContainer;
        equipmentContainer.removeAllViews();

        List<UserEquipment> equipmentList = user.getEquipment();
        if (equipmentList == null || equipmentList.isEmpty()) {
            Toast.makeText(getContext(), "Nema opreme za prikaz", Toast.LENGTH_SHORT).show();
            return;
        }

        for (UserEquipment ue : equipmentList) {
            // Ako nije moj profil i oprema nije aktivna -> preskoči
            if (!isOwnProfile && !ue.isActive()) continue;

            // Poziv servisa da dobijemo detalje o opremi
            equipmentService.getEquipmentById(ue.getEquipmentId(), new EquipmentService.EquipmentCallback() {
                @Override
                public void onSuccess(Equipment equipment) {
                    // Kada dobijemo opremu, pravimo dinamički prikaz (slika + ime)
                    View equipmentView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_equipment_card, equipmentContainer, false);

                    ImageView image = equipmentView.findViewById(R.id.image_equipment);
                    TextView name = equipmentView.findViewById(R.id.text_equipment_name);

                    name.setText(equipment.getName());

                    int resId = getResources().getIdentifier(equipment.getImage(), "drawable", requireContext().getPackageName());
                    if (resId != 0) {
                        image.setImageResource(resId);
                    }

                    equipmentContainer.addView(equipmentView);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("GalleryFragment", "Greška pri učitavanju opreme: " + e.getMessage());
                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void displayUserBadges(User user) {
        TextView badgeCountText = binding.textBadgeCount;
        LinearLayout badgeContainer = binding.layoutBadgeContainer;

        List<String> badges = user.getBadges();
        int count = (badges != null) ? badges.size() : 0;
        badgeCountText.setText("Broj bedževa: " + count);

        badgeContainer.removeAllViews();

        if (badges == null || badges.isEmpty()) {
            TextView noBadgesText = new TextView(getContext());
            noBadgesText.setText("Nema bedževa");
            noBadgesText.setTextColor(Color.DKGRAY);
            badgeContainer.addView(noBadgesText);
            return;
        }

        for (String badgeName : badges) {
            ImageView badgeView = new ImageView(getContext());
            int resId = getResources().getIdentifier(badgeName, "drawable", requireContext().getPackageName());

            if (resId == 0) {
                resId = R.drawable.badgedef; // default ikonica ako ne postoji
            }

            badgeView.setImageResource(resId);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(8, 0, 8, 0);
            badgeView.setLayoutParams(params);

            badgeContainer.addView(badgeView);
        }
    }

}
