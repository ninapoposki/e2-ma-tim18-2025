package com.example.habitforge.presentation.activity.ui.mission;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AllianceMissionViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();

    public AllianceMissionViewModel() {
        mText.setValue("This is Alliance Mission fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}

