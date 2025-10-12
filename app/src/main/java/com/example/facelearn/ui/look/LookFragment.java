package com.example.facelearn.ui.look;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.facelearn.Constant;
import com.example.facelearn.FaceLearnException;
import com.example.facelearn.MainActivity;
import com.example.facelearn.databinding.FragmentLookBinding;
import com.example.facelearn.util.CameraControl;
import com.example.facelearn.util.DataStore;
import com.example.facelearn.util.ImageUtil;
import com.example.facelearn.util.PythonControl;

import java.util.Locale;

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

        final ImageButton search = binding.search;
        search.setOnClickListener((view) -> {

            MainActivity activity = (MainActivity) LookFragment.this.requireActivity();
            CameraControl cameraControl = activity.getCameraControl();
            cameraControl.findFace(
                    (bitmap) -> {
                        try {
                            ImageUtil.saveJpegFile(bitmap, Constant.TEMPORAL_FILE_PATH);

                            PythonControl py = new PythonControl(activity);
                            String vecStr = py.callPython();

                            DataStore dataStore = activity.getDataStore();
                            DataStore.Result result = dataStore.nearest(vecStr);

                            Toast.makeText(
                                    activity,
                                    String.format(Locale.getDefault(), "%s さん です。(%.2f)", result.name, result.point),
                                    Toast.LENGTH_LONG).show();
                        } catch (FaceLearnException ex) {
                            Toast.makeText(activity, "画像解析に失敗しました。", Toast.LENGTH_LONG).show();
                        }
                    },
                    () -> {
                        Toast.makeText(activity, "顔を見つけられませんでした。", Toast.LENGTH_LONG).show();
                    }
            );
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}