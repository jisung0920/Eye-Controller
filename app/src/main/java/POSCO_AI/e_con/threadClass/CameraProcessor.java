package POSCO_AI.e_con.threadClass;

import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class CameraProcessor extends AsyncTask<String, String, Boolean> implements CameraBridgeViewBase.CvCameraViewListener2 {


    private Mat matResult;


    DataInputStream inputStream;
    DataOutputStream outputStream;



    public CameraProcessor() {

        matResult= new Mat();



    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("MATGET","get FRAME");
        inputFrame.rgba().copyTo(matResult);
        return inputFrame.rgba();
    }


    @Override
    protected Boolean doInBackground(String... strings) {
        String IP = strings[0];
        int PORT = Integer.parseInt(strings[1]);
        Log.d("SERVER",IP+":"+PORT);

        Log.d("SERVER","data");
        final int imgSize = (int) (matResult.total() * matResult.channels());
        byte[] data = new byte[imgSize];

        try {

            Log.d("SERVER","socket");
            Socket socket = new Socket(IP,PORT);
            Log.d("SERVER","STREAM");
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            Log.d("SERVER","WRITE");
            while(true){
                Thread.sleep(500);
                matResult.get(0,0,data);
                outStream.write(data);
            }

        } catch (Exception e) {
            Log.e("SERVER", String.valueOf(e));
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPostExecute(Boolean aBoolean) {
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        super.onPostExecute(aBoolean);
    }
}
