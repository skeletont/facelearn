package com.example.facelearn.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class CameraControl {
    private static final String TAG = "CameraControl";

    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    ;
    private Handler cameraHandler;

    private Context context;
    private CameraManager cameraManager;
    private ImageReader imageReader;

    private SurfaceHolder surfaceHolder;

    private CameraDevice cameraDevice;

    private CameraCaptureSession session;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private CaptureCompleted onCaptureCompleted;

    private CaptureFailed onCaptureFailed;

    private int cameraIndex = 0;

    private HandlerThread handlerThread;

    public interface CaptureCompleted {
        void onCaptureCompleted(Bitmap bitmap);
    }

    public interface CaptureFailed {
        void onCaptureFailed();
    }

    public void initialze(
            Context context,
            SurfaceHolder holder
    ) {
        this.context = context;
        this.handlerThread = new HandlerThread("camera-control");
        this.handlerThread.start();
        this.cameraHandler = new Handler(handlerThread.getLooper());
        this.cameraManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);

        if (this.surfaceHolder == null) {
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    Log.d(TAG, "surfaceChanged");
                    CameraControl.this.surfaceHolder = holder;
                    initCamera2(cameraManager);
                }

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceCreated");
                    CameraControl.this.surfaceHolder = holder;
                    initCamera2(cameraManager);
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceDestroyed");
                    CameraControl.this.surfaceHolder = null;
                }
            });
        } else {
            initCamera2(cameraManager);
        }
    }

    public void initCamera2(CameraManager cameraManager) {
        Log.d(TAG, "initCamera2");
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
            String cameraId = cameraIds[cameraIndex % cameraIds.length];

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            Size[] sizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP))
                    .getOutputSizes(ImageFormat.JPEG);
            imageReader = ImageReader.newInstance(
                    sizes[0].getWidth(), sizes[0].getHeight(), ImageFormat.JPEG, 3);

            imageReader.setOnImageAvailableListener(
                    reader -> {
                        Log.d(TAG, "setOnImageAvailableListener");
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
            Log.d(TAG, "onDisconnected");
            cameraDevice = camera;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError");
            cameraDevice = camera;
        }

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened");
            cameraDevice = camera;
            onOpen(cameraDevice);
        }
    };

    private void onOpen(CameraDevice cameraDevice) {
        try {
            SessionConfiguration config = new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    List.of(
                            new OutputConfiguration(imageReader.getSurface()),
                            new OutputConfiguration(surfaceHolder.getSurface())
                    ),
                    cameraExecutor,
                    cameraCaptureSessionStateCallback
            );
            cameraDevice.createCaptureSession(config);
        } catch (CameraAccessException e) {
            // throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed");
            CameraControl.this.session = session;
            // onOpen(cameraDevice);
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigured");
            CameraControl.this.session = session;
            requestCapturePreview(session);
            // requestCaptureFace(session);
        }
    };

    void requestCapturePreview(@NonNull CameraCaptureSession session) {
        Log.d(TAG, "requestCapturePreview");
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
        Log.d(TAG, "requestCaptureFace");
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
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void findFace(CaptureCompleted completed) {
        findFace(completed, null);
    }

    public void findFace(CaptureCompleted completed, CaptureFailed failed) {
        Log.d(TAG, "findFace");
        if (session != null) {
            onCaptureCompleted = completed;
            onCaptureFailed = failed;
            requestCaptureFace(session);
        }
    }


    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(
                @NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request,
                @NonNull TotalCaptureResult result) {
            Log.d(TAG, "onCaptureCompleted");
            super.onCaptureCompleted(session, request, result);

            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
            if (faces == null || faces.length == 0) {
                Log.d(TAG, "face: none");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "sleep error");
                }
                // requestCaptureFace(session);
                if (onCaptureFailed != null) {
                    onCaptureFailed.onCaptureFailed();
                }
            } else {
                for (Face f : faces) {
                    Log.d(TAG, "face: " + f);
                }
                Face face = faces[0];
                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    Log.e(TAG, "onCaptureCompleted: image is null.");
                    onCaptureFailed.onCaptureFailed();
                    return;
                }
                image.setCropRect(face.getBounds());

                Bitmap bitmap0 = ImageUtil.imageToBitmap(image);
                Bitmap bitmap = Bitmap.createBitmap(
                        bitmap0,
                        face.getBounds().left,
                        face.getBounds().top,
                        face.getBounds().right - face.getBounds().left,
                        face.getBounds().bottom - face.getBounds().top
                );

                image.close();

                // バックカメラで、ポートレート状態で、左に90度回転しているので、画一的に右90度回転する
                // フロントカメラで、ポートレート状態で、右に90度回転しているので、画一的に右270度回転する
                final Matrix m = new Matrix();
                m.setRotate(cameraDevice.getId().equals("0") ? 90 : 270);
                final Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);

                if (onCaptureCompleted != null) {
                    onCaptureCompleted.onCaptureCompleted(bitmap2);
                }
            }
        }

        @Override
        public void onCaptureFailed(
                @NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request,
                @NonNull CaptureFailure failure) {
            Log.d(TAG, "onCaptureFailed");
            super.onCaptureFailed(session, request, failure);

            if (onCaptureFailed != null) {
                onCaptureFailed.onCaptureFailed();
            }
        }
    };

    public void switchCamera() {
        Log.d(TAG, "switchCamera");
        cameraIndex++;
    }

    public void sessionClose() {
        session.close();
    }
}
