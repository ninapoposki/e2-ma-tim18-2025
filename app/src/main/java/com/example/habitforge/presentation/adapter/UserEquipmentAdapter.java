package com.example.habitforge.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.application.service.EquipmentService;

import java.util.List;

public class UserEquipmentAdapter extends RecyclerView.Adapter<UserEquipmentAdapter.EquipmentViewHolder> {

    public interface OnEquipmentActivateListener {
        void onActivate(UserEquipment item, int position);
    }

    private final List<UserEquipment> equipmentList;
    private final OnEquipmentActivateListener listener;
    private final EquipmentService equipmentService;
    private final Context context;
    private final OnEquipmentUpgradeListener upgradeListener;
    public UserEquipmentAdapter(Context context, List<UserEquipment> equipmentList, OnEquipmentActivateListener listener,  OnEquipmentUpgradeListener upgradeListener) {
        this.context = context;
        this.equipmentList = equipmentList;
        this.listener = listener;
        this.upgradeListener = upgradeListener;
        this.equipmentService = new EquipmentService(context);
    }
    public interface OnEquipmentUpgradeListener {
        void onUpgrade(UserEquipment item, int position);
    }


    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        UserEquipment userEq = equipmentList.get(position);

        // Učitaj ime i sliku iz EquipmentService
        equipmentService.getEquipmentById(userEq.getEquipmentId(), new EquipmentService.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                holder.tvName.setText(equipment.getName());

                int resId = context.getResources()
                        .getIdentifier(equipment.getImage(), "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.imageView.setImageResource(resId);
                } else {
                   // holder.imageView.setImageResource(R.drawable.ic_default_equipment);
                }

                if (EquipmentType.WEAPON.equals(equipment.getType())) {
                    holder.btnUpgrade.setVisibility(View.VISIBLE);
                } else {
                    holder.btnUpgrade.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                holder.tvName.setText("Unknown equipment");
            }
        });

        holder.tvActivated.setVisibility(userEq.isActive() ? View.VISIBLE : View.GONE);
        holder.btnActivate.setVisibility(userEq.isActive() ? View.GONE : View.VISIBLE);

        holder.btnActivate.setOnClickListener(v -> listener.onActivate(userEq, position));
        holder.btnUpgrade.setOnClickListener(v -> {
            if (upgradeListener != null) {
                upgradeListener.onUpgrade(userEq, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvName, tvActivated;
        Button btnActivate, btnUpgrade;

        EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_equipment);
            tvName = itemView.findViewById(R.id.tvEquipmentName);
            tvActivated = itemView.findViewById(R.id.tvActivated);
            btnActivate = itemView.findViewById(R.id.btnActivate);
            btnUpgrade = itemView.findViewById(R.id.btnUpgrade);
        }
    }
}

//package com.example.habitforge.presentation.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.habitforge.R;
//import com.example.habitforge.application.model.UserEquipment;
//
//import java.util.List;
//
//public class UserEquipmentAdapter extends RecyclerView.Adapter<UserEquipmentAdapter.ViewHolder> {
//
//    public interface OnActivateClickListener {
//        void onActivate(UserEquipment item, int position);
//    }
//
//    private final List<UserEquipment> equipmentList;
//    private final OnActivateClickListener listener;
//
//    public UserEquipmentAdapter(List<UserEquipment> equipmentList, OnActivateClickListener listener) {
//        this.equipmentList = equipmentList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_user_equipment, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        UserEquipment item = equipmentList.get(position);
//        holder.tvName.setText(item.getEquipmentId()); // možeš i prikazati ime iz Equipment objekta ako ga imaš
//       // holder.btnActivate.setOnClickListener(v -> listener.onActivate(item));
//        if(item.isActive()){
//            holder.btnActivate.setVisibility(View.GONE);
//            holder.tvActivated.setVisibility(View.VISIBLE);
//        } else {
//            holder.btnActivate.setVisibility(View.VISIBLE);
//            holder.tvActivated.setVisibility(View.GONE);
//            holder.btnActivate.setOnClickListener(v -> listener.onActivate(item, position));
//        }
//    }
//
//
//
//    @Override
//    public int getItemCount() {
//        return equipmentList.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView tvName;
//        Button btnActivate;
//        TextView tvActivated;
//        ViewHolder(View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tvEquipmentName);
//            btnActivate = itemView.findViewById(R.id.btnActivate);
//            tvActivated = itemView.findViewById(R.id.tvActivated);
//        }
//    }
//}
