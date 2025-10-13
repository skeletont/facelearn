package com.example.facelearn;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.widget.Toolbar;

import com.example.facelearn.databinding.ActivityMainBinding;
import com.example.facelearn.util.CameraControl;
import com.example.facelearn.util.DataStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FaceLearnActivity extends AppCompatActivity {
    private static final String TAG = "FaceLearnActivity";
    private ActivityMainBinding binding;

    private Handler cameraHandler;

    private CameraControl cameraControl;

    private DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // 作業ディレクトリを作成
        File dir = new File(Constant.TEMPORAL_FILE_PATH).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Pythonに読み込ませるデータファイルを作成
        AssetManager assetManager = getAssets();
        File ptFile = new File(Constant.KNOWLEDGE_FILE_PATH);
        if (!ptFile.exists()) {
            try (
                    // please download https://github.com/timesler/facenet-pytorch/releases/download/v2.2.9/20180402-114759-vggface2.pt
                    InputStream is = assetManager.open("pt/20180402-114759-vggface2.pt");
                    OutputStream os = new FileOutputStream(ptFile)
            ) {
                byte[] buffer = new byte[2048];
                int bufLen = 0;
                while ((bufLen = is.read(buffer)) > 0) {
                    os.write(buffer, 0, bufLen);
                    os.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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

//        Toolbar toolbar = new Toolbar(this);
//        setSupportActionBar(toolbar);

        dataStore = new DataStore(this);

        //cameraHandler = new Handler();
        cameraHandler = null;
        cameraControl = new CameraControl();
        initCameraControl();

        binding.surfaceView.setOnClickListener(a -> {
            Log.d(TAG, "setOnClickListener");
            cameraControl.sessionClose();
            cameraControl.switchCamera();
            initCameraControl();
        });
    }

    void initCameraControl() {
        Log.d(TAG, "initCameraControl");
        cameraControl.initialze(
                FaceLearnActivity.this,
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