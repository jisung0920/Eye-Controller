package POSCO_AI.e_con.threadClass;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CameraProcessor extends AsyncTask<String, String, Boolean> implements CameraBridgeViewBase.CvCameraViewListener2 {


    private Mat matResult;
    private Boolean previewVisible;
    protected int x, y ,click;
    private View gazePointer;
    private Socket socket;


    public CameraProcessor(View gazePointer) {

        matResult = new Mat();
        previewVisible = false;
        this.gazePointer = gazePointer;


    }

    public void setPreviewVisible(Boolean previewVisible) {
        this.previewVisible = previewVisible;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        inputFrame.rgba().copyTo(matResult);

//        return inputFrame.rgba().t();
        if (previewVisible)
            return inputFrame.rgba().t();
        else
            return null;
    }


    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Thread.sleep(1000);

            String IP = strings[0];
            int PORT = Integer.parseInt(strings[1]);
            Log.d("SERVER", IP + ":" + PORT);

            final int imgSize = (int) (matResult.total() * matResult.channels());
            Log.d("SERVER", "SIZE : " +imgSize);

            byte[] outData = new byte[imgSize];
            byte[] inData = new byte[30];


            Log.d("SERVER", "socket");
            socket = new Socket(IP, PORT); //android - Client  connect with Server socket(python)

            Log.d("SERVER", "STREAM");

            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inStream = new DataInputStream(socket.getInputStream());

            Log.d("SERVER", "WHILE");
            while (true) {
                Thread.sleep(1000);
                matResult.put(0, 0, outData);
                Log.d("SERVER", "WRITE");

                outStream.write(outData);

                Log.d("SERVER", "READ");
//                inStream.read(inData);
//                String msg = new String(inData);
//                publishProgress(msg);
            }

        } catch (Exception e ) {

            Log.e("SERVER", String.valueOf(e));
            e.printStackTrace();

        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onProgressUpdate(String... values) {
        String[] coordinates = values[0].split("/");
        x = Integer.parseInt(coordinates[0]);
        y = Integer.parseInt(coordinates[1]);
        click = Integer.parseInt(coordinates[2]);
        gazePointer.setX(x);
        gazePointer.setY(y);
        if(click==1){
//                EconUtils.gazeTouchMotion(webView,x,y, MotionEvent.ACTION_DOWN);
//                EconUtils.gazeTouchMotion(webView,x,y,MotionEvent.ACTION_UP);
        }
        super.onProgressUpdate(values);
    }


    @Override
    public void onPostExecute(Boolean aBoolean) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPostExecute(aBoolean);
    }
}
