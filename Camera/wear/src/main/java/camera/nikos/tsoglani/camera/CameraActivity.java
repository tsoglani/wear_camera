package camera.nikos.tsoglani.camera;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.WatchViewStub;
import android.view.MotionEvent;
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
import java.util.List;


public class CameraActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static RelativeLayout cameraView;
    private Button switchCamera, capture, flash,video;
    private boolean isFlashUsed = false;
    private Resources.Theme defaultTheme;
    static CameraActivity cameraActivity;
boolean isTakingVIdeo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_camera_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                cameraActivity = CameraActivity.this;
                defaultTheme = getTheme();
                cameraView = (RelativeLayout) stub.findViewById(R.id.cameraView);
                switchCamera = (Button) findViewById(R.id.switchCamera);
                video=(Button)findViewById(R.id.video);
                flash = (Button) findViewById(R.id.flash);
                capture = (Button) findViewById(R.id.capture);
                switchCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/cameraview", "SwitchCamera".getBytes());


                    }
                });

                video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isTakingVIdeo=!isTakingVIdeo;

                        if(isTakingVIdeo){
                            sendMessage("/cameraview","StartCaptureVideo".getBytes());
                            video.setBackgroundResource(R.drawable.stop);
                        }else{
                            sendMessage("/cameraview","StopCaptureVideo".getBytes());
                            video.setBackgroundResource(R.drawable.video);
                        }
                    }
                });
                capture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/cameraview", "Capture".getBytes());
                    }
                });
                flash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/cameraview", "SwitchFlash".getBytes());
                        isFlashUsed = !isFlashUsed;
                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {
                                if (isFlashUsed) {
                                    flash.setBackground(getResources().getDrawable(R.drawable.flash_yes_blue_));
                                } else {
                                    flash.setBackground(getResources().getDrawable(R.drawable.flash_no_blue2));

                                }
                            }
                        });
                    }
                });
                createDataConnection();
                cameraView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();

                        Resources.Theme theme=getTheme();
                        if (event.getPointerCount() == 2) {
                            // handle multi-touch events
                            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                                mDist = getFingerSpacing(event);
                            } else if (action == MotionEvent.ACTION_MOVE) {
                                handleZoom(event);
                            }
//                            getWindow().setType(android.R.attr.windowSwipeToDismiss);
                            //windowSwipeToDismiss  getWindow().setType(android.R.attr.windowSwipeToDismiss);
//                            setTheme(R.style.AppTheme);
//                            setContentView(R.layout.activity_camera);

                        } else {
//                            setTheme(R.style.Theme_Wearable);
//                            setContentView(R.layout.activity_camera);
                            return false;
                        }
                        return true;

                    }
                });
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
//                            sendMessage("/cameraActivity", "getCameraPicture".getBytes());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
    }


    static GoogleApiClient channelClient;

    private GoogleApiClient messageClient;

    public void createDataConnection() {
        channelClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        channelClient.connect();

    }



    public void createMessageConnection() {
        messageClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        messageClient.connect();

    }


    private void toast(final String s) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(CameraActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
   static boolean willSendForClosing=true;

    @Override
    protected void onResume() {
        super.onResume();
        willSendForClosing=true;
    }

    private void sendMessage(final String message, final byte[] payload) {


        if (messageClient == null || !messageClient.isConnected()) {
            createMessageConnection();
        }

        Wearable.NodeApi.getConnectedNodes(messageClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {
                    Wearable.MessageApi.sendMessage(messageClient, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                if (new String(payload).equals("main")) {
                                    closeDataConnection();
                                    closeMessageConnection();
                                }
                            } else {

                                toast("Not connected with phone device");
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(willSendForClosing)
        sendMessage("/close_application", "main".getBytes());
//        closeDataConnection();

    }

    private void closeDataConnection() {
        if (channelClient != null) {
//            Wearable.DataApi.removeListener(channelClient, this);
            channelClient.disconnect();
            channelClient = null;
        }
    }

    private void closeMessageConnection() {
        if (messageClient != null) {
            messageClient.disconnect();
            messageClient = null;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (channelClient == null || !channelClient.isConnected()) {
            channelClient = null;

        }
//        if (channelClient != null)
//            Wearable.DataApi.addListener(channelClient, this);

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Get the pointer ID
//        int action = event.getAction();
//
//
//        if (event.getPointerCount() > 1) {
//            // handle multi-touch events
//            if (action == MotionEvent.ACTION_POINTER_DOWN) {
//                mDist = getFingerSpacing(event);
//            } else if (action == MotionEvent.ACTION_MOVE) {
//                handleZoom(event);
//            }
//
//        }
//        return true;
//    }

    float mDist;
    int zoom = 0;

    private void handleZoom(MotionEvent event) {
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < 100)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        sendMessage("/zoom", Integer.toString(zoom).getBytes());
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);


        return (float) Math.sqrt(x * x + y * y);
    }

//    AsyncTask<Void, Void, Void> async;
//
//    @Override
//    public void onDataChanged(DataEventBuffer dataEventBuffer) {
//
//        for (final DataEvent event : dataEventBuffer) {
//            Toast.makeText(CameraActivity.this, event.getDataItem().getUri().getPath(), Toast.LENGTH_SHORT).show();
//
//            if (event.getType() == DataEvent.TYPE_CHANGED &&
//                    event.getDataItem().getUri().getPath().equals("/image") || event.getDataItem().getUri().getPath().equals("/cameraImage")) {
//                if(async!=null){
//                    continue;
//                }
//                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
//                final Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
//
//                async = new AsyncTask<Void, Void, Void>() {
//                    Bitmap bitmap;
//                    BitmapDrawable ob;
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        bitmap = loadBitmapFromAsset(profileAsset);
//                        ob = new BitmapDrawable(getResources(), bitmap);
//
////                        Wearable.DataApi.deleteDataItems(channelClient, getUriForDataItem());
////                        channelClient.disconnect();
//
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void aVoid) {
//                        super.onPostExecute(aVoid);
//                        if (ob != null) {
//                            cameraView.setBackground(ob);
//                        }
////                        ob = null;
////                        sendMessage("/cameraActivity","getCameraPicture".getBytes());
//                        async=null;
//System.gc();
//                    }
//                };
//                async.execute();
////                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
//
//
//                // Do something with the bitmap
//            } else if (event.getType() == DataEvent.TYPE_CHANGED &&
//                    event.getDataItem().getUri().getPath().equals("/close")) {
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//
//
//        }
//        dataEventBuffer.release();
//        System.gc();
//    }


//    private int TIMEOUT_MS = 1000;
//
//    public Bitmap loadBitmapFromAsset(Asset asset) {
//
//        if (asset == null) {
//            throw new IllegalArgumentException("Asset must be non-null");
//        }
//        if (channelClient == null) {
//            return null;
//        }
//        ConnectionResult result = channelClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
//        if (!result.isSuccess()) {
//            return null;
//        }
//        // convert asset into a file descriptor and block until it's ready
//        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                channelClient, asset).await().getInputStream();
////        channelClient.disconnect();
//
//        if (assetInputStream == null) {
//            Log.w("request uknown", "Requested an unknown Asset.");
//            return null;
//        }
//        // decode the stream into a bitmap
//        return BitmapFactory.decodeStream(assetInputStream);
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(CameraActivity.this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }
}
