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
                // Afficher l'image de la cam??ra sur la SurfaceView
                mSurfaceHolder.unlockCanvasAndPost((Canvas) msg.obj);
            }
            return true;
        }
    });

    /**
     M??thode appel??e lors de la cr??ation de l'activit?? de la cam??ra.
     Initialise les ??l??ments de l'interface utilisateur et instancie les objets n??cessaires au fonctionnement de la cam??ra.
     */
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

    /**
     M??thode qui red??marre la pr??visualisation de la cam??ra lorsque l'application est mise en arri??re-plan puis r??activ??e.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Enregistrer un listener qui sera appel?? chaque fois que la valeur du capteur de boussole change
        mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

        startCameraPreview();
    }

    /**
     M??thode appel??e lorsque l'application passe en pause.
     Arr??te la pr??visualisation de la cam??ra et d??sactive le listener de capteur de boussole.
     */
    @Override
    protected void onPause() {
        stopCameraPreview();

        mSensorManager.unregisterListener(this);

        super.onPause();
    }

    /**
     * Lancement de l'aper??u de la cam??ra
     */
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

    /**
     * Arr??te la pr??visualisation de la cam??ra
     */
    private void stopCameraPreview() {
        if (mIsCameraPreviewing) {
            mIsCameraPreviewing = false;
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Cette m??thode est appel??e lorsque la surface de pr??visualisation de la cam??ra est cr????e. Elle d??marre la pr??visualisation de la cam??ra en ouvrant la cam??ra,
     * en d??finissant son orientation et en configurant son affichage et son rappel de pr??visualisation.
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startCameraPreview();
    }

    /**
     * "M??thode ex??cut??e lorsque les dimensions de la SurfaceView associ??e ?? la pr??visualisation de la cam??ra ont chang??"
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        stopCameraPreview();
        startCameraPreview();
    }

    /**
     M??thode appel??e lorsque la surface de pr??visualisation de la cam??ra est d??truite
     Arr??te la pr??visualisation de la cam??ra
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopCameraPreview();
    }

    /**
     *Fonction pour mettre ?? jour l'image de la cam??ra avec l'image de surimpression en fonction de l'orientation de l'appareil :
     * Cette fonction est appel??e chaque fois que des donn??es de pr??visualisation sont disponibles de la cam??ra. Elle v??rifie d'abord si
     * l'angle de rotation de l'appareil est compris entre 0 et 45 ou entre 315 et 360 (c'est-??-dire si l'appareil est orient?? vers le nord).
     * Si c'est le cas, elle convertit les donn??es de la cam??ra en une image Bitmap et la combine avec l'image de surimpression.
     * Sinon, elle ne combine pas les images et ne met ?? jour que l'image de la cam??ra. Enfin, elle utilise un objet Canvas pour dessiner l'image combin??e sur l'??cran.
     */
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        // V??rifier si l'angle de rotation est compris entre 0 et 45 ou entre 315 et 360
        if (mDeviceOrientation >= 0 && mDeviceOrientation <= 45 || mDeviceOrientation >= 315 && mDeviceOrientation <= 360) {
            Log.d("Orientation Nord", "OUI");
            // Afficher l'image de surimpression
            final int width = camera.getParameters().getPreviewSize().width;
            final int height = camera.getParameters().getPreviewSize().height;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Convertir les donn??es de la cam??ra en image Bitmap
                    YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(100, 100, width, height), 50, out);
                    final Bitmap cameraBitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

                    // Superposer l'image de la cam??ra avec l'image de surimpression
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
                    // Convertir les donn??es de la cam??ra en image Bitmap
                    YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(100, 100, width, height), 50, out);
                    final Bitmap cameraBitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());


                    // D??verrouillez l'objet Canvas.

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

    /**
     * Fonction pour superposer deux images Bitmap
     *
     * Cette fonction prend en entr??e deux images Bitmap, une image de la cam??ra et une image de surimpression,
     * et retourne une nouvelle image Bitmap qui est la combinaison des deux images en superposition.
     * La fonction cr??e un nouveau bitmap de la taille de l'image de la cam??ra, dessine l'image de la cam??ra sur ce bitmap,
     * puis dessine l'image de surimpression sur ce bitmap en utilisant une translation pour d??placer l'image de surimpression vers la position souhait??e.
     * La fonction retourne finalement le bitmap combin??.
     * @param cameraBitmap
     * @param overlayBitmap
     * @return
     */
    private Bitmap combineImages(Bitmap cameraBitmap, Bitmap overlayBitmap) {
        // Cr??er un nouveau bitmap de la taille de l'image de la cam??ra
       Bitmap combinedBitmap = Bitmap.createBitmap(cameraBitmap.getWidth(), cameraBitmap.getHeight(), cameraBitmap.getConfig());

        // Dessiner l'image de la cam??ra sur le nouveau bitmap
       Canvas canvas = new Canvas(combinedBitmap);
       canvas.drawBitmap(cameraBitmap, 0, 0, null);

        // Dessiner l'image de surimpression sur le nouveau bitmap
       canvas.translate(100, 100);
       canvas.drawBitmap(overlayBitmap, 0, 0, null);


        return cameraBitmap;
    }

    /**
     * "Mise ?? jour de l'angle de rotation de l'image de surimpression en fonction de l'orientation de l'appareil"
     * @param event
     */
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

    /**
     * Fonction pour capturer un sprite dans l'application, renvoie ensuite ?? la map avec un extra dictant le comportement
     * ?? suivre.
     * @param view
     */
    public void CatchSprite(View view) {

        // Retourner sur la vue activity_maps
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("capture", 1);
        startActivity(intent);
    }




}




