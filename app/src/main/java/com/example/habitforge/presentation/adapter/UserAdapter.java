package com.example.habitforge.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onClick(User user);
    }

    private final List<User> users;
    private final OnUserClickListener listener;

    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Koristimo tvoj custom layout user_item.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Popunjavanje podataka
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());
        holder.title.setText(user.getTitle());

        // Avatar logika (npr. "avatar4" → R.drawable.avatar4)
        int avatarResId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(user.getAvatar(), "drawable",
                        holder.itemView.getContext().getPackageName());

        // Ako avatar postoji, postavi ga
        if (avatarResId != 0) {
            holder.avatar.setImageResource(avatarResId);
        } else {
            // Ako ne postoji, koristi podrazumevani avatar
            holder.avatar.setImageResource(R.drawable.avatar1);
        }

        // Klik na korisnika
        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username, email, title;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.image_avatar);
            username = itemView.findViewById(R.id.text_username);
            email = itemView.findViewById(R.id.text_email);
            title = itemView.findViewById(R.id.text_title);
        }
    }
}
