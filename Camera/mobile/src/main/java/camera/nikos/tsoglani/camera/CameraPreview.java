package camera.nikos.tsoglani.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.hardware.Camera.Size;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        // Get the pointer's current position
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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceHolder);

                startVideo();


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
            mCamera.setPreviewDisplay(surfaceHolder);
            startVideo();
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
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    Thread thread = null;

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
                if (thread != null && thread.isAlive()) {
                    return;
                }
                thread = new Thread() {
                    @Override
                    public void run() {
                        if (dataClient == null || !dataClient.isConnected()) {
                            createDataGoogleApiConnection();

                        }


                        if (dataClient != null && dataClient.isConnected()) {
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

                            matrix.postRotate(-90);

                            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                            Asset asset = createAssetFromBitmap(rotatedBitmap);
                            final PutDataMapRequest request = PutDataMapRequest.create("/image");
                            DataMap map = request.getDataMap();
                            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                            map.putAsset("profileImage", asset);
                            Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());
                        }


                        try {
                            camera.addCallbackBuffer(data);
                        } catch (Exception e) {
                            Log.e("CameraTest", "addCallbackBuffer error");

                        }

                        thread = null;
                    }
                };
                thread.start();
                System.gc();
            }

        });

    }

    void stopVideo() {
        if (null == mCamera)
            return;
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mCamera = null;
    }

    public void finish() {
        stopVideo();

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

    protected void onStop() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
        Asset asset = createAssetFromBitmap(bmp);
        PutDataMapRequest request = PutDataMapRequest.create("/close");
        DataMap map = request.getDataMap();
        map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
        map.putAsset("profileImage", asset);
        if (dataClient == null) {
            createDataGoogleApiConnection();
        }
        if (dataClient != null) {
            Wearable.DataApi.putDataItem(dataClient, request.asPutDataRequest());

        }
    }

}


