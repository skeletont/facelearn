package com.example.facelearn.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.facelearn.databinding.FragmentManageBinding;

public class ManagementFragment extends Fragment {

    private FragmentManageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ManagementViewModel managementViewModel =
                new ViewModelProvider(this).get(ManagementViewModel.class);

        binding = FragmentManageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textManage;
        managementViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}