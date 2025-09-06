package com.example.facelearn;

import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.CameraCaptureCallback;
import androidx.camera.core.impl.CameraCaptureResult;
import androidx.camera.core.impl.CameraConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.facelearn.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Executor cameraExecutor;
    private Handler cameraHandler;

    CameraManager cameraManager;

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

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraHandler = new Handler();

        initCamera(this);

        cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        initCamera2(cameraManager);

    }
    ImageReader imageReader;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    void initCamera(Context context ) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
                bindFaceDetect(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void initCamera2(CameraManager cameraManager) {
        Log.d("MainActivity", "initCamera2");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                Log.d("MainActivity", "camera id: " + id);
            }
            String cameraId = cameraIds[0];

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            imageReader = ImageReader.newInstance(
                    sizes[0].getWidth(), sizes[0].getHeight(), ImageFormat.JPEG, 3);

            imageReader.setOnImageAvailableListener(
                   reader -> {
                    },
                    cameraHandler);

            cameraManager.openCamera(cameraId, cameraExecutor, cameraDeviceStateCallback);
        } catch (CameraAccessException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d("MainActivity", "onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d("MainActivity", "onError");
        }

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d("MainActivity", "onOpened");
            try {
//                SessionConfiguration config = new SessionConfiguration(
//                        SESSION_REGULAR,
//                        List.of(),
//                        cameraExecutor,
//                        cameraCaptureSessionStateCallback);
//                cameraDevice.createCaptureSession ( config);
                cameraDevice.createCaptureSession(
                        List.of(imageReader.getSurface()),
                        cameraCaptureSessionStateCallback,
                        cameraHandler);
            } catch (CameraAccessException e) {
                // throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    };

    CameraCaptureSession.StateCallback   cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d("MainActivity", "onConfigureFailed");
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d("MainActivity", "onConfigured");
            camera2(session);
        }
    };
    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private void bindFaceDetect(ProcessCameraProvider cameraProvider) {
        ImageCapture.Builder builder = new ImageCapture.Builder();
        builder.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);
        ImageCapture imageCapture = builder.build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        Camera camera = cameraProvider.bindToLifecycle(
                (LifecycleOwner)this,
                cameraSelector,
                imageCapture);

        CameraControl control = camera.getCameraControl();

        CameraInfo info = camera.getCameraInfo();

        imageCapture.takePicture(cameraExecutor,
                new ImageCapture.OnImageCapturedCallback(){
                    @Override
                    public void onCaptureStarted() {
                        Log.d("MainActivity", "onCaptureStarted");
                    }
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Log.d("MainActivity", "onCaptureSuccess");
                    }
                    @Override
                    public void onError(@NonNull final ImageCaptureException exception) {
                        Log.d("MainActivity", "onError");
                    }
                });
//        Long videoCallStreamId =
//                CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_VIDEO_CALL.toLong();

        List<CameraInfo> cameraInfos = cameraProvider.getAvailableCameraInfos();
        CameraInfo frontCameraInfo = null;
        for (CameraInfo cameraInfo : cameraInfos) {
            long[] availableStreamUseCases = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                availableStreamUseCases = Camera2CameraInfo.from(cameraInfo)
                        .getCameraCharacteristic(
                                CameraCharacteristics.SCALER_AVAILABLE_STREAM_USE_CASES
                        );
            }
//            boolean isVideoCallStreamingSupported = Arrays.asList(availableStreamUseCases)
//                    .contains(videoCallStreamId);
//            boolean isFrontFacing = (cameraInfo.getLensFacing() ==
//                    CameraSelector.LENS_FACING_FRONT);
//
//            if (isVideoCallStreamingSupported && isFrontFacing) {
//                frontCameraInfo = cameraInfo;
//            }
        }
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(
                (LifecycleOwner)this,
                cameraSelector,
                preview);
    }
    void configureSession(CameraDevice device, List<Surface> targets) {
        ArrayList<OutputConfiguration> configs = new ArrayList();
        //String streamUseCase = CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL;
        //long useCase = Long.valueOf(streamUseCase);
        long useCase = CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL;
        for (Surface s : targets) {
            OutputConfiguration config = new OutputConfiguration(s);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                config.setStreamUseCase(useCase);
            }
            configs.add(config);
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    Surface outputTarget;

    void camera2(@NonNull CameraCaptureSession session) {
        Log.d("MainActivity", "camera2");
        try {
            CameraDevice cameraDevice = session.getDevice();
            CaptureRequest.Builder captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // captureRequest.addTarget(previewSurface);
            captureRequest.addTarget(imageReader.getSurface());
            captureRequest.set(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);
            session.capture(captureRequest.build(), null, null);
        } catch (CameraAccessException e) {
            // throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback(){

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d("MainActivity", "onCaptureCompleted");
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d("MainActivity", "onCaptureFailed");
        }
    };
}