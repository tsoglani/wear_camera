package camera.nikos.tsoglani.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by tsoglani on 25/2/2016.
 */
public class WearService extends WearableListenerService {


    @Override
    public void onChannelOpened(Channel channel) {
String path =channel.getPath();
        Log.e("path =",path);
        if (path.startsWith("/image")) {
            if (CameraActivity.channelClient == null){

                CameraActivity.createDataConnection();
            }
//            if (CameraActivity.channelClient == null || CameraActivity.cameraView == null) {
//                return;
//            }

           String dimansions =path.substring("/image ".length(),path.length());
            Log.e("dimansions",dimansions);

            String dimansionsArray[]=   dimansions.split(",");
            String w=dimansionsArray[0].substring("w=".length(), dimansionsArray[0].length());
            String h=dimansionsArray[1].substring("h=".length(),dimansionsArray[1].length());
//            String os=dimansionsArray[2];
//            String cs=dimansionsArray[3];
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
//            byte[] compressedData = new byte[Integer.parseInt(cs)];
            byte[]   decompressedData= new byte[imageWidth*imageHeight];
//            Toast.makeText(WearService.this, "imageWidth= "+imageWidth+ "  imageWidth= " +imageWidth, Toast.LENGTH_SHORT).show();
            try {

                if(!CameraActivity.channelClient.isConnected()){/////////////////
                    return;
                }

                Log.e("dimansions","is connected");

                channel.getInputStream(CameraActivity.channelClient).await().getInputStream().read(decompressedData);
//                byte[] decompressedData=compressedData;
//                try {
//                  decompressedData = decompress(compressedData,Integer.parseInt(os));
//                } catch (DataFormatException e) {
//                    e.printStackTrace();
//                }

                final  Bitmap bitmap = BitmapFactory.decodeByteArray(decompressedData, 0, decompressedData.length);

                CameraActivity.cameraActivity.runOnUiThread(new Thread() {
                    @Override
                    public void run() {
//                        if( CameraActivity.cameraView!=null){
                            Log.e("dimansions","cameraView is not null ");

                            CameraActivity.cameraView.setBackground(new BitmapDrawable(bitmap));}
//                    }
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

    public static byte[] decompress(byte[] data, int length) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[length];
        while (!inflater.finished()) {
            outputStream.write(buffer, 0, inflater.inflate(buffer));
        }
        inflater.end();
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    //when file is ready
    @Override
    public void onInputClosed(Channel channel, int i, int i1) {

//        Toast.makeText(this, "File received!", Toast.LENGTH_SHORT).show();

    }
}