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
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements OrientationManager.OrientationListener {
    CameraPreview mCameraPreview;
    Camera mCamera;
    private Button switch_camera, capture, open_galery, flash, videoCapture;
    protected int curentCameraMode = FRONT_CAMERA;
    final static int FRONT_CAMERA = 0, BACK_CAMERA = 1;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private RelativeLayout camera_relatice;
    private boolean isFlashOn = false;
    static CameraActivity cameraActivity;

    int rotationMode = 0;
    final int LANDSHAPE = 1, POIRTRAIT = 0, POIRTRAIT_REVERSE = 2, LANDSHAPE_REVERSE = 3;
    private OrientationManager orientationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraActivity = this;
        setContentView(R.layout.activity_camera);
        sendMessage("/camera", "start".getBytes());
        camera_relatice = (RelativeLayout) findViewById(R.id.camera_relatice);
        switch_camera = (Button) findViewById(R.id.switch_camera);
        open_galery = (Button) findViewById(R.id.open_galery);
        flash = (Button) findViewById(R.id.flash);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        capture = (Button) findViewById(R.id.capture);
        videoCapture = (Button) findViewById(R.id.cam_to_video);
        turnOnScreen();
        videoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         new AsyncTask<Void,Void,Void>(){
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();
                 runOnUiThread(new Thread(){
                     @Override
                     public void run() {
                         videoCapture.setVisibility(View.INVISIBLE);
                     }
                 });
             }

             @Override
             protected Void doInBackground(Void... voids) {

                 if (!isVideoMode) {

                     startCaptureVideo();
                 } else {

                     stopCaptureVideo();
                 }
                 return null;
             }
         }.execute();



//
            }
        });
        OrientationManager orientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this);
        orientationManager.enable();
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlash();
            }
        });
        open_galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesGalery();
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                capture();
            }
        });
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });


        getCameraInstance(FRONT_CAMERA);
//        mCameraPreview.refreshCamera(mCamera);

        //        mCameraPreview = new CameraPreview(this, mCamera);

    }

    void capture() {
        mCamera.setPreviewCallbackWithBuffer(null);

        mCamera.takePicture(null, null, picture);

    }


    void startCaptureVideo() {

        runOnUiThread(new Thread(){
            @Override
            public void run() {
                isVideoMode = true;
                getCameraInstance(curentCameraMode);
                videoCapture.setBackgroundResource(R.drawable.stop);
                switch_camera.setVisibility(View.INVISIBLE);
               capture.setVisibility(View.INVISIBLE);
                open_galery.setVisibility(View.INVISIBLE);
                flash.setVisibility(View.INVISIBLE);
                videoCapture.setVisibility(View.VISIBLE);
            }
        });

        sendMessage("/info","startCapturing".getBytes());
    }

    void stopCaptureVideo() {
        runOnUiThread(new Thread(){
            @Override
            public void run() {


                videoCapture.setBackgroundResource(R.drawable.video);
                isVideoMode = false;
                switch_camera.setVisibility(View.VISIBLE);
                capture.setVisibility(View.VISIBLE);
                open_galery.setVisibility(View.VISIBLE);
                flash.setVisibility(View.VISIBLE);
                mCameraPreview.stopVideo();
                if (mCameraPreview.mMediaRecorder!=null){
                    mCameraPreview.mMediaRecorder.stop();
                    mCameraPreview.mMediaRecorder.release();
                    mCameraPreview=null;
                }
                videoCapture.setVisibility(View.VISIBLE);
            }
        });
        sendMessage("/info","stopCapturing".getBytes());


    }


    public boolean isVideoMode = false;
    //    boolean isVideoRecording = false;
    MediaRecorder mediaRecorder;

//    private boolean prepareMediaRecorder() {
//
////        if (curentCameraMode == FRONT_CAMERA) {
//            getCameraInstance(BACK_CAMERA);
////        }
//
//
//        mediaRecorder = new MediaRecorder();
//
//        mCamera.unlock();
//        mediaRecorder.setCamera(mCamera);
//
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//
//
//
////        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
////        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
////        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
////        mediaRecorder.setVideoFrameRate(20);
//
//
//
//
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
//
//
//        mediaRecorder.setVideoFrameRate(20); // set to 20
//
//
//        mediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
//
//
//
//
//
////        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
//        mediaRecorder.setOutputFile(initFile().getPath());
//        mediaRecorder.setMaxDuration(2400000); //set maximum duration 60 sec.
//        mediaRecorder.setMaxFileSize(300000000); //set maximum file size 50M
//
//
//mCameraPreview.refreshCamera(mCamera);
//
//        //super.setVideoSize(quality.resX,quality.resY);
//        try {
//            mediaRecorder.prepare();
//
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//            releaseMediaRecorder();
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            releaseMediaRecorder();
//            return false;
//        }
//        return true;
//
//    }

//    private File initFile() {
//        // File dir = new
//        // File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
//        // this
//        File file;
//        File dir = new File(Environment.getExternalStorageDirectory(), this
//                .getClass().getPackage().getName());
//
//
//        if (!dir.exists() && !dir.mkdirs()) {
//
//            Toast.makeText(CameraActivity.this, "not record", Toast.LENGTH_SHORT);
//            file = null;
//        } else {
//            file = new File(dir.getAbsolutePath(), new SimpleDateFormat(
//                    "'IMG_'yyyyMMddHHmmss'.mp4'").format(new Date()));
//        }
//        return file;
//    }


//    private void releaseMediaRecorder() {
//        if (mediaRecorder != null) {
//            mediaRecorder.reset(); // clear recorder configuration
//            mediaRecorder.release(); // release the recorder object
//            mediaRecorder = null;
//            mCamera.lock(); // lock camera for later use
//        }
//    }

    Camera.PictureCallback picture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            addForStoring(data);
        }
    };

    void switchFlash() {
        mCamera.setPreviewCallbackWithBuffer(null);

        isFlashOn = !isFlashOn;
        if (isFlashOn) {
            flash.setBackground(getResources().getDrawable(R.drawable.flash_yes));
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(p);
        } else {
            flash.setBackground(getResources().getDrawable(R.drawable.flash_no));
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
        }
        mCameraPreview.refreshCamera(mCamera);
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

    //private int picRotation=-90;
    private void addForStoring(byte[] bytes) {
//        Toast.makeText(CameraActivity.this, "addForStoring start", Toast.LENGTH_SHORT).show();

        BitmapDrawable bitmap = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes,
                0, bytes.length));


        Matrix matrix = new Matrix();

//        if(rotationMode==POIRTRAIT)
//            Toast.makeText(CameraActivity.this, "POIRTRAIT", Toast.LENGTH_SHORT).show();
//        if(rotationMode==LANDSHAPE)
//            Toast.makeText(CameraActivity.this, "LANDSHAPE", Toast.LENGTH_SHORT).show();


        if (rotationMode == LANDSHAPE) {
            matrix.postRotate(0);
        }
        if (rotationMode == LANDSHAPE_REVERSE) {
            matrix.postRotate(-180);
        } else if (rotationMode == POIRTRAIT) {
            if (BACK_CAMERA == curentCameraMode) {
                matrix.postRotate(90);
            } else
                matrix.postRotate(-90);
        }
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


    private Bitmap getRotatedImage(Bitmap bitmap, String photoPath) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap bmp = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bmp = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                bmp = rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                bmp = rotateImage(bitmap, 270);
                break;
            default:
                bmp = rotateImage(bitmap, 0);

        }

        return bmp;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }


    private void goToMenu() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);
    }

    void switchCamera() {
        if (mCamera == null) {
            goToMenu();
            return;
        }

        mCamera.setPreviewCallbackWithBuffer(null);

        if (curentCameraMode == FRONT_CAMERA) {
            getCameraInstance(BACK_CAMERA);
        } else if (curentCameraMode == BACK_CAMERA) {
            getCameraInstance(FRONT_CAMERA);
        }
    }

    Camera getCameraInstance(int cameraMode) {

        Intent startMain;
        Exception e;
        Error error;
        Camera camera = null;
        if (this.mCameraPreview != null) {
            this.mCameraPreview.stopCamera();
            this.camera_relatice.removeView(this.mCameraPreview);
            this.mCameraPreview = null;
        }
        if (BACK_CAMERA == cameraMode) {
            try {
                camera = Camera.open();
            } catch (Exception e2) {
                camera = null;
            }
            if (camera == null) {
                camera = openFrontFacingCamera();
            }
            if (camera == null && camera == null) {
                try {
                    camera = Camera.open(0);
                    if (camera == null) {
                        throw new Exception();
                    }
                } catch (Throwable e3) {
                    e3.printStackTrace();
                    try {

                        if (cameraMode == 0) {
                            try {
                                camera = openFrontFacingCamera();
                            } catch (Exception e4) {
                                e4.printStackTrace();
                            }
                            if (camera == null) {
                                try {
                                    camera = Camera.open();
                                } catch (Exception e5) {
                                    camera = null;
                                }
                            }
                            if (camera == null) {
                                try {
                                    camera = Camera.open(0);

                                } catch (Exception|Error e6) {
                     e6.printStackTrace();
                                    this.mCamera = camera;
                                    this.mCamera.setDisplayOrientation(90);
                                    this.mCameraPreview = new CameraPreview(this, this.mCamera);
                                    this.curentCameraMode = cameraMode;
                                    this.camera_relatice.addView(this.mCameraPreview, 0);
                                    return camera;
                                }
                            }
                        }
                        this.mCamera = camera;
                        this.mCamera.setDisplayOrientation(90);
                        this.mCameraPreview = new CameraPreview(this, this.mCamera);
                        this.curentCameraMode = cameraMode;
                        this.camera_relatice.addView(this.mCameraPreview, 0);
                    } catch (Exception e42) {
                        e42.printStackTrace();
                    }
                    return camera;
                }
            }
        }
        if (cameraMode == FRONT_CAMERA) {
            camera = openFrontFacingCamera();
            if (camera == null) {
                camera = Camera.open();
            }
            if (camera == null) {
                camera = Camera.open(0);

            }
        }
        this.mCamera = camera;
        this.mCamera.setDisplayOrientation(90);
        this.mCameraPreview = new CameraPreview(this, this.mCamera);
        this.curentCameraMode = cameraMode;
        this.camera_relatice.addView(this.mCameraPreview, 0);
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

    static boolean willSendForClosing=true;
    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mCameraPreview != null&&(!isOpenGalery)&&willSendForClosing) {
            mCameraPreview.sendGoBack();
            mCameraPreview.finish();
        }
//        if (mCameraPreview != null) {
//            mCameraPreview.onStop();
//
//        }


        try {
            if (mCamera != null) {
                hideFlash();
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

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
        cameraActivity = this;
        isOpenGalery=false;
        willSendForClosing=true;
//        if (mCameraPreview != null && mCamera != null) {
//if(mCamera==null)
            getCameraInstance(curentCameraMode);

//        }
//            mCameraPreview.refreshCamera(mCamera);

    }

    @Override
    public void onBackPressed() {
        if(mCamera!=null)
        mCamera.setPreviewCallbackWithBuffer(null);
        finish();
        mCameraPreview.finish();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);

    }

    boolean isOpenGalery=false;
    private void openImagesGalery() {
        if(mCamera!=null)
        mCamera.setPreviewCallbackWithBuffer(null);
        isOpenGalery=true;
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
        try {
            sendMessage("/info","exit".getBytes());
            turnOffScreen();
        } catch (Exception e) {
e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            cameraActivity = null;
            if (mCamera != null) {
                mCamera.stopPreview();
//            if(mCamera.)
//            mCamera.unlock();
                mCamera.release();

                mCamera = null;
            }
            if (mCameraPreview != null) {
                mCameraPreview.finish();
                mCameraPreview.destroy();

            }
        }catch (RuntimeException e){

        }
    }

    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }


    private GoogleApiClient client;

    private void sendMessage(final String message, final byte[] payload) {

        if (client == null || !client.isConnected()) {
            onReadyForContent();
        }
        if (client != null)
            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    List<Node> nodes = getConnectedNodesResult.getNodes();
                    for (Node node : nodes) {


                        Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (sendMessageResult.getStatus().isSuccess()) {
                                    if(message.equals("/camera")){
                                        client=null;
                                    }if(new String(payload).equals("exit")){
                                        client.disconnect();
                                        client=null;
                                    }
                                } else {
                                    client = null;
                                    Toast.makeText(CameraActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                }
            });
    }


//    private int getScreenOrientation() {
//        int rotation = getWindowManager().getDefaultDisplay().getRotation();
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        int orientation;
//        // if the device's natural orientation is portrait:
//        if ((rotation == Surface.ROTATION_0
//                || rotation == Surface.ROTATION_180) && height > width ||
//                (rotation == Surface.ROTATION_90
//                        || rotation == Surface.ROTATION_270) && width > height) {
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                    break;
//                case Surface.ROTATION_90:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                    break;
//                case Surface.ROTATION_180:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                    break;
//                case Surface.ROTATION_270:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//                    break;
//                default:
//                    Log.e("TAG", "Unknown screen orientation. Defaulting to " +
//                            "portrait.");
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                    break;
//            }
//        }
//        // if the device's natural orientation is landscape or if the device
//        // is square:
//        else {
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                    break;
//                case Surface.ROTATION_90:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                    break;
//                case Surface.ROTATION_180:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//                    break;
//                case Surface.ROTATION_270:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                    break;
//                default:
//                    Log.e("TAG", "Unknown screen orientation. Defaulting to " +
//                            "landscape.");
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                    break;
//            }
//        }
//
//        return orientation;
//    }


    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch (screenOrientation) {
            case PORTRAIT:
            case REVERSED_PORTRAIT:
                rotationMode = POIRTRAIT;

                break;
            case REVERSED_LANDSCAPE:
                rotationMode = LANDSHAPE_REVERSE;

                break;
            case LANDSCAPE:
                rotationMode = LANDSHAPE;

                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            finish();
            goToMenu();
            return true;
        }
//        if ((keyCode == KeyEvent.KEYCODE_APP_SWITCH)) {
//            finish();
//
//        }
//        Toast.makeText(CameraActivity.this, "keyCode ="+keyCode+"\nKEYCODE_APP_SWITCH"+KeyEvent.KEYCODE_APP_SWITCH, Toast.LENGTH_SHORT).show();

        return super.onKeyDown(keyCode, event);
    }
    public void onUserLeaveHint() { // this only executes when Home is selected.
        // do stuff
        super.onUserLeaveHint();
        if(!isOpenGalery)
            finish();
    }
}