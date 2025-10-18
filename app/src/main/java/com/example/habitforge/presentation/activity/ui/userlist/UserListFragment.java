package com.example.habitforge.presentation.activity.ui.userlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.User;
import com.example.habitforge.presentation.adapter.UserListAdapter;

import java.util.List;

public class UserListFragment extends Fragment implements UserListAdapter.OnUserClickListener {

    private UserListViewModel viewModel;
    private UserListAdapter adapter;

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
    }

    @Override
    public void onUserClick(User user) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_userListFragment_to_galleryFragment, bundle);
    }
}
