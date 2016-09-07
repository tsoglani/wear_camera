package wear.nikos.tsoglanakos.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
private RelativeLayout start_layout;
    static boolean isMainOnFront;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_layout=(RelativeLayout)findViewById(R.id.start_layout);
        start_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        isMainOnFront=false;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        isMainOnFront=true;
//    }
private GoogleApiClient client;

    protected void onStop() {
        super.onStop();
        isMainOnFront = false;


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendMessage("/info","exit".getBytes());

    }

    protected void onResume() {
        super.onResume();
        isMainOnFront = true;
    }

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
                                    Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                }
            });
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
}
