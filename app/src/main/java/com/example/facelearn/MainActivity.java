package com.example.facelearn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.facelearn.databinding.ActivityMainBinding;
import com.example.facelearn.util.CameraControl;
import com.example.facelearn.util.ImageUtil;
import com.example.facelearn.util.PythonControl;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Handler cameraHandler;

    private CameraControl cameraControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        cameraHandler = new Handler();
        cameraControl = new CameraControl();
        cameraControl.initialze(
                MainActivity.this,
                cameraHandler,
                binding.surfaceView.getHolder(),
                (bitmap) -> {
                    ImageUtil.saveJpegFile(bitmap, "/sdcard/Android/data/com.example.facelearn/files/MyImage.jpg");

                    PythonControl py = new PythonControl(MainActivity.this);
                    String vecStr = py.callPython();

                    SharedPreferences pref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                    pref.edit().putString("p1", vecStr).apply();
                });
    }

}