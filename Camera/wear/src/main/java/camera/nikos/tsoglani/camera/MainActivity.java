package camera.nikos.tsoglani.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private Button camerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                onReadyForContent();
                if(CameraActivity.cameraActivity==null||CameraActivity.cameraActivity.willSendForClosing)
                sendMessage("/main", "main".getBytes());
                camerButton = (Button) stub.findViewById(R.id.openCamera);
                camerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("/main", "start".getBytes());
                    }
                });
            }
        });
    }


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }

    private void toast(final String s) {

        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

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
                                    if (new String(payload).equals("close_application")) {
                                        client.disconnect();
                                        client = null;
                                    }
//                                    Toast.makeText(MainActivity.this, "sucess", Toast.LENGTH_SHORT).show();
                                } else {
                                    client = null;
                                    Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                }
            });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sendMessage("/close_application", "close_application".getBytes());


    }


    private void goToCameraView() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
        if (client != null) {
            Wearable.MessageApi.removeListener(client, MainActivity.this);
            client.disconnect();
            client = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (client != null) {
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
            client = null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
//        Toast.makeText(MainActivity.this, "onConected", Toast.LENGTH_SHORT).show();
        Wearable.MessageApi.addListener(client, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Log.i(MobileService.class.getSimpleName(), "WEAR Message " + messageEvent.getPath());

        String message = new String(messageEvent.getData());
        if (message.equals("start")) {
            goToCameraView();
        }
    }
}
