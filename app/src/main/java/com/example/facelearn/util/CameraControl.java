package com.example.facelearn.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraControl {

    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    ;
    private Handler cameraHandler;

    private Context context;
    private CameraManager cameraManager;
    private ImageReader imageReader;

    private SurfaceHolder surfaceHolder;

    private CameraCaptureSession session;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private CaptureCompleted onCaptureCompleted;

    public interface CaptureCompleted {
        void onCaptureCompleted(Bitmap bitmap);
    }

    public void initialze(
            Context context,
            Handler cameraHandler,
            SurfaceHolder holder,
            CaptureCompleted onCaptureCompleted) {
        this.context = context;
        this.cameraHandler = cameraHandler;
        this.surfaceHolder = holder;
        this.onCaptureCompleted = onCaptureCompleted;

        this.cameraManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d("MainActivity", "surfaceChanged");
            }

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d("MainActivity", "surfaceCreated");

                initCamera2(cameraManager);
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d("MainActivity", "surfaceDestroyed");
            }
        });
    }

    public void initCamera2(CameraManager cameraManager) {
        Log.d("MainActivity", "initCamera2");
        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            String cameraId = cameraIds[1];

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
            imageReader = ImageReader.newInstance(
                    sizes[0].getWidth(), sizes[0].getHeight(), ImageFormat.JPEG, 3);

            imageReader.setOnImageAvailableListener(
                    reader -> {
                        Log.d("MainActivity", "setOnImageAvailableListener");
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
                cameraDevice.createCaptureSession(
                        List.of(
                                imageReader.getSurface(),
                                surfaceHolder.getSurface()
                        ),
                        cameraCaptureSessionStateCallback,
                        cameraHandler);
            } catch (CameraAccessException e) {
                // throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    };

    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d("MainActivity", "onConfigureFailed");
            CameraControl.this.session = null;
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d("MainActivity", "onConfigured");
            CameraControl.this.session = session;
            requestCapture(session);
            requestCaptureFace(session);
        }
    };

    void requestCapture(@NonNull CameraCaptureSession session) {
        Log.d("MainActivity", "requestCapture");
        try {
            CameraDevice cameraDevice = session.getDevice();
            CaptureRequest.Builder captureRequestPreview
                    = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestPreview.addTarget(this.surfaceHolder.getSurface());
            captureRequestPreview.set(
                    CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_AUTO);
            session.setRepeatingRequest(captureRequestPreview.build(), null, null);

        } catch (CameraAccessException e) {
            // throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    void requestCaptureFace(@NonNull CameraCaptureSession session) {
        Log.d("MainActivity", "requestCapture");
        try {
            CameraDevice cameraDevice = session.getDevice();
            CaptureRequest.Builder captureRequest
                    = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequest.addTarget(imageReader.getSurface());
            captureRequest.set(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);
            session.capture(captureRequest.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            // throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(
                @NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request,
                @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d("MainActivity", "onCaptureCompleted");
            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
            if (faces == null || faces.length == 0) {
                Log.d("MainActivity", "face: none");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d("MainActivity", "sleep error");
                }
                requestCaptureFace(session);
            } else {
                for (Face f : faces) {
                    Log.d("MainActivity", "face: " + f);
                }
                Face face = faces[0];
                Image image = imageReader.acquireLatestImage();
                image.setCropRect(face.getBounds());

                // Bitmap bitmap = convertImageToBitmap(image);
                Bitmap bitmap0 = ImageUtil.imageToBitmap(image);
                Bitmap bitmap = Bitmap.createBitmap(
                        bitmap0,
                        face.getBounds().left,
                        face.getBounds().top,
                        face.getBounds().right - face.getBounds().left,
                        face.getBounds().bottom - face.getBounds().top
                );

                if (onCaptureCompleted != null) {
                    onCaptureCompleted.onCaptureCompleted(bitmap);
                }
            }
        }

        @Override
        public void onCaptureFailed(
                @NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request,
                @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d("MainActivity", "onCaptureFailed");
        }
    };

}
