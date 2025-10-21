package com.example.habitforge.presentation.activity.ui.allianceReq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Alliance;
import com.example.habitforge.application.model.AllianceInvite;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.presentation.adapter.InvitesAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AllianceInvitesFragment extends Fragment {

    private RecyclerView rvInvites;
    private InvitesAdapter adapter;
    private List<AllianceInvite> invites = new ArrayList<>();
    private String currentUserId;

    private UserRepository userRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_invites, container, false);
        rvInvites = view.findViewById(R.id.rvInvites);

        rvInvites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvitesAdapter(invites,
                (invite, position) -> acceptInvite(invite),
                (invite, position) -> declineInvite(invite));
        rvInvites.setAdapter(adapter);

        userRepository = new UserRepository(getContext());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadInvites();

        return view;
    }

    private void loadInvites() {
        userRepository.getAllianceInvites(currentUserId, new UserRepository.InvitesCallback() {
            @Override
            public void onSuccess(List<AllianceInvite> list) {
                invites.clear();
                invites.addAll(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju poziva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptInvite(AllianceInvite invite) {
        // prvo dohvati trenutnog korisnika
        userRepository.getUserById(currentUserId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User currentUser) {
                String oldAllianceId = currentUser.getAllianceId();
                if (oldAllianceId != null && !oldAllianceId.isEmpty()) {
                    // korisnik je već u savezu, proveri da li je misija aktivna
                    userRepository.getAllianceById(oldAllianceId, new UserRepository.AlliancePageCallback() {
                        @Override
                        public void onSuccess(Alliance oldAlliance) {
                            if (oldAlliance.isMissionStarted()) {
                                Toast.makeText(getContext(),
                                        "Ne možete napustiti trenutni savez dok traje misija",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // može da pređe u novi savez
                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                        .setTitle("Change Alliance")
                                        .setMessage("You are already in an alliance. Are you sure you want to join this new alliance?")
                                        .setPositiveButton("Yes", (dialog, which) -> switchAlliance(oldAllianceId, invite.getAllianceId(), invite))
                                        .setNegativeButton("No", null)
                                        .show();
                               // switchAlliance(oldAllianceId, invite.getAllianceId(), invite);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Greška pri učitavanju saveza", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // korisnik nije u savezu, samo dodaj u novi
                    switchAlliance(null, invite.getAllianceId(), invite);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchAlliance(String oldAllianceId, String newAllianceId, AllianceInvite invite) {
        if (oldAllianceId != null) {
            userRepository.removeMemberFromAlliance(oldAllianceId, currentUserId, ignored -> {
                addToNewAlliance(newAllianceId, invite);
            });
        } else {
            addToNewAlliance(newAllianceId, invite);
        }
    }

    private void addToNewAlliance(String newAllianceId, AllianceInvite invite) {
        userRepository.updateUserAlliance(currentUserId, newAllianceId, success -> {
            userRepository.addMemberToAlliance(newAllianceId, currentUserId, addSuccess -> {
                userRepository.deleteInvite(invite.getId(), ignored -> loadInvites());
            });
        });
    }

    private void declineInvite(AllianceInvite invite) {
        userRepository.deleteInvite(invite.getId(), ignored -> loadInvites());
    }
}
