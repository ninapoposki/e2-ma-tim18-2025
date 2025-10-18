package com.example.habitforge.presentation.activity.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitforge.application.model.User;
import com.example.habitforge.application.session.SessionManager;

import java.util.ArrayList;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<User> mUserLiveData = new MutableLiveData<>();

    public SessionManager sessionManager;
    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment" );
    }

    public void setUserEmail(String email) {
        if (email != null && !email.isEmpty()) {
            mText.setValue("This is gallery fragment. Logged in as: " + email);
        }
    }

    public void setUser(User user) {
        if (user != null) {
            // Popuni default vrednosti za prazne polja
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) user.setAvatar("default_avatar.png");
            if (user.getUsername() == null || user.getUsername().isEmpty()) user.setUsername("Unknown");
            if (user.getLevel() == 0) user.setLevel(1);
            if (user.getTitle() == null || user.getTitle().isEmpty()) user.setTitle("Beginner");
            if (user.getPowerPoints() == 0) user.setPowerPoints(0);
            if (user.getExperiencePoints() == 0) user.setExperiencePoints(0);
            if (user.getCoins() == 0) user.setCoins(0);
            if (user.getBadges() == null) user.setBadges(new ArrayList<>());
            if (user.getEquipment() == null || user.getEquipment().isEmpty()) user.setEquipment(new ArrayList<>());
            // QR kod ignorisemo za sada

            mUserLiveData.setValue(user);
        }

    }
    public LiveData<User> getUserLiveData() {
        return mUserLiveData;
    }
    public LiveData<String> getText() {
        return mText;
    }
}