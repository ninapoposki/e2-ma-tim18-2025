package com.example.habitforge.presentation.activity.ui.userlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class UserListViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

    public UserListViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public void fetchAllUsersExceptCurrent(String currentUserId) {
        userRepository.getAllUsers(new UserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                List<User> filtered = new ArrayList<>();
                for (User u : users) {
                    if (!u.getUserId().equals(currentUserId)) {
                        filtered.add(u);
                    }
                }
                usersLiveData.postValue(filtered);
            }

            @Override
            public void onFailure(Exception e) {
                usersLiveData.postValue(new ArrayList<>());
            }
        });
    }
}
