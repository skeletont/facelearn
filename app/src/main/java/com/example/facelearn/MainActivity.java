package com.example.facelearn;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.facelearn.databinding.ActivityMainBinding;
import com.example.facelearn.util.CameraControl;
import com.example.facelearn.util.DataStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    private Handler cameraHandler;

    private CameraControl cameraControl;

    private DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_look,
                R.id.navigation_register,
                R.id.navigation_manage)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        dataStore = new DataStore(this);

        //cameraHandler = new Handler();
        cameraHandler = null;
        cameraControl = new CameraControl();
        initCameraControl();

        binding.surfaceView.setOnClickListener(a -> {
            Log.d(TAG, "setOnClickListener");
            cameraControl.switchCamera();
            initCameraControl();
        });
    }

    void initCameraControl() {
        Log.d(TAG, "initCameraControl");
        cameraControl.initialze(
                MainActivity.this,
                cameraHandler,
                binding.surfaceView.getHolder()
//                ,
//                (bitmap) -> {
//                    try {
//                        ImageUtil.saveJpegFile(bitmap, Constant.TEMPORAL_FILE_PATH);
//
//                        PythonControl py = new PythonControl(MainActivity.this);
//                        String vecStr = py.callPython();
//
//                        dataStore.add("p1", vecStr);
//                    } catch (FaceLearnException ex) {
//                        // Snackbar.make(MainActivity.this, , "画像解析に失敗しました。", Snackbar.LENGTH_LONG).show();
//                        Toast.makeText(MainActivity.this, "画像解析に失敗しました。", Toast.LENGTH_LONG).show();
//                    }
//                }
                );
    }

    public CameraControl getCameraControl() {
        return cameraControl;
    }

    public DataStore getDataStore() {
        return dataStore;
    }
}