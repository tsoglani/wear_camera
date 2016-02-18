package camera.nikos.tsoglani.camera;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    CameraPreview mCameraPreview;
    Camera mCamera;
    private Button switch_camera, capture, open_galery, flash;
    private int curentCameraMode = FRONT_CAMERA;
    final static int FRONT_CAMERA = 0, BACK_CAMERA = 1;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private RelativeLayout camera_relatice;
    private boolean isFlashOn = false;
    static CameraActivity cameraActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraActivity = this;
        setContentView(R.layout.activity_camera);
        camera_relatice = (RelativeLayout) findViewById(R.id.camera_relatice);
        switch_camera = (Button) findViewById(R.id.switch_camera);
        open_galery = (Button) findViewById(R.id.open_galery);
        flash = (Button) findViewById(R.id.flash);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        capture = (Button) findViewById(R.id.capture);


        turnOnScreen();

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setPreviewCallbackWithBuffer(null);
                switchFlash();
            }
        });
        open_galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setPreviewCallbackWithBuffer(null);
                openImagesGalery();
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setPreviewCallbackWithBuffer(null);
                capture();
            }
        });
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setPreviewCallbackWithBuffer(null);
                switchCamera();
            }
        });


        getCameraInstance(FRONT_CAMERA);
//        mCameraPreview.refreshCamera(mCamera);

        //        mCameraPreview = new CameraPreview(this, mCamera);

    }

    void capture() {

        mCamera.takePicture(null, null, picture);

    }

//    private Bitmap getScreenShot() {
//        mCameraPreview.setDrawingCacheEnabled(true);
//        mCameraPreview.buildDrawingCache(true);
//        Bitmap bitmap = mCameraPreview.getDrawingCache();
//        return bitmap;
//    }
//
//    void capture2() {
//        if (dataClient == null) {
//            createDataGoogleApiConnection();
//        }
//        new Thread() {
//            @Override
//            public void run() {
//                mCamera.takePicture(null, null, picture2);
//
//
//            }
//        }.start();
//
//    }
//
//    public Bitmap getBitmap() {
//        mCameraPreview.setDrawingCacheEnabled(true);
//        mCameraPreview.buildDrawingCache(true);
//        Bitmap bitmap = Bitmap.createBitmap(mCameraPreview.getWidth(), mCameraPreview.getHeight(), Bitmap.Config.ARGB_8888);
////        Canvas canvas = new Canvas(bitmap);
////        canvas.drawBitmap(bitmap, mCameraPreview.getWidth(), mCameraPreview.getHeight(), null);
////        mCameraPreview.draw(canvas);
//        return bitmap;
//    }
//
//    public void sendCameraBitmap() {
//        if (dataClient == null) {
//            createDataGoogleApiConnection();
//        }
//
//
//        if (mCamera != null) {
//
//
////            Asset asset = createAssetFromBitmap(mCameraPreview.getBackground());
////            PutDataMapRequest request = PutDataMapRequest.create("/image");
////            DataMap map = request.getDataMap();
////            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
////            map.putAsset("profileImage", asset);
////            if (dataClient != null)
////                Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
////           capture2();
//        } else {
//            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//            Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
//            Asset asset = createAssetFromBitmap(bmp);
//            PutDataMapRequest request = PutDataMapRequest.create("/image");
//            DataMap map = request.getDataMap();
//            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//            map.putAsset("profileImage", asset);
//            if (dataClient != null)
//                Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//        }
//    }

//    Camera.PictureCallback picture2 = new Camera.PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            mCameraPreview.refreshCamera(mCamera);
//
//            BitmapDrawable bitmap = new BitmapDrawable(BitmapFactory.decodeByteArray(data,
//                    0, data.length));
//
//
//            Matrix matrix = new Matrix();
//
//            matrix.postRotate(-90);
//
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getBitmap(), 0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight(), matrix, true);
//
//            Asset asset = createAssetFromBitmap(rotatedBitmap);
//            PutDataMapRequest request = PutDataMapRequest.create("/image");
//            DataMap map = request.getDataMap();
//            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//            map.putAsset("profileImage", asset);
//            if (dataClient != null)
//                Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//        }
//    };


    Camera.PictureCallback picture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            addForStoring(data);
        }
    };

    void switchFlash() {
        isFlashOn = !isFlashOn;
        if (isFlashOn) {
            flash.setBackground(getResources().getDrawable(R.drawable.flash_yes));
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
        } else {
            flash.setBackground(getResources().getDrawable(R.drawable.flash_no));
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
        }
    }
//
//
//     GoogleApiClient dataClient;
//
//    public void createDataGoogleApiConnection() {
//        dataClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(Bundle bundle) {
//
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult result) {
//
//                        Toast.makeText(CameraActivity.this, "Connection Faild", Toast.LENGTH_SHORT).show();
//                        dataClient = null;
//                    }
//                })
//                .addApi(Wearable.API)
//                .build();
//
//        dataClient.connect();
//
//    }
//
//
//     static Asset createAssetFromBitmap(Bitmap bitmap) {
//        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
//        return Asset.createFromBytes(byteStream.toByteArray());
//    }


//    private Bitmap getBitmap(byte[] bytes) {
////        Toast.makeText(CameraActivity.this, "addForStoring start", Toast.LENGTH_SHORT).show();
//
//        BitmapDrawable bitmap = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes,
//                0, bytes.length));
//
//
//        Matrix matrix = new Matrix();
//
//        matrix.postRotate(-90);
//
//        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getBitmap(), 0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight(), matrix, true);
//
//        return rotatedBitmap;
////        Toast.makeText(CameraActivity.this, "addForStoring ends", Toast.LENGTH_SHORT).show();
//
////
////
////
//    }


    private void addForStoring(byte[] bytes) {
//        Toast.makeText(CameraActivity.this, "addForStoring start", Toast.LENGTH_SHORT).show();

        BitmapDrawable bitmap = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes,
                0, bytes.length));


        Matrix matrix = new Matrix();

        matrix.postRotate(-90);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getBitmap(), 0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight(), matrix, true);

        Matrix matrix2 = new Matrix();

//        matrix2.postRotate(-90);

//        Bitmap rotatedBitmap2 = Bitmap.createBitmap(bitmap.getBitmap(), 0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight(), matrix2, true);

        BitmapDrawable bitmap2 = new BitmapDrawable(rotatedBitmap);

        open_galery.setBackground(bitmap2);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        saveFunction(byteArray);
//        Toast.makeText(CameraActivity.this, "addForStoring ends", Toast.LENGTH_SHORT).show();

//
//
//
    }


    void switchCamera() {
        if (curentCameraMode == FRONT_CAMERA) {
            getCameraInstance(BACK_CAMERA);
        } else if (curentCameraMode == BACK_CAMERA) {
            getCameraInstance(FRONT_CAMERA);
        }
    }

    Camera getCameraInstance(int cameraMode) {

        Camera camera = null;
        try {

            if (mCameraPreview != null) {
                mCameraPreview.stopVideo();
                camera_relatice.removeView(mCameraPreview);
                mCameraPreview = null;
            }
            if (BACK_CAMERA == cameraMode)
                camera = Camera.open();
            if (FRONT_CAMERA == cameraMode)
                camera = openFrontFacingCamera();

            mCamera = camera;
            mCamera.setDisplayOrientation(90);
            mCameraPreview = new CameraPreview(this, mCamera);


            curentCameraMode = cameraMode;
            camera_relatice.addView(mCameraPreview, 0);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
//        mTextureView.setLayoutParams(new ViewGroup.LayoutParams(500,500));

        return camera;
    }


    private Camera openFrontFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                }
            }
        }

        return cam;
    }

    private void ShowFlash() {
        new Thread() {
            @Override
            public void run() {
                try {


                    try {

                        Camera.Parameters p = mCamera.getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                        mCamera.setParameters(p);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }


    private void hideFlash() {
        new Thread() {
            @Override
            public void run() {
                try {


                    try {

                        Camera.Parameters p = mCamera.getParameters();

                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                        mCamera.setParameters(p);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    @Override
    protected void onDestroy() {
        if (mCameraPreview != null)
            mCameraPreview.onStop();

        super.onDestroy();

        try {
            if (mCamera != null) {
                hideFlash();
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            turnOffScreen();
            if (mCameraPreview != null) {
                new Thread() {
                    @Override
                    public void run() {
                        mCameraPreview.destroy();
                    }
                }
                        .start();
            }
        } catch (Exception e) {

        }
    }


    @TargetApi(21)
    public void turnOnScreen() {
        // turn on screen
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();
    }

    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    public void turnOffScreen() {
        // turn off screen
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
        mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraPreview != null && mCamera != null) {

            getCameraInstance(curentCameraMode);

        }
//            mCameraPreview.refreshCamera(mCamera);

    }

    @Override
    public void onBackPressed() {
        mCamera.setPreviewCallbackWithBuffer(null);
        finish();
        mCameraPreview.finish();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);

    }

    private void openImagesGalery() {
        int OPEN_GALLERY = 1;
        Intent intent;// = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.CATEGORY_APP_GALLERY);
        intent = Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY);

        startActivity(intent);
    }

    public void saveFunction(byte[] data) {
        //make a new picture file
        File pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            return;
        }
        try {


            //write the file
            FileOutputStream fos = new FileOutputStream(pictureFile);

            fos.write(data);

            fos.close();

            Toast.makeText(getApplicationContext(), "   Picture saved at: " + pictureFile.getParentFile().getPath() + " folder , with title: " + pictureFile.getName(), Toast.LENGTH_LONG).show();

            addImageToGallery(pictureFile.getPath(), this);
            mCameraPreview.refreshCamera(mCamera);
//            if (mode.equalsIgnoreCase("voice")) {
//                if (!MainActivity.isSoundAfterCapture) {
//                    startSound(RecordActivity.voice);
//
//                }
//                soundEnable = true;
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //refresh camera to continue preview


    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCameraPreview.finish();
    }


}

