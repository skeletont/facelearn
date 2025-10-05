package com.example.facelearn.ui.look;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.facelearn.databinding.FragmentLookBinding;

public class LookFragment extends Fragment {

    private FragmentLookBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LookViewModel lookViewModel =
                new ViewModelProvider(this).get(LookViewModel.class);

        binding = FragmentLookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLook;
        lookViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}