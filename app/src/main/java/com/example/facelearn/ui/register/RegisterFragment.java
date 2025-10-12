package com.example.facelearn.ui.register;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.facelearn.Constant;
import com.example.facelearn.FaceLearnException;
import com.example.facelearn.MainActivity;
import com.example.facelearn.R;
import com.example.facelearn.databinding.FragmentRegisterBinding;
import com.example.facelearn.util.CameraControl;
import com.example.facelearn.util.DataStore;
import com.example.facelearn.util.ImageUtil;
import com.example.facelearn.util.PythonControl;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";

    private FragmentRegisterBinding binding;

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        RegisterViewModel registerViewModel =
                new ViewModelProvider(this).get(RegisterViewModel.class);

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textRegister;
        registerViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Log.d(TAG, "dd: " + Environment.getDataDirectory());
        Log.d(TAG, "sd: " + Environment.getStorageDirectory());
        Log.d(TAG, "esd: " + Environment.getExternalStorageDirectory());
        Log.d(TAG, "espd: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));


        final ImageButton button = binding.buttonAdd;
        button.setOnClickListener((view) -> {
            MainActivity activity = (MainActivity) RegisterFragment.this.requireActivity();
            CameraControl cameraControl = activity.getCameraControl();
            cameraControl.findFace(
                    (bitmap) -> {
                        View innerView = inflater.inflate(R.layout.dialog_add_face, null);
                        EditText addName = innerView.findViewById(R.id.addName);
                        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                .setView(innerView)
                                .setPositiveButton("登録", (dialog, buttonId) -> {
                                    try {
                                        DataStore dataStore = activity.getDataStore();


                                        ImageUtil.saveJpegFile(bitmap, Constant.TEMPORAL_FILE_PATH);

                                        PythonControl py = new PythonControl(activity);
                                        String vecStr = py.callPython();

                                        dataStore.add(addName.getText().toString(), vecStr);
                                    } catch (FaceLearnException ex) {
                                        Toast.makeText(activity, "画像解析に失敗しました。", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    },
                    () -> {
                        Toast.makeText(activity, "顔を見つけられませんでした。", Toast.LENGTH_LONG).show();
                    });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}