package camera.nikos.tsoglani.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.hardware.Camera.Size;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tsoglani on 29/9/2015.
 */
public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    // Constructor that obtains context and camera
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    public void setZoom(int zoom) {
        Camera.Parameters params = mCamera.getParameters();

        if (params.isZoomSupported()) {
            mCamera.cancelAutoFocus();

            int maxZoom = params.getMaxZoom();

            if (zoom > maxZoom)
                zoom = maxZoom;
            if (zoom < 0)
                zoom = 0;
            params.setZoom(zoom);
            mCamera.setParameters(params);
        }
    }

    float mDist;

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current pos50dpition
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);


        return (float) Math.sqrt(x * x + y * y);
    }

    MediaRecorder mMediaRecorder;

    private boolean initRecorder(Surface surface) throws IOException {

        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mMediaRecorder.setOutputFile(initFile().getPath());
        mMediaRecorder.setMaxDuration(600000); //set maximum duration 60 sec.
        mMediaRecorder.setMaxFileSize(50000000); //set maximum file size 50M

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private File initFile() {
        // File dir = new
        // File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        // this
        File file;
        File dir = new File(Environment.getExternalStorageDirectory(), this
                .getClass().getPackage().getName());


        if (!dir.exists() && !dir.mkdirs()) {

            Toast.makeText(getContext(), "not record", Toast.LENGTH_SHORT);
            file = null;
        } else {
            file = new File(dir.getAbsolutePath(), new SimpleDateFormat(
                    "'IMG_'yyyyMMddHHmmss'.mp4'").format(new Date()));
        }
        return file;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                if (!CameraActivity.cameraActivity.isVideoMode) {
                    mCamera.setPreviewDisplay(surfaceHolder);
                } else {

                    if (initRecorder(surfaceHolder.getSurface()))
                        mMediaRecorder.start();
                }

                startVideo();

                if (!CameraActivity.cameraActivity.isVideoMode)
                    mCamera.startPreview();
            }
        } catch (Exception e) {
            // left blank for now

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        // start preview with new settings
        try {
            if (!CameraActivity.cameraActivity.isVideoMode) {
                mCamera.setPreviewDisplay(surfaceHolder);
            }
//            else {
//                mMediaRecorder.reset();
//                mMediaRecorder.release();
//                mCamera.release();
//
//                // once the objects have been released they can't be reused
//                mMediaRecorder = null;
//                mCamera = null;
//            }
            startVideo();
            if (!CameraActivity.cameraActivity.isVideoMode)

                mCamera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        } catch (Exception e) {

        }
    }

    public void refreshCamera(Camera camera) {
        if (mSurfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        mCamera = camera;
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            startVideo();

            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    Thread[] threads = new Thread[1];
    int rotation = 0;

    private void startVideo() {
//        SurfaceHolder videoCaptureViewHolder = null;
//        try {
//            mCamera = Camera.open();
//        } catch (RuntimeException e) {
//            Log.e("CameraTest", "Camera Open filed");
//            return;
//        }
//        mCamera.setErrorCallback(new Camera.ErrorCallback() {
//            public void onError(int error, Camera camera) {
//            }
//        });
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewFrameRate(30);
//        parameters.setPreviewFpsRange(15000,30000);
//        List<int[]> supportedPreviewFps=parameters.getSupportedPreviewFpsRange();
//        Iterator<int[]> supportedPreviewFpsIterator=supportedPreviewFps.iterator();
//        while(supportedPreviewFpsIterator.hasNext()){
//            int[] tmpRate=supportedPreviewFpsIterator.next();
//            StringBuffer sb=new StringBuffer();
////            sb.append("supportedPreviewRate: ");
//            for(int i=tmpRate.length,j=0;j<i;j++){
//                sb.append(tmpRate[j]+", ");
//            }
////            Log.v("CameraTest",sb.toString());
//        }
//
//        List<Size> supportedPreviewSizes=parameters.getSupportedPreviewSizes();
//        Iterator<Size> supportedPreviewSizesIterator=supportedPreviewSizes.iterator();
//        while(supportedPreviewSizesIterator.hasNext()){
//            Size tmpSize=supportedPreviewSizesIterator.next();
////            Log.v("CameraTest","supportedPreviewSize.width = "+tmpSize.width+"supportedPreviewSize.height = "+tmpSize.height);
//        }

//        mCamera.setParameters(parameters);
//        if (null != mSurfaceHolder)
//            videoCaptureViewHolder = mSurfaceHolder;
//        try {
//            mCamera.setPreviewDisplay(mSurfaceHolder);
//        } catch (Throwable t) {
//        }
//        Log.v("CameraTest","Camera PreviewFrameRate = "+mCamera.getParameters().getPreviewFrameRate());
//        Size previewSize=mCamera.getParameters().getPreviewSize();
//        int dataBufferSize=(int)(500*500*
//                (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat())/8.0));
//        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
//        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
//        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
//        new Thread(){
//            @Override
//            public void run() {
//                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] data, Camera camera) {
////                camera.addCallbackBuffer(data);
//new Thread(){}.start();
//                        try {
//                            if (dataClient == null || !dataClient.isConnected()) {
//                                createDataGoogleApiConnection();
//
//                            }
//
//
//                            if (dataClient != null && dataClient.isConnected()) {
//                                Camera.Parameters parameters = camera.getParameters();
//                                int w = parameters.getPreviewSize().width;
//                                int h = parameters.getPreviewSize().height;
//                                int format = parameters.getPreviewFormat();
//                                YuvImage image = new YuvImage(data, format, w, h, null);
//
//                                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                Rect area = new Rect(0, 0, w, h);
//                                image.compressToJpeg(area, 50, out);
//
//                                Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
//                                Matrix matrix = new Matrix();
//
//                                matrix.postRotate(-90);
//
//                                Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//
//                                Asset asset = createAssetFromBitmap(rotatedBitmap);
//                                final PutDataMapRequest request = PutDataMapRequest.create("/image");
//                                DataMap map = request.getDataMap();
//                                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//                                map.putAsset("profileImage", asset);
//                                Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        }.start();

//         Camera.Parameters parameters = mCamera.getParameters();


        mCamera.setErrorCallback(new Camera.ErrorCallback() {
            public void onError(int error, Camera camera) {
            }
        });
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFrameRate(30);
        parameters.setPreviewFpsRange(15000, 30000);
        List<int[]> supportedPreviewFps = parameters.getSupportedPreviewFpsRange();
        Iterator<int[]> supportedPreviewFpsIterator = supportedPreviewFps.iterator();
        while (supportedPreviewFpsIterator.hasNext()) {
            int[] tmpRate = supportedPreviewFpsIterator.next();
            StringBuffer sb = new StringBuffer();
            sb.append("supportedPreviewRate: ");
            for (int i = tmpRate.length, j = 0; j < i; j++) {
                sb.append(tmpRate[j] + ", ");
            }
            Log.v("CameraTest", sb.toString());
        }

        List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Iterator<Size> supportedPreviewSizesIterator = supportedPreviewSizes.iterator();
        while (supportedPreviewSizesIterator.hasNext()) {
            Size tmpSize = supportedPreviewSizesIterator.next();
        }


        Size previewSize = mCamera.getParameters().getPreviewSize();
        int dataBufferSize = (int) (previewSize.height * previewSize.width *
                (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8.0));
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            public void onPreviewFrame(final byte[] data, final Camera camera) {
//                if(dataClient==null){
//                    return;
//                }


                if (threads[0] != null) {// && threads[1] != null && threads[2] != null ) {
                    return;
                }

                CameraActivity ca = (CameraActivity) getContext();
                if (ca.rotationMode == ca.LANDSHAPE) {
                    rotation = 0;
                }
                if (ca.rotationMode == ca.LANDSHAPE_REVERSE) {
                    rotation = (-180);
                } else if (ca.rotationMode == ca.POIRTRAIT) {
                    if (CameraActivity.BACK_CAMERA == CameraActivity.cameraActivity.curentCameraMode) {
                        rotation = (90);
                    } else
                        rotation = (-90);

                }

                if (threads[0] == null) {
                    threads[0] = new Thread() {
                        @Override
                        public void run() {

                            try {
                                if (dataClient == null || !dataClient.isConnected()) {
                                    createDataGoogleApiConnection();

                                }


                                if (dataClient != null && dataClient.isConnected()) {
                                    try {
                                        Camera.Parameters parameters = camera.getParameters();
                                        int w = parameters.getPreviewSize().width;
                                        int h = parameters.getPreviewSize().height;
                                        int format = parameters.getPreviewFormat();
                                        YuvImage image = new YuvImage(data, format, w, h, null);

                                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                                        Rect area = new Rect(0, 0, w, h);
                                        image.compressToJpeg(area, 50, out);

                                        Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
                                        Matrix matrix = new Matrix();


                                        matrix.postRotate(rotation);

                                        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                                        int sizeW = 400, sizeH = 400;
                                        Bitmap sendedBitmap = getResizedBitmap(rotatedBitmap, sizeW, sizeH);
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        sendedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
                                        byte[] byteArray = stream.toByteArray();
                                        sendImage("/image w=" + sizeW + ",h=" + sizeH, byteArray);
                                        try {
                                            camera.addCallbackBuffer(data);
                                        } catch (Exception e) {
                                            Log.e("CameraTest", "addCallbackBuffer error");

                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                System.gc();

                            }
                            threads[0] = null;
                        }
                    };
                    threads[0].start();
                }
//                else if (threads[1] == null) {
//                    threads[1] = new Thread() {
//                        @Override
//                        public void run() {
//                            try {
//                                if (dataClient == null || !dataClient.isConnected()) {
//                                    createDataGoogleApiConnection();
//
//                                }
//
//
//                                if (dataClient != null && dataClient.isConnected()) {
//                                    Camera.Parameters parameters = camera.getParameters();
//                                    int w = parameters.getPreviewSize().width;
//                                    int h = parameters.getPreviewSize().height;
//                                    int format = parameters.getPreviewFormat();
//                                    YuvImage image = new YuvImage(data, format, w, h, null);
//
//                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                    Rect area = new Rect(0, 0, w, h);
//                                    image.compressToJpeg(area, 50, out);
//
//                                    Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
//                                    Matrix matrix = new Matrix();
//
//                                    matrix.postRotate(rotation);
//
//                                    Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//
//                                    Asset asset = createAssetFromBitmap(rotatedBitmap);
//                                    final PutDataMapRequest request = PutDataMapRequest.create("/image");
//                                    DataMap map = request.getDataMap();
//                                    map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//                                    map.putAsset("profileImage", asset);
//                                    Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//                                    try {
//                                        camera.addCallbackBuffer(data);
//                                    } catch (Exception e) {
//                                        Log.e("CameraTest", "addCallbackBuffer error");
//
//                                    }
//                                }
//
//
//
//                            } catch (OutOfMemoryError e) {
//                                e.printStackTrace();
//                                System.gc();
//
//                            }
//
//                            threads[1] = null;
//                        }
//                    };
//                    threads[1].start();
//                }
//                if (threads[2] == null) {
//                    threads[2] = new Thread() {
//                        @Override
//                        public void run() {
//                            try {
//                                if (dataClient == null || !dataClient.isConnected()) {
//                                    createDataGoogleApiConnection();
//
//                                }
//
//
//                                if (dataClient != null && dataClient.isConnected()) {
//                                    Camera.Parameters parameters = camera.getParameters();
//                                    int w = parameters.getPreviewSize().width;
//                                    int h = parameters.getPreviewSize().height;
//                                    int format = parameters.getPreviewFormat();
//                                    YuvImage image = new YuvImage(data, format, w, h, null);
//
//                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                    Rect area = new Rect(0, 0, w, h);
//                                    image.compressToJpeg(area, 50, out);
//
//                                    Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
//                                    Matrix matrix = new Matrix();
//
//                                    matrix.postRotate(rotation);
//
//                                    Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//
//                                    Asset asset = createAssetFromBitmap(rotatedBitmap);
//                                    final PutDataMapRequest request = PutDataMapRequest.create("/image");
//                                    DataMap map = request.getDataMap();
//                                    map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//                                    map.putAsset("profileImage", asset);
//                                    Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//                                    try {
//                                        camera.addCallbackBuffer(data);
//                                    } catch (Exception e) {
//                                        Log.e("CameraTest", "addCallbackBuffer error");
//
//                                    }
//                                }
//
//
//
//                            } catch (OutOfMemoryError e) {
//                                e.printStackTrace();
//                                System.gc();
//
//                            }
//
//                            threads[2] = null;
//                        }
//                    };
//                    threads[2].start();
//                }
                System.gc();
            }

        });

    }

    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }
    void stopVideo(){
if(mMediaRecorder!=null) {
    mMediaRecorder.reset();
    mMediaRecorder.release();

    // once the objects have been released they can't be reused
    mMediaRecorder = null;
//    mCamera = null;
    refreshCamera(mCamera);


}
    }
    void stopCamera() {
        if (null == mCamera)
            return;
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();

        }
        mCamera = null;
    }

    public void finish() {
        stopCamera();

    }


    public void destroy() {
        if (dataClient != null) {
            dataClient.disconnect();
        }
        dataClient = null;
    }

    GoogleApiClient dataClient;

    public void createDataGoogleApiConnection() {
        dataClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                        Toast.makeText(getContext(), "Connection Faild", Toast.LENGTH_SHORT).show();
                        dataClient = null;
                    }
                })
                .addApi(Wearable.API)
                .build();

        dataClient.connect();

    }


    static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

//    protected void onStop() {
//        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//        Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
//        Asset asset = createAssetFromBitmap(bmp);
//        PutDataMapRequest request = PutDataMapRequest.create("/close");
//        DataMap map = request.getDataMap();
//        map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//        map.putAsset("profileImage", asset);
//        if (dataClient == null) {
//            createDataGoogleApiConnection();
//        }
//        if (dataClient != null) {
//            Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
//
//        }
//    }


    private void sendImage(final String path, final byte[] data) {
        if (dataClient == null) {
            createDataGoogleApiConnection();
            ;
        }

        Wearable.NodeApi.getConnectedNodes(dataClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (final Node node : nodes) {
                    new Thread() {
                        @Override
                        public void run() {

                            //toast(node.getId());
                            ChannelApi.OpenChannelResult result = Wearable.ChannelApi.openChannel(dataClient, node.getId(), path).await();
                            Channel channel = result.getChannel();

//sending file
                            try {
                                channel.getOutputStream(dataClient).await().getOutputStream().write(data, 0, data.length);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }

            }
        });
    }

    public void sendGoBack() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        sendImage("/close", byteArray);
    }

}


