package com.example.habitforge.presentation.activity.ui.alliance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitforge.application.model.Alliance;
import com.example.habitforge.application.model.AllianceMessage;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AllianceViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<AllianceMessage>> messagesLiveData = new MutableLiveData<>();

    public LiveData<List<AllianceMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }
    private ListenerRegistration messagesListener;

    public AllianceViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public LiveData<Alliance> getAllianceLiveData() {
        return allianceLiveData;
    }
    public void getUserById(String userId, UserRepository.UserCallback callback) {
        userRepository.getUserById(userId, callback);
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

    public void disbandAlliance(String allianceId, UserRepository.GenericCallback callback) {
        userRepository.disbandAlliance(allianceId, callback);
    }


    public void sendAllianceMessage(String allianceId, String senderId, String senderName, String content) {
        AllianceMessage msg = new AllianceMessage(allianceId, senderId, senderName, content);
        userRepository.sendAllianceMessage(msg, success -> {
            if (!success) {
                errorLiveData.postValue("Failed to send message");
            }
        });
    }

    public void getUsername(String userId, UserRepository.UserCallback callback) {
        userRepository.getUsernameById(userId, callback);
    }

    public void listenAllianceMessages(String allianceId) {
        if (messagesListener != null) messagesListener.remove();
        messagesListener = userRepository.listenAllianceMessages(allianceId, new UserRepository.AllianceMessageCallback() {
            @Override
            public void onSuccess(List<AllianceMessage> messages) {
                messagesLiveData.postValue(messages);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue("Failed to load messages: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messagesListener != null) messagesListener.remove();
    }

}
