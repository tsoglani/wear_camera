package camera.nikos.tsoglani.camera;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.PowerManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class MobileService extends WearableListenerService {
    private Camera cam = null;
    private String curentCommand = null;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        //Log.i(MobileService.class.getSimpleName(), "WEAR Message " + messageEvent.getPath());

        String message = new String(messageEvent.getData());
//        Toast.makeText(MobileService.this, "Message = " + message, Toast.LENGTH_SHORT).show();


        if (message.equals("start")) {
            unlockTheScreen();
            if (CameraActivity.cameraActivity != null) {// not tested .. is for restart when the application in mobile is already open ( rare )
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startMain);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }



            Intent intent = new Intent(this, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);



        } else if (message.equals("OpenFrontCamera")) {
            CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                @Override
                public void run() {
                    CameraActivity.cameraActivity.getCameraInstance(CameraActivity.BACK_CAMERA);
                }
            });
        } else if (message.equals("OpenBackCamera")) {
            CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                @Override
                public void run() {
                    CameraActivity.cameraActivity.getCameraInstance(CameraActivity.BACK_CAMERA);
                }
            });
        } else if (message.equals("Capture")) {
            CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                @Override
                public void run() {
                    CameraActivity.cameraActivity.capture();
                }
            });
        } else if (message.equals("SwitchFlash")) {
            CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                @Override
                public void run() {
                    CameraActivity.cameraActivity.switchFlash();
                }
            });
        } else if (message.equals("SwitchCamera")) {
            CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                @Override
                public void run() {
                    CameraActivity.cameraActivity.switchCamera();
                }
            });
        } else if (message.equals("close_connection") || message.equals("close_application")) {
            Toast.makeText(MobileService.this, "close_connection", Toast.LENGTH_SHORT).show();
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);
        }


        if (messageEvent.getPath().equals("/zoom")) {

            int zoom = Integer.parseInt(message);
            CameraActivity.cameraActivity.mCameraPreview.setZoom(zoom);
        }


//        else if (message.equals("getCameraPicture")) {
//            Toast.makeText(MobileService.this, "Message = " + message, Toast.LENGTH_SHORT).show();
//            CameraActivity.cameraActivity.sendCameraBitmap();
//        }

        curentCommand = message;
    }

    private void toast(final String s) {

        Toast.makeText(MobileService.this, s, Toast.LENGTH_SHORT).show();

    }

private void unlockTheScreen(){
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    boolean isScreenOn = powerManager.isScreenOn();

    if (!isScreenOn) {
        //Screen is in OFF State
        //Code to power on and release lock



        KeyguardManager km = (KeyguardManager) this
                .getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km
                .newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();
    }
}
    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
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

                                } else {
                                    client = null;
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
        if (client != null) {
            client.disconnect();

        }

    }

}