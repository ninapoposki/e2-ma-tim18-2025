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
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.presentation.adapter.AllianceMessageAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class AllianceFragment extends Fragment {

    private AllianceViewModel viewModel;
    private TextView textAllianceName, textLeader, textMembers;
    private ImageView imageLeaderAvatar;
    private LinearLayout layoutMembers;
    private Button buttonDisband;

    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private Button buttonSend;
    private AllianceMessageAdapter messageAdapter;

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
        //textMembers = view.findViewById(R.id.text_members);
        imageLeaderAvatar = view.findViewById(R.id.image_leader_avatar);
        layoutMembers = view.findViewById(R.id.layout_members);
        buttonDisband = view.findViewById(R.id.button_disband_alliance);
        recyclerMessages = view.findViewById(R.id.recycler_alliance_messages);
        editMessage = view.findViewById(R.id.edit_message);
        buttonSend = view.findViewById(R.id.button_send_message);



        viewModel = new ViewModelProvider(this).get(AllianceViewModel.class);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
            textAllianceName.setText(alliance.getName());
          //  textLeader.setText("Leader: " + alliance.getLeaderId());
           // textMembers.setText("Members: " + String.join(", ", alliance.getMemberIds()));

            viewModel.getUserById(alliance.getLeaderId(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User leader) {
                    textLeader.setText("Leader: " + leader.getUsername());
                    // Učitaj avatar, npr. sa Glide
                    Glide.with(getContext())
                            .load(leader.getAvatar())
                            .placeholder(R.drawable.avatar1)
                            .into(imageLeaderAvatar);
                    if (alliance.getLeaderId().equals(currentUserId)) {
                        // Ako je korisnik lider
                        buttonDisband.setVisibility(View.VISIBLE);

                        if (alliance.isMissionStarted()) {
                            // Ako je misija pokrenuta - onemogući dugme
                            buttonDisband.setEnabled(false);
                            buttonDisband.setAlpha(0.5f); // Vizuelno izgleda sivo/deaktivirano
                        } else {
                            // Ako nije pokrenuta, dugme aktivno
                            buttonDisband.setEnabled(true);
                            buttonDisband.setAlpha(1f);
                        }
                    } else {
                        buttonDisband.setVisibility(View.GONE);
                    }
                }



                @Override
                public void onFailure(Exception e) {
                    textLeader.setText("Leader: Nepoznat");
                }
            });

            // Članovi
            layoutMembers.removeAllViews();
            for (String memberId : alliance.getMemberIds()) {
                viewModel.getUserById(memberId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User member) {
                        View memberItem = LayoutInflater.from(getContext()).inflate(R.layout.item_member, layoutMembers, false);
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
                    public void onFailure(Exception e) {
                        // ako ne postoji user
                    }
                });
            }

            buttonDisband.setOnClickListener(v -> {
                viewModel.disbandAlliance(alliance.getId(), success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Alliance has been removed!", Toast.LENGTH_SHORT).show();
                        requireActivity().runOnUiThread(() -> {
                            NavHostFragment.findNavController(this).navigate(R.id.nav_home);
                        });
                    } else {
                        Toast.makeText(getContext(), "Error deleting alliance", Toast.LENGTH_SHORT).show();
                    }
                });
            });


        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
        );

        messageAdapter = new AllianceMessageAdapter(currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(messageAdapter);

        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
            viewModel.listenAllianceMessages(alliance.getId());
        });

        viewModel.getMessagesLiveData().observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.setMessages(messages);
            recyclerMessages.scrollToPosition(messages.size() - 1);
        });

        buttonSend.setOnClickListener(v -> {
            String content = editMessage.getText().toString().trim();
            if (!content.isEmpty()) {
               // String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName(); // ili username iz baze
//                viewModel.sendAllianceMessage(viewModel.getAllianceLiveData().getValue().getId(),
//                        currentUserId, senderName, content);
//                editMessage.setText("");
                String userId = currentUserId;
                String allianceId = viewModel.getAllianceLiveData().getValue().getId();

                // Poziv ViewModel metode koja dohvata username
                viewModel.getUsername(userId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        String senderName = user.getUsername();  // OVDE DOBIJAŠ USERNAME
                        viewModel.sendAllianceMessage(allianceId, userId, senderName, content);
                        editMessage.setText("");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Ne mogu da dohvatim username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        viewModel.loadAllianceForCurrentUser(currentUserId);
    }
}
