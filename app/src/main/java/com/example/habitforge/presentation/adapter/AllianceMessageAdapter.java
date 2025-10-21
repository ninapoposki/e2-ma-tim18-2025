package com.example.habitforge.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.AllianceMessage;

import java.util.ArrayList;
import java.util.List;

public class AllianceMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<AllianceMessage> messages = new ArrayList<>();
    private final String currentUserId;

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    public AllianceMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<AllianceMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? TYPE_RIGHT : TYPE_LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == TYPE_RIGHT ? R.layout.item_message_right : R.layout.item_message_left, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MessageViewHolder) holder).bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textContent, textTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.text_message_username);
            textContent = itemView.findViewById(R.id.text_message_content);
            textTime = itemView.findViewById(R.id.text_message_time);
        }

        public void bind(AllianceMessage msg) {
            textUsername.setText(msg.getSenderName());
            textContent.setText(msg.getContent());
            textTime.setText(android.text.format.DateFormat.format("HH:mm", msg.getTimestamp()));
        }
    }
}
