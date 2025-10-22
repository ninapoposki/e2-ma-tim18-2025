package com.example.habitforge.presentation.activity.ui.alliance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.example.habitforge.R;
import com.example.habitforge.application.model.Alliance;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.service.AllianceMissionService;
import com.example.habitforge.application.service.UserService;
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.presentation.adapter.AllianceMessageAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class AllianceFragment extends Fragment {

    private AllianceViewModel viewModel;
    private TextView textAllianceName, textLeader;
    private ImageView imageLeaderAvatar;
    private LinearLayout layoutMembers;
    private Button buttonDisband, buttonStartMission;
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private Button buttonSend;

    private AllianceMessageAdapter messageAdapter;
    private final AllianceMissionService missionService = new AllianceMissionService(getContext());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textAllianceName = view.findViewById(R.id.text_alliance_name);
        textLeader = view.findViewById(R.id.text_leader);
        imageLeaderAvatar = view.findViewById(R.id.image_leader_avatar);
        layoutMembers = view.findViewById(R.id.layout_members);
        buttonDisband = view.findViewById(R.id.button_disband_alliance);
        recyclerMessages = view.findViewById(R.id.recycler_alliance_messages);
        editMessage = view.findViewById(R.id.edit_message);
        buttonSend = view.findViewById(R.id.button_send_message);
        buttonStartMission = view.findViewById(R.id.button_start_mission);
        buttonStartMission.setVisibility(View.GONE);

        UserService userService = new UserService(requireContext());
        viewModel = new ViewModelProvider(this).get(AllianceViewModel.class);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Praćenje saveza
        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
            textAllianceName.setText(alliance.getName());

            // Učitaj lidera
            viewModel.getUserById(alliance.getLeaderId(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User leader) {
                    textLeader.setText("Leader: " + leader.getUsername());
                    Glide.with(getContext())
                            .load(leader.getAvatar())
                            .placeholder(R.drawable.avatar1)
                            .into(imageLeaderAvatar);

                    boolean isLeader = alliance.getLeaderId().equals(currentUserId);

                    // Ako je lider saveza
                    if (isLeader) {
                        buttonDisband.setVisibility(View.VISIBLE);
                        buttonDisband.setEnabled(!alliance.isMissionStarted());
                        buttonDisband.setAlpha(alliance.isMissionStarted() ? 0.5f : 1f);

                        buttonStartMission.setVisibility(View.VISIBLE);
                        buttonStartMission.setEnabled(true);
                        buttonStartMission.setAlpha(1f);

                        if (alliance.isMissionStarted()) {
                            buttonStartMission.setText("View Mission");
                            buttonStartMission.setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putString("allianceId", alliance.getId());
                                NavHostFragment.findNavController(AllianceFragment.this)
                                        .navigate(R.id.nav_alliance_mission, args);
                            });
                        } else {
                            //  Nema aktivne misije – startuj novu
                            buttonStartMission.setText("Start Mission");
                            buttonStartMission.setOnClickListener(v -> {
                                missionService.startMission(
                                        alliance.getId(),
                                        "Special Alliance Mission",
                                        "Defeat the Alliance Boss together!",
                                        () -> {
                                            Toast.makeText(getContext(), "Mission started!", Toast.LENGTH_SHORT).show();
                                            alliance.setMissionStarted(true);
                                            userService.updateAllianceMissionStatus(alliance.getId(), true, success -> {});
                                            Bundle args = new Bundle();
                                            args.putString("allianceId", alliance.getId());
                                            NavHostFragment.findNavController(AllianceFragment.this)
                                                    .navigate(R.id.nav_alliance_mission, args);
                                        },
                                        () -> Toast.makeText(getContext(), "Error starting mission!", Toast.LENGTH_SHORT).show()
                                );
                            });
                        }

                    } else {
                        //Ako je običan član
                        buttonDisband.setVisibility(View.GONE);

                        if (alliance.isMissionStarted()) {
                            buttonStartMission.setVisibility(View.VISIBLE);
                            buttonStartMission.setText("View Mission");
                            buttonStartMission.setEnabled(true);
                            buttonStartMission.setAlpha(1f);
                            buttonStartMission.setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putString("allianceId", alliance.getId());
                                NavHostFragment.findNavController(AllianceFragment.this)
                                        .navigate(R.id.nav_alliance_mission, args);
                            });
                        } else {
                            buttonStartMission.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    textLeader.setText("Leader: Nepoznat");
                }
            });

            //  Prikaz članova saveza
            layoutMembers.removeAllViews();
            for (String memberId : alliance.getMemberIds()) {
                viewModel.getUserById(memberId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User member) {
                        View memberItem = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_member, layoutMembers, false);
                        TextView memberName = memberItem.findViewById(R.id.text_member_name);
                        ImageView memberAvatar = memberItem.findViewById(R.id.image_member_avatar);
                        memberName.setText(member.getUsername());
                        Glide.with(getContext())
                                .load(member.getAvatar())
                                .placeholder(R.drawable.avatar1)
                                .into(memberAvatar);
                        layoutMembers.addView(memberItem);
                    }

                    @Override
                    public void onFailure(Exception e) {}
                });
            }

            //  Brisanje saveza
            buttonDisband.setOnClickListener(v -> {
                viewModel.disbandAlliance(alliance.getId(), success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Alliance removed!", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(AllianceFragment.this)
                                .navigate(R.id.nav_home);
                    } else {
                        Toast.makeText(getContext(), "Error deleting alliance", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Poruke saveza
        messageAdapter = new AllianceMessageAdapter(currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(messageAdapter);

        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(),
                alliance -> viewModel.listenAllianceMessages(alliance.getId()));

        viewModel.getMessagesLiveData().observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.setMessages(messages);
            recyclerMessages.scrollToPosition(messages.size() - 1);
        });

        buttonSend.setOnClickListener(v -> {
            String content = editMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                String allianceId = viewModel.getAllianceLiveData().getValue().getId();
                viewModel.getUsername(currentUserId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        viewModel.sendAllianceMessage(allianceId, currentUserId, user.getUsername(), content);
                        editMessage.setText("");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Ne mogu da dohvatim username", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        viewModel.loadAllianceForCurrentUser(currentUserId);
    }
}
