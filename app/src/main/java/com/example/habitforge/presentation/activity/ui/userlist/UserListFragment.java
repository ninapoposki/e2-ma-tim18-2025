package com.example.habitforge.presentation.activity.ui.userlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.FriendRequest;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.presentation.adapter.UserListAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment implements UserListAdapter.OnUserClickListener {

    private UserListViewModel viewModel;
    private UserListAdapter adapter;
    private EditText editAllianceName;
    private TextView buttonCreateAlliance;
    private List<String> selectedUserIds = new ArrayList<>(); // ID-jevi prijatelja koji su odabrani


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserListAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> adapter.submitList(users));
        viewModel.fetchAllUsersExceptCurrent(currentUserId);

        viewModel.loadFriendsAndAllRequests(currentUserId, (friendIds, pendingSentIds, incomingIds) -> {
            // Kombinujemo sve ID-jeve koje treba sakriti dugme:
            List<String> hideButtonIds = new ArrayList<>(friendIds);
            hideButtonIds.addAll(pendingSentIds);
            hideButtonIds.addAll(incomingIds);

            adapter.updateFriendData(hideButtonIds); // prilagodi adapter metodu da prima samo jednu listu
        });

        viewModel.getAllianceCreationStatus().observe(getViewLifecycleOwner(), status -> {
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();

            if (status.equals("Alliance created and invites sent")) {
                editAllianceName.setText("");
                adapter.getSelectedUserIds().clear();
                adapter.notifyDataSetChanged();


                adapter.setShowCheckboxes(false);
                    adapter.clearSelections();
            }
        });

        Button buttonStart = view.findViewById(R.id.button_start_create_alliance);
        LinearLayout layoutCreate = view.findViewById(R.id.layout_create_alliance);

        editAllianceName = view.findViewById(R.id.edit_alliance_name);
        buttonCreateAlliance = view.findViewById(R.id.button_create_alliance);
        buttonStart.setOnClickListener(v -> {


            viewModel.hasAlliance(currentUserId, hasAlliance -> {
                if (hasAlliance) {
                    // Već ima savez
                    Toast.makeText(getContext(),
                            "You are member of alliance or have one. You can not create new one.",
                            Toast.LENGTH_SHORT).show();
                } else {


            layoutCreate.setVisibility(View.VISIBLE);
            adapter.setShowCheckboxes(true); // dodaćemo ovo u adapter
            adapter.notifyDataSetChanged();
            buttonStart.setVisibility(View.GONE);
                }
            });
        });
        buttonCreateAlliance.setOnClickListener(v ->{ createAlliance();
                layoutCreate.setVisibility(View.GONE);
       buttonStart.setVisibility(View.VISIBLE);
//        adapter.setShowCheckboxes(false);
//        adapter.clearSelections(); SAD ZAKOMENTARISALA
        });

    }

    @Override
    public void onUserClick(User user) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_userListFragment_to_galleryFragment, bundle);
    }
    @Override
    public void onSendFriendRequest(User user) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.sendFriendRequest(currentUserId, user.getUserId(), new UserRepository.FriendRequestCallback() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                Toast.makeText(getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                List<String> newList=  adapter.getCurrentUserFriendIds();
                newList.add(user.getUserId());
                adapter.updateFriendData(newList);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAlliance() {
        String allianceName = editAllianceName.getText().toString().trim();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //List<String> invitedIds = adapter.getSelectedUserIds();
        List<String> invitedIds = new ArrayList<>(adapter.getSelectedUserIds());

        viewModel.createAlliance(requireContext(), allianceName, currentUserId, invitedIds);
    }



}
