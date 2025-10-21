package com.example.habitforge.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.AllianceInvite;

import java.util.List;
import java.util.function.BiConsumer;

public class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.InviteViewHolder> {

    private List<AllianceInvite> invites;
    private BiConsumer<AllianceInvite, Integer> acceptCallback;
    private BiConsumer<AllianceInvite, Integer> declineCallback;

    public InvitesAdapter(List<AllianceInvite> invites,
                          BiConsumer<AllianceInvite, Integer> acceptCallback,
                          BiConsumer<AllianceInvite, Integer> declineCallback) {
        this.invites = invites;
        this.acceptCallback = acceptCallback;
        this.declineCallback = declineCallback;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_invite, parent, false);
        return new InviteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        AllianceInvite invite = invites.get(position);
        holder.tvMessage.setText(invite.getFromUserId() + " has invited you to their alliance");

        holder.btnAccept.setOnClickListener(v -> acceptCallback.accept(invite, position));
        holder.btnDecline.setOnClickListener(v -> declineCallback.accept(invite, position));
    }

    @Override
    public int getItemCount() {
        return invites.size();
    }

    static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        Button btnAccept, btnDecline;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
