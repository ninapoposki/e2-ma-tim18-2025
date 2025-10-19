package com.example.habitforge.application.service;

import android.content.Context;

import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.data.repository.EquipmentRepository;

public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(Context context) {
        this.equipmentRepository = new EquipmentRepository(context);
    }

    public interface EquipmentCallback {
        void onSuccess(Equipment equipment);
        void onFailure(Exception e);
    }

    public void getEquipmentById(String equipmentId, EquipmentCallback callback) {
        equipmentRepository.getEquipmentById(equipmentId, new EquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                callback.onSuccess(equipment);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
