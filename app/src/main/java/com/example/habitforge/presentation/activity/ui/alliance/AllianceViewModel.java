package com.example.habitforge.presentation.activity.ui.alliance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitforge.application.model.Alliance;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;

public class AllianceViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AllianceViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public LiveData<Alliance> getAllianceLiveData() {
        return allianceLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadAllianceForCurrentUser(String currentUserId) {
        userRepository.getUserById(currentUserId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                String allianceId = user.getAllianceId();
                if (allianceId == null || allianceId.isEmpty()) {
                    errorLiveData.postValue("Niste član nijednog saveza");
                    return;
                }

                userRepository.getAllianceById(allianceId, new UserRepository.AlliancePageCallback() {
                    @Override
                    public void onSuccess(Alliance alliance) {
                        allianceLiveData.postValue(alliance);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        errorLiveData.postValue("Greška pri učitavanju saveza: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue("Greška pri učitavanju korisnika: " + e.getMessage());
            }
        });
    }
}
