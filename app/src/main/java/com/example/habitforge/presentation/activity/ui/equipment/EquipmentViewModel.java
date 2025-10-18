package com.example.habitforge.presentation.activity.ui.equipment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EquipmentViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EquipmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is equipment fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}