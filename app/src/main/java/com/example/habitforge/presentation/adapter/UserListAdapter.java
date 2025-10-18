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

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserListAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    // Postavljanje nove liste korisnika
    public void submitList(List<User> list) {
        this.users = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Popunjavanje osnovnih podataka
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail() != null ? user.getEmail() : "Nema email");
        holder.title.setText(user.getTitle() != null ? user.getTitle() : "");

        // Avatar logika
        String avatarName = user.getAvatar();
        if (avatarName == null || avatarName.isEmpty()) {
            avatarName = "avatar1"; // default
        } else {
            // ukloni ekstenziju ako postoji (.png)
            avatarName = avatarName.replace(".png", "");
        }

        int resId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(avatarName, "drawable",
                        holder.itemView.getContext().getPackageName());

        if (resId != 0) {
            holder.avatar.setImageResource(resId);
        } else {
            holder.avatar.setImageResource(R.drawable.avatar1);
        }

        // Klik na korisnika
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
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
