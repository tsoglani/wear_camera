package camera.nikos.tsoglani.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends Activity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private RelativeLayout cameraView;
    private Button switchCamera, capture, flash;
    private boolean isFlashUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_camera_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                cameraView = (RelativeLayout) stub.findViewById(R.id.cameraView);
                switchCamera = (Button) findViewById(R.id.switchCamera);
                flash = (Button) findViewById(R.id.flash);
                capture = (Button) findViewById(R.id.capture);
                switchCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/cameraview", "SwitchCamera".getBytes());


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
                                    flash.setBackground(getResources().getDrawable(R.drawable.flash_yes));
                                } else {
                                    flash.setBackground(getResources().getDrawable(R.drawable.flash_no));

                                }
                            }
                        });
                    }
                });
                createDataConnection();
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


    private GoogleApiClient dataClient;

    private GoogleApiClient messageClient;

    public void createDataConnection() {
        dataClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        dataClient.connect();

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
                                if (new String(payload).equals("close_connection")) {
                                    closeDataConnection();
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
    protected void onStop() {
        super.onStop();
        closeDataConnection();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sendMessage("/cameraview", "close_connection".getBytes());
            }
        }.start();

        closeMessageConnection();
    }

    private void closeDataConnection() {
        if (dataClient != null) {
            Wearable.DataApi.removeListener(dataClient, this);
            dataClient.disconnect();
            dataClient = null;
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
        if (dataClient == null || !dataClient.isConnected()) {
            dataClient = null;

        }
        if (dataClient != null)
            Wearable.DataApi.addListener(dataClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE) {
                handleZoom(event);
            }

        }
        return true;
    }

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

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (final DataEvent event : dataEventBuffer) {
            Toast.makeText(CameraActivity.this, event.getDataItem().getUri().getPath(), Toast.LENGTH_SHORT).show();

            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image") || event.getDataItem().getUri().getPath().equals("/cameraImage")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                final Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                new AsyncTask<Void, Void, Void>() {
                    Bitmap bitmap;
                    BitmapDrawable ob;

                    @Override
                    protected Void doInBackground(Void... params) {
                        bitmap = loadBitmapFromAsset(profileAsset);
                        ob = new BitmapDrawable(getResources(), bitmap);

//                        Wearable.DataApi.deleteDataItems(dataClient, getUriForDataItem());
//                        dataClient.disconnect();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (ob != null) {
                            cameraView.setBackground(ob);
                        }
                        ob = null;
//                        sendMessage("/cameraActivity","getCameraPicture".getBytes());


                    }
                }.execute();
//                Bitmap bitmap = loadBitmapFromAsset(profileAsset);


                // Do something with the bitmap
            } else if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/close")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }


        }
        dataEventBuffer.release();
        System.gc();
    }


    private int TIMEOUT_MS = 1000;

    public Bitmap loadBitmapFromAsset(Asset asset) {

        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        if (dataClient == null) {
            return null;
        }
        ConnectionResult result = dataClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                dataClient, asset).await().getInputStream();
//        dataClient.disconnect();

        if (assetInputStream == null) {
            Log.w("request uknown", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(CameraActivity.this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }
}
