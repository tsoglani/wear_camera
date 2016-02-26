package camera.nikos.tsoglani.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;

/**
 * Created by tsoglani on 25/2/2016.
 */
public class WearService extends WearableListenerService {


    @Override
    public void onChannelOpened(Channel channel) {
String path =channel.getPath();
        if (path.startsWith("/image")) {
            if (CameraActivity.channelClient == null || CameraActivity.cameraView == null) {
                return;
            }
           String dimansions =path.substring("/image ".length(),path.length());
         String dimansionsArray[]=   dimansions.split(",");
            String w=dimansionsArray[0].substring("w=".length(), dimansionsArray[0].length());
            String h=dimansionsArray[0].substring("w=".length(),dimansionsArray[0].length());
//            File file = new File("/sdcard/file.png");
//            if (!file.exists())
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    //handle error
//                }

            int imageWidth,imageHeight;
            imageWidth=Integer.parseInt(w);
            imageHeight=Integer.parseInt(h);
            byte[] data = new byte[imageWidth * imageHeight];
//            Toast.makeText(WearService.this, "imageWidth= "+imageWidth+ "  imageWidth= " +imageWidth, Toast.LENGTH_SHORT).show();
            try {
                channel.getInputStream(CameraActivity.channelClient).await().getInputStream().read(data);
               final  Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        CameraActivity.cameraView.setBackground(new BitmapDrawable(bitmap));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
//                channel.receiveFile(channelClient, Uri.fromFile(file), false);
        } else if (path.equals("/close")) {
            CameraActivity.cameraActivity.willSendForClosing=false;
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //when file is ready
    @Override
    public void onInputClosed(Channel channel, int i, int i1) {

//        Toast.makeText(this, "File received!", Toast.LENGTH_SHORT).show();

    }
}