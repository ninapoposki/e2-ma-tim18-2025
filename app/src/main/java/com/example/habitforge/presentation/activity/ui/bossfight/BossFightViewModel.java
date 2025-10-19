package com.example.habitforge.presentation.activity.ui.bossfight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BossFightViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();

    public BossFightViewModel() {
        mText.setValue("This is Boss Fight fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
