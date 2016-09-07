package wear.nikos.tsoglanakos.camera;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.Wearable;

public class CameraActivity extends Activity implements ConnectionCallbacks, MessageApi.MessageListener {
    static CameraActivity cameraActivity;
    static RelativeLayout cameraView;
    static GoogleApiClient channelClient;
    static boolean willSendForClosing;
    private Button capture;
    private Theme defaultTheme;
    private Button flash;
    private boolean isFlashUsed;
    boolean isTakingVIdeo;
    private static String lastVieoCommand;
    private GoogleApiClient messageClient;
    private PointF previousZoomPoint;
    private Button switchCamera;
    private Button video;
    int zoom;
    private GoogleApiClient client;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String message = new String(messageEvent.getData());

        Log.e("info",message);

        if(message.equals("startCapturing")){
            runOnUiThread(new startCaptureVideoThread());
        }else if(message.equals("stopCapturing")){
            runOnUiThread(new stopCaptureVideoThread());
        }else if(message.equals("exit")){
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cameraActivity=this;
    }

    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.2 */
    class stopCaptureVideoThread extends Thread {
        stopCaptureVideoThread() {
        }

        public void run() {
            try {
                CameraActivity.this.video.setBackgroundResource(R.drawable.video);
                CameraActivity.this.capture.setVisibility(View.VISIBLE);
                CameraActivity.this.flash.setVisibility(View.VISIBLE);
                CameraActivity.this.switchCamera.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                CameraActivity.this.toast("Error" + e.getMessage());
            }
        }
    }


    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.3 */
    class startCaptureVideoThread extends Thread {
        startCaptureVideoThread() {
        }

        public void run() {
            try {
                CameraActivity.this.video.setBackgroundResource(R.drawable.stop);
                CameraActivity.this.capture.setVisibility(View.INVISIBLE);
                CameraActivity.this.flash.setVisibility(View.INVISIBLE);
                CameraActivity.this.switchCamera.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                CameraActivity.this.toast("Error" + e.getMessage());
            }
        }
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

    }    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.6 */
    class C02236 extends Thread {
        final /* synthetic */ String val$s;

        C02236(String str) {
            this.val$s = str;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r4 = this;
            r1 = camera.nikos.tsoglani.camera.CameraActivity.this;	 Catch:{ Exception -> 0x000d, Error -> 0x0018 }
            r2 = r4.val$s;	 Catch:{ Exception -> 0x000d, Error -> 0x0018 }
            r3 = 0;
            r1 = android.widget.Toast.makeText(r1, r2, r3);	 Catch:{ Exception -> 0x000d, Error -> 0x0018 }
            r1.show();	 Catch:{ Exception -> 0x000d, Error -> 0x0018 }
        L_0x000c:
            return;
        L_0x000d:
            r0 = move-exception;
        L_0x000e:
            r1 = "Error CameraActToast";
            r2 = r0.getMessage();
            android.util.Log.e(r1, r2);
            goto L_0x000c;
        L_0x0018:
            r0 = move-exception;
            goto L_0x000e;
            */
//            throw new UnsupportedOperationException("Method not decompiled: camera.nikos.tsoglani.camera.CameraActivity.6.run():void");
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.7 */
    class C02247 extends Thread {
        final /* synthetic */ String val$message;
        final /* synthetic */ byte[] val$payload;

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.7.1 */
        class C03871 implements ResultCallback<GetConnectedNodesResult> {

            /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.7.1.1 */
            class C03861 implements ResultCallback<SendMessageResult> {
                C03861() {
                }

                public void onResult(SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        CameraActivity.this.toast("Not connected with phone device");
                    } else if (new String(C02247.this.val$payload).equals("main")) {
                        CameraActivity.this.closeDataConnection();
                        CameraActivity.this.closeMessageConnection();
                    } else if (new String(C02247.this.val$payload).equals("StartCaptureVideo")) {
                        CameraActivity.this.startVideoReceivedFunction();
                        CameraActivity.this.lastVieoCommand = "StartCaptureVideo";
                    } else if (new String(C02247.this.val$payload).equals("StopCaptureVideo")) {
                        CameraActivity.this.stopVideoReceivedFunction();
                        CameraActivity.this.lastVieoCommand = "StopCaptureVideo";
                    } else if (C02247.this.val$message.equals("/close_application")) {
                        CameraActivity.this.closeDataConnection();
                        CameraActivity.this.closeMessageConnection();
                    }
                }
            }

            C03871() {
            }

            public void onResult(GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    Wearable.MessageApi.sendMessage(CameraActivity.this.messageClient, node.getId(), C02247.this.val$message, C02247.this.val$payload).setResultCallback(new C03861());
                }
            }
        }

        C02247(String str, byte[] bArr) {
            this.val$message = str;
            this.val$payload = bArr;
        }

        public void run() {
            if (CameraActivity.this.messageClient == null || !CameraActivity.this.messageClient.isConnected()) {
                CameraActivity.this.createMessageConnection();
            }
            Wearable.NodeApi.getConnectedNodes(CameraActivity.this.messageClient).setResultCallback(new C03871());
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1 */
    class OnLayoutInflatedListener implements WatchViewStub.OnLayoutInflatedListener {

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.1 */
        class C02141 implements OnClickListener {
            C02141() {
            }

            public void onClick(View v) {
                CameraActivity.this.sendMessage("/cameraview", "SwitchCamera".getBytes());
            }
        }

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.2 */
        class C02162 implements OnClickListener {

            /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.2.1 */
            class C02151 extends Thread {
                C02151() {
                }

                public void run() {
                    try {
                        if (CameraActivity.this.lastVieoCommand == null || CameraActivity.this.lastVieoCommand.equals("StopCaptureVideo")) {

                            if (CameraActivity.this.isTakingVIdeo) {
                                CameraActivity.this.sendMessage("/cameraview", "StopCaptureVideo".getBytes());
                                lastVieoCommand="StopCaptureVideo";
                            } else {
                                CameraActivity.this.sendMessage("/cameraview", "StartCaptureVideo".getBytes());
                                lastVieoCommand="StartCaptureVideo";
                            }
                        }
//                        if (CameraActivity.this.lastVieoCommand.equals("StartCaptureVideo")) {
//                            CameraActivity.this.isTakingVIdeo = false;
//                        }
                        if (CameraActivity.this.isTakingVIdeo) {
                            CameraActivity.this.sendMessage("/cameraview", "StopCaptureVideo".getBytes());
                        } else {
                            CameraActivity.this.sendMessage("/cameraview", "StartCaptureVideo".getBytes());
                        }
                        CameraActivity.this.isTakingVIdeo =     !CameraActivity.this.isTakingVIdeo ;;
                    } catch (Exception e) {
                        e.printStackTrace();
//                        CameraActivity.this.isTakingVIdeo = false;
                    }
                }
            }

            C02162() {
            }

            public void onClick(View v) {
                CameraActivity.this.runOnUiThread(new C02151());
            }
        }

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.3 */
        class C02173 implements OnClickListener {
            C02173() {
            }

            public void onClick(View v) {
                CameraActivity.this.sendMessage("/cameraview", "Capture".getBytes());
            }
        }

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.4 */
        class C02194 implements OnClickListener {

            /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.4.1 */
            class C02181 extends Thread {
                C02181() {
                }

                public void run() {
                    if (CameraActivity.this.isFlashUsed) {
                        CameraActivity.this.flash.setBackground(CameraActivity.this.getResources().getDrawable(R.drawable.flash_yes_blue_));
                    } else {
                        CameraActivity.this.flash.setBackground(CameraActivity.this.getResources().getDrawable(R.drawable.flash_no_blue2));
                    }
                }
            }

            C02194() {
            }

            public void onClick(View v) {
                CameraActivity.this.sendMessage("/cameraview", "SwitchFlash".getBytes());
                CameraActivity.this.isFlashUsed = !CameraActivity.this.isFlashUsed;
                CameraActivity.this.runOnUiThread(new C02181());
            }
        }

        /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.1.5 */
        class C02205 implements OnTouchListener {
            C02205() {
            }

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 2) {
                    if (CameraActivity.this.previousZoomPoint == null) {
                        CameraActivity.this.previousZoomPoint = new PointF(event.getX(), event.getY());
                    } else {
                        CameraActivity cameraActivity;
                        float different = CameraActivity.this.previousZoomPoint.y - event.getY();
                        boolean haveZoom = false;
                        if (different > 0.0f && CameraActivity.this.zoom < 100) {
                            cameraActivity = CameraActivity.this;
                            cameraActivity.zoom++;
                            haveZoom = true;
                        }
                        if (different < 0.0f && CameraActivity.this.zoom > 0) {
                            cameraActivity = CameraActivity.this;
                            cameraActivity.zoom--;
                            haveZoom = true;
                        }
                        if (haveZoom) {
                            CameraActivity.this.sendMessage("/zoom", Integer.toString(CameraActivity.this.zoom).getBytes());
                        }
                        CameraActivity.this.previousZoomPoint = null;
                    }
                }
                return true;
            }
        }

        OnLayoutInflatedListener() {
        }


@Override
        public void onLayoutInflated(WatchViewStub stub) {
            CameraActivity.cameraActivity = CameraActivity.this;
            CameraActivity.this.defaultTheme = CameraActivity.this.getTheme();
            CameraActivity.cameraView = (RelativeLayout) stub.findViewById(R.id.cameraView);
            CameraActivity.this.switchCamera = (Button) CameraActivity.this.findViewById(R.id.switchCamera);
            CameraActivity.this.video = (Button) CameraActivity.this.findViewById(R.id.video);
            CameraActivity.this.flash = (Button) CameraActivity.this.findViewById(R.id.flash);
            CameraActivity.this.capture = (Button) CameraActivity.this.findViewById(R.id.capture);
            CameraActivity.this.switchCamera.setOnClickListener(new C02141());
            CameraActivity.this.video.setOnClickListener(new C02162());
            CameraActivity.this.capture.setOnClickListener(new C02173());
            CameraActivity.this.flash.setOnClickListener(new C02194());
            CameraActivity.createDataConnection();
            CameraActivity.cameraView.requestFocus();
            CameraActivity.cameraView.setOnTouchListener(new C02205());
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.4 */
    static class C03844 implements OnConnectionFailedListener {
        C03844() {
        }

        public void onConnectionFailed(ConnectionResult result) {
//            CameraActivity.this.toast("Connection Faild");
        }
    }

    /* renamed from: camera.nikos.tsoglani.camera.CameraActivity.5 */
    class C03855 implements OnConnectionFailedListener {
        C03855() {
        }

        public void onConnectionFailed(ConnectionResult result) {
            CameraActivity.this.toast("Connection Faild");
        }
    }

    public CameraActivity() {
        this.isFlashUsed = false;
        this.zoom = 0;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ((WatchViewStub) findViewById(R.id.watch_camera_view_stub)).setOnLayoutInflatedListener(new OnLayoutInflatedListener());
    }

    public void stopVideoReceivedFunction() {
        runOnUiThread(new stopCaptureVideoThread());
    }

    public void startVideoReceivedFunction() {
        runOnUiThread(new startCaptureVideoThread());
    }

    public static void createDataConnection() {
        try{
        channelClient = new Builder(cameraActivity).addConnectionCallbacks(cameraActivity).addOnConnectionFailedListener(new C03844()).addApi(Wearable.API).build();
        channelClient.connect();
    }catch (Exception e){
            e.printStackTrace();
        }}

    public void createMessageConnection() {
        this.messageClient = new Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(new C03855()).addApi(Wearable.API).build();
        this.messageClient.connect();
    }

    private void toast(String s) {
        runOnUiThread(new C02236(s));
    }

    static {
        willSendForClosing = true;
    }

    protected void onResume() {
        super.onResume();
        onReadyForContent();

        cameraActivity = this;
        willSendForClosing = true;
    }

    private void sendMessage(String message, byte[] payload) {
        new C02247(message, payload).start();
    }



    private void closeDataConnection() {
        if (channelClient != null) {
            channelClient.disconnect();
            channelClient = null;
        }
    }

    private void closeMessageConnection() {
        if (this.messageClient != null) {
            this.messageClient.disconnect();
            this.messageClient = null;
        }
    }

    public void onConnected(Bundle connectionHint) {
        if(client==null){
            onReadyForContent();
        }
        Wearable.MessageApi.addListener(client, this);

        if (channelClient == null || !channelClient.isConnected()) {
            channelClient = null;
        }

    }

    public void onConnectionSuspended(int i) {
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (client != null) {
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
            client = null;
        }
        cameraActivity = null;
        if (willSendForClosing) {
            sendMessage("/close_application", "main".getBytes());
        }
    }
}