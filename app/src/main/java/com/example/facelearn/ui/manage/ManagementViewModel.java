package com.example.facelearn.ui.manage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ManagementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is manage fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}