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

    final String IP;
    final int PORT;
//    Socket socket;
    DatagramSocket socket;

    DataInputStream inputStream;
    DataOutputStream outputStream;



    public CameraProcessor(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
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
        inputFrame.rgba().t().copyTo(matResult);
        return inputFrame.rgba();
    }


    @Override
    protected Boolean doInBackground(String... strings) {
        byte[] datafile =null;
        byte[] matBuffer;

        try {

            while (true) {
                Thread.sleep(1000);
                Log.d("MATGET", matResult.get(300, 300)[0] + "");
            }
//            socket = new DatagramSocket(8500); // 소켓을 생성한다.
//            DatagramPacket packet;
//            byte[] buf;
//            buf = new byte[20];
//            packet = new DatagramPacket(buf, buf.length);
//            socket.setSoTimeout(5000);
//
//            while (true) {
//                socket.receive(packet);
//                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
//                Log.d("UDP", msg+"-"+matResult.get(300,300));
//
//                publishProgress(msg);
//            }



//            socket = new Socket(IP, PORT);
//            outputStream = new DataOutputStream(socket.getOutputStream());
//            inputStream = new DataInputStream(socket.getInputStream());
//            Log.d("SERVER","SOCKET");
//            while(true){
//                Log.d("SERVER","DATA");
//                Mat matData = new Mat();
//                matResult.copyTo(matData);
//
////                matBuffer = new byte[(int) (600*900*3)];
////                matData.get(0, 0, matBuffer);
//                byte[] tmp = "tmp".getBytes();
//
//                Log.d("SERVER","READY");
//                outputStream.write(tmp);
//                Log.d("SERVER","WRITE");
//
//                inputStream.read();
//            }

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
