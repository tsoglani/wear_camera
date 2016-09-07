package wear.nikos.tsoglanakos.camera;


import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class MobileService extends WearableListenerService {
    private Camera cam;
    private GoogleApiClient client;
    private String curentCommand;

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.1 */
    class C02411 extends Thread {
        C02411() {
        }

        public void run() {
            CameraActivity.cameraActivity.getCameraInstance(1);
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.2 */
    class C02422 extends Thread {
        C02422() {
        }

        public void run() {
            CameraActivity.cameraActivity.getCameraInstance(1);
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.3 */
    class C02433 extends Thread {
        C02433() {
        }

        public void run() {
            CameraActivity.cameraActivity.capture();
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.4 */
    class C02444 extends Thread {
        C02444() {
        }

        public void run() {
            CameraActivity.cameraActivity.switchFlash();
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.5 */
    class C02455 extends Thread {
        C02455() {
        }

        public void run() {
            CameraActivity.cameraActivity.switchCamera();
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.6 */
    class C07536 implements OnConnectionFailedListener {
        C07536() {
        }

        public void onConnectionFailed(ConnectionResult result) {
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.MobileService.7 */
    class C07557 implements ResultCallback<GetConnectedNodesResult> {
        final /* synthetic */ String val$message;
        final /* synthetic */ byte[] val$payload;

        /* renamed from: camera.nikos.tsoglani.camera.MobileService.7.1 */
        class C07541 implements ResultCallback<SendMessageResult> {
            C07541() {
            }

            public void onResult(SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    MobileService.this.client = null;
                }
            }
        }

        C07557(String str, byte[] bArr) {
            this.val$message = str;
            this.val$payload = bArr;
        }

        public void onResult(GetConnectedNodesResult getConnectedNodesResult) {
            for (Node node : getConnectedNodesResult.getNodes()) {
                Wearable.MessageApi.sendMessage(MobileService.this.client, node.getId(), this.val$message, this.val$payload).setResultCallback(new C07541());
            }
        }
    }

    public MobileService() {
        this.cam = null;
        this.curentCommand = null;
    }
    CameraActivity cameraActivity;
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String message = new String(messageEvent.getData());
        Log.e("message",message);
        if (message.equals("start")) {
            unlockTheScreen();
             cameraActivity = CameraActivity.cameraActivity;
            CameraActivity.willSendForClosing = false;
            if (CameraActivity.cameraActivity != null) {
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


            Intent startMain = new Intent(this, CameraActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);
        }
        if (message.equals("main")) {
            unlockTheScreen();
            cameraActivity = CameraActivity.cameraActivity;
            CameraActivity.willSendForClosing = false;

            Intent startMain = new Intent(this, MainActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);

        } else if (message.equals("OpenFrontCamera")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.runOnUiThread(new C02411());
            }
        } else if (message.equals("OpenBackCamera")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.runOnUiThread(new C02422());
            }
        } else if (message.equals("StopCaptureVideo")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.stopCaptureVideo();
            }
        } else if (message.equals("StartCaptureVideo")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.startCaptureVideo();
            }
        } else if (message.equals("Capture")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.runOnUiThread(new C02433());
            }
        } else if (message.equals("SwitchFlash")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.runOnUiThread(new C02444());
            }
        } else if (message.equals("SwitchCamera")) {
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.runOnUiThread(new C02455());
            }
        } else if ((message.equals("close_connection") || message.equals("close_application")) && (MainActivity.isMainOnFront || CameraActivity.cameraActivity != null)) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);
        }
        if (messageEvent.getPath().equals("/zoom")) {
            int zoom = Integer.parseInt(message);
            if (CameraActivity.cameraActivity != null) {
                CameraActivity.cameraActivity.mCameraPreview.setZoom(zoom);
            }
        }
        this.curentCommand = message;
    }

    private void toast(String s) {
        Toast.makeText(this, s, 0).show();
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
        this.client = new Builder(this).addOnConnectionFailedListener(new C07536()).addApi(Wearable.API).build();
        this.client.connect();
    }

    private void sendMessage(String message, byte[] payload) {
        if (this.client == null || !this.client.isConnected()) {
            onReadyForContent();
        }
        if (this.client != null) {
            Wearable.NodeApi.getConnectedNodes(this.client).setResultCallback(new C07557(message, payload));
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.client != null) {
            this.client.disconnect();
        }
    }
}