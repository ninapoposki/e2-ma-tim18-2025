package com.example.habitforge.presentation.activity.ui.userlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitforge.application.model.FriendRequest;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public void sendFriendRequest(String fromUserId, String toUserId, UserRepository.FriendRequestCallback callback) {
        userRepository.sendFriendRequest(fromUserId, toUserId, callback);
    }

//    public void fetchCurrentUserFriends(String currentUserId, Consumer<List<String>> callback) {
//        userRepository.getUserById(currentUserId, new UserRepository.UserCallback() {
//            @Override
//            public void onSuccess(User user) {
//                callback.accept(user.getFriendIds());
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.accept(new ArrayList<>());
//            }
//        });
//    }

//    public void fetchPendingFriendRequests(String currentUserId, Consumer<List<String>> callback) {
//        userRepository.getFriendRequestsForUser(currentUserId, new UserRepository.FriendRequestCallback() {
//            @Override
//            public void onSuccess(List<FriendRequest> requests) {
//                List<String> ids = new ArrayList<>();
//                for (FriendRequest r : requests) {
//                    ids.add(r.getFromUserId());
//                }
//                callback.accept(ids);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.accept(new ArrayList<>());
//            }
//        });
//    }

    //ovo radi prvo
//public void loadFriendsAndPending(String currentUserId, BiConsumer<List<String>, List<String>> callback) {
//    userRepository.getCurrentUserFriends(currentUserId, friendIds -> {
//        userRepository.getPendingFriendRequests(currentUserId, pendingIds -> {
//            callback.accept(friendIds, pendingIds);
//        });
//    });
//}
    public void loadFriendsAndAllRequests(String currentUserId, TriConsumer<List<String>, List<String>, List<String>> callback) {
        // Prvo dohvati prijatelje
        userRepository.getCurrentUserFriends(currentUserId, friendIds -> {
            // Zatim dohvati pending i incoming zahteve
            userRepository.getPendingAndIncomingFriendRequests(currentUserId, (pendingSentIds, incomingIds) -> {
                // callback sa 3 listi: prijatelji, pending koje smo poslali, pending koje smo primili
                callback.accept(friendIds, pendingSentIds, incomingIds);
            });
        });
    }
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }



}
