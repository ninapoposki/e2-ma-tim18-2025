package com.example.habitforge.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.UserEquipment;

import java.util.List;

public class UserEquipmentAdapter extends RecyclerView.Adapter<UserEquipmentAdapter.ViewHolder> {

    public interface OnActivateClickListener {
        void onActivate(UserEquipment item, int position);
    }

    private final List<UserEquipment> equipmentList;
    private final OnActivateClickListener listener;

    public UserEquipmentAdapter(List<UserEquipment> equipmentList, OnActivateClickListener listener) {
        this.equipmentList = equipmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserEquipment item = equipmentList.get(position);
        holder.tvName.setText(item.getEquipmentId()); // možeš i prikazati ime iz Equipment objekta ako ga imaš
       // holder.btnActivate.setOnClickListener(v -> listener.onActivate(item));
        if(item.isActive()){
            holder.btnActivate.setVisibility(View.GONE);
            holder.tvActivated.setVisibility(View.VISIBLE);
        } else {
            holder.btnActivate.setVisibility(View.VISIBLE);
            holder.tvActivated.setVisibility(View.GONE);
            holder.btnActivate.setOnClickListener(v -> listener.onActivate(item, position));
        }
    }



    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnActivate;
        TextView tvActivated;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEquipmentName);
            btnActivate = itemView.findViewById(R.id.btnActivate);
            tvActivated = itemView.findViewById(R.id.tvActivated);
        }
    }
}
