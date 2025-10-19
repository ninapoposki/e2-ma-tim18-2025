package com.example.habitforge.presentation.activity.ui.friendRequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habitforge.R;
import com.example.habitforge.application.model.FriendRequest;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment {

    private UserRepository userRepository;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository(getContext());
        container = view.findViewById(R.id.requests_container);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadFriendRequests(currentUserId);
    }

    private void loadFriendRequests(String currentUserId) {
        container.removeAllViews(); // isprazni pre nego što dodamo nove

        userRepository.getFriendRequestsForUser(currentUserId, new UserRepository.FriendRequestCallback() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                for (FriendRequest request : requests) {
                    addRequestView(request);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRequestView(FriendRequest request) {
        TextView tv = new TextView(getContext());
        tv.setText("Loading username...");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(tv);

        Button acceptBtn = new Button(getContext());
        acceptBtn.setText("Accept");
        Button declineBtn = new Button(getContext());
        declineBtn.setText("Decline");
        layout.addView(acceptBtn);
        layout.addView(declineBtn);

        container.addView(layout);

        // Dohvati username onoga ko je poslao zahtev
        userRepository.getUsernameById(request.getFromUserId(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.example.habitforge.application.model.User user) {
                tv.setText(user.getUsername());
            }

            @Override
            public void onFailure(Exception e) {
                tv.setText("Unknown user");
            }
        });

        acceptBtn.setOnClickListener(v -> {
            userRepository.acceptFriendRequest(request, task -> {
                container.removeView(layout); // odmah ukloni iz UI
                Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
            });
        });

        declineBtn.setOnClickListener(v -> {
            userRepository.declineFriendRequest(request, task -> {
                container.removeView(layout); // odmah ukloni iz UI
                Toast.makeText(getContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
