package fr.miage.projet_m2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, SensorEventListener {
    private static final String TAG = "MainActivity";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mOverlayImage;
    private Bitmap mOverlayBitmap;
    private boolean mIsCameraPreviewing = false;
    private final int UPDATE_CAMERA_IMAGE = 1;

    private float mDeviceOrientation;
    private float mOverlayRotation;
    private SensorManager mSensorManager;
    private Sensor mMagneticFieldSensor;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == UPDATE_CAMERA_IMAGE) {
                // Afficher l'image de la caméra sur la SurfaceView
                mSurfaceHolder.unlockCanvasAndPost((Canvas) msg.obj);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        mOverlayImage = (ImageView) findViewById(R.id.overlay_image);
        mOverlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pngegg);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enregistrer un listener qui sera appelé chaque fois que la valeur du capteur de boussole change
        mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

        startCameraPreview();
    }

    @Override
    protected void onPause() {
        stopCameraPreview();

        mSensorManager.unregisterListener(this);

        super.onPause();
    }

    private void startCameraPreview() {
        if (!mIsCameraPreviewing) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mIsCameraPreviewing = true;

            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopCameraPreview() {
        if (mIsCameraPreviewing) {
            mIsCameraPreviewing = false;
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        stopCameraPreview();
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopCameraPreview();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        // Vérifier si l'angle de rotation est compris entre 0 et 45 ou entre 315 et 360
        if (mDeviceOrientation >= 0 && mDeviceOrientation <= 45 || mDeviceOrientation >= 315 && mDeviceOrientation <= 360) {
            Log.d("Orientation Nord", "OUI");
            // Afficher l'image de surimpression
            final int width = camera.getParameters().getPreviewSize().width;
            final int height = camera.getParameters().getPreviewSize().height;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Convertir les données de la caméra en image Bitmap
                    YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(100, 100, width, height), 50, out);
                    final Bitmap cameraBitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

                    // Superposer l'image de la caméra avec l'image de surimpression
                    final Bitmap combinedBitmap = combineImages(cameraBitmap, mOverlayBitmap);

                    /*mSurfaceHolder = mSurfaceView.getHolder();
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawBitmap(combinedBitmap, 100, 100, null);
                        mHandler.obtainMessage(UPDATE_CAMERA_IMAGE, canvas).sendToTarget();
                    }*/
                }
            }).start();
        } else {
            Log.d("Orientation Nord", "NON");
            // Ne pas afficher l'image de surimpression
            final int width = camera.getParameters().getPreviewSize().width;
            final int height = camera.getParameters().getPreviewSize().height;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Convertir les données de la caméra en image Bitmap
                    YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(100, 100, width, height), 50, out);
                    final Bitmap cameraBitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());


                    // Déverrouillez l'objet Canvas.

                    /*mSurfaceHolder = mSurfaceView.getHolder();
                    Canvas canvas = mSurfaceHolder.lockCanvas();

                    if (canvas != null) {
                        canvas.drawBitmap(cameraBitmap, 100, 100, null);
                        mHandler.obtainMessage(UPDATE_CAMERA_IMAGE, canvas).sendToTarget();
                    }*/
                }
            }).start();
        }
    }


    private Bitmap combineImages(Bitmap cameraBitmap, Bitmap overlayBitmap) {
        // Créer un nouveau bitmap de la taille de l'image de la caméra
       Bitmap combinedBitmap = Bitmap.createBitmap(cameraBitmap.getWidth(), cameraBitmap.getHeight(), cameraBitmap.getConfig());

        // Dessiner l'image de la caméra sur le nouveau bitmap
       Canvas canvas = new Canvas(combinedBitmap);
       canvas.drawBitmap(cameraBitmap, 0, 0, null);

        // Dessiner l'image de surimpression sur le nouveau bitmap
       canvas.translate(100, 100);
       canvas.drawBitmap(overlayBitmap, 0, 0, null);


        return cameraBitmap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mDeviceOrientation = event.values[0];
        }
        // Calculer l'angle de rotation de l'image de surimpression en fonction de l'orientation de l'appareil
        mOverlayRotation = -mDeviceOrientation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void CatchSprite(View view) {

        // Retourner sur la vue activity_maps
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("capture", 1);
        startActivity(intent);
    }




}




