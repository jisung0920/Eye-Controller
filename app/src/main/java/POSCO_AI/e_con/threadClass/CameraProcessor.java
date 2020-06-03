package POSCO_AI.e_con.threadClass;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import POSCO_AI.e_con.EconUtils;
import POSCO_AI.e_con.R;


public class CameraProcessor extends AsyncTask<String, String, Boolean> implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "CameraProcessor";


    private Mat matResult;
    private Boolean previewVisible;
    protected int x, y ,click,scroll;
    private int upTo,bottomTo;
    private View gazePointer;
    private Socket socket;
    private String serverIP;
    private int serverPORT;
    private WebView webView;
    private ImageView emotionView;
    private int W,H;
    private int blinkCounter = 0,scrollCounter=0;
    private final int BLINK_TH = 3,SCROLL_TH=3;


    public CameraProcessor(View gazePointer,String IP,int PORT, WebView webView,ImageView emotionView,int W, int H) {

        matResult = new Mat();
        previewVisible = false;
        this.gazePointer = gazePointer;
        this.serverIP = IP;
        this.serverPORT = PORT;
        this.webView = webView;
        this.emotionView = emotionView;
        this.W = W;
        this.H = H;

    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public void setServerPORT(int serverPORT) {
        this.serverPORT = serverPORT;
    }

    public void setPreviewVisible(Boolean previewVisible) {
        this.previewVisible = previewVisible;
    }

    public void setGazePointer(View gazePointer) {
        this.gazePointer = gazePointer;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onImageAvailable: 소켓 연결 대기중 IP: " + serverIP + " PORT: " + serverPORT);
                    socket = new Socket(serverIP, serverPORT);
                    Log.d(TAG, "onImageAvailable: 소켓 연결 성공 IP: " + serverIP + " PORT: " + serverPORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: 실행");
        // inputFrame을 matResult에 복사, 서버에 전송되는 이미지가 세로방향에 맞게 나오게 하기 위해 t()로 행렬 전치
        inputFrame.rgba().t().copyTo(matResult);
        // 이미지 크기 변환
        Size size = new Size(480, 640);
        Imgproc.resize(matResult, matResult, size);

        Imgproc.resize(matResult, matResult, size);
        // 세로방향에 맞게 나오게 하기 위해 이미지를 뒤집음. 수평으로는 양수, 수직으로 0, 모두 뒤집기는 음수
        Core.flip(matResult, matResult, -1);



        try {
            // 비트맵 생성
            Bitmap bitmap = Bitmap.createBitmap(matResult.width() , matResult.height(), Bitmap.Config.ARGB_8888);
            Log.d(TAG, "onCameraFrame: width: " + matResult.width() + "height: " + matResult.height());
            Utils.matToBitmap(matResult, bitmap);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            OutputStream os = null;
            InputStream is =null;

            if (socket != null) {
                os = socket.getOutputStream();
                is = socket.getInputStream();
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ba);

            byte[] bytes = ba.toByteArray();

            int len = bytes.length;
            StringBuilder stLen = new StringBuilder(String.valueOf(len));

            int wantDigit = 10;
            int currentDigit = stLen.length();

            for (int i = 0; i < wantDigit - currentDigit; i++) {
                stLen.append(' ');
            }

            byte[] stLenBytes = stLen.toString().getBytes();
            // 바이트 배열의 길이를 스트림에서 출력하여 서버에 전송
            os.write(stLenBytes);
//            Log.d(TAG, "onCameraFrame: ba: " + Arrays.toString(bytes));
            // 바이트 배열을 스트림에서 출력하여 서버에 전송
            ba.writeTo(os);
            byte[] receiveBuffer = new byte[64] ;
            int buffer_count = is.read(receiveBuffer);
            String strData = new String(receiveBuffer, 0, buffer_count, "UTF-8");
            Log.d(TAG, "Cordi "+strData);
            publishProgress(strData);
        } catch (Exception e) {
            e.printStackTrace();
        }




        // 뷰에서 전면카메라 이미지를 보려면 matResult를 반환하고 보이지 않게 하려면 null 반환
        if (previewVisible) {
            Log.d(TAG, "onCameraFrame: previewVisible: " + previewVisible);
//             return inputFrame.rgba().t();
            return matResult;
            // 480x640으로 이미지를 바꾸려면 return null로 해야 동작
//            return null;
        } else {
            Log.d(TAG, "onCameraFrame: previewVisible 없음: " + previewVisible);
            return null;
        }
    }


    @Override
    protected Boolean doInBackground(String... strings) {

        /* 서버에 전면카메라 이미지 바이트 배열 전송 */


        return null;
    }

    @Override
    public void onProgressUpdate(String... values) {
        String[] coordinates = values[0].split("/");
        int emotionNum=0;
        x = Integer.parseInt(coordinates[0]);
        y = Integer.parseInt(coordinates[1]);
        click = Integer.parseInt(coordinates[2]);
        scroll = Integer.parseInt((coordinates[3]));
        emotionNum = Integer.parseInt((coordinates[4]));
        gazePointer.setX(x);
        gazePointer.setY(y);
        if(click==1)
            blinkCounter++;
        else
            blinkCounter=0;

        if(blinkCounter==BLINK_TH){
            EconUtils.gazeTouchMotion(webView,x,y, MotionEvent.ACTION_DOWN);
            EconUtils.gazeTouchMotion(webView,x,y,MotionEvent.ACTION_UP);
            blinkCounter=0;
            emotionNum = 0;
        }
        if(scroll==1)
            scrollCounter++;
        else
            scrollCounter=0;

        if(scrollCounter>SCROLL_TH){
            if(scroll==1) {
                if (y<H*(0.3)) {
                    if (webView.getScrollY() > 850) {
                        upTo = webView.getScrollY() - 850;
                    } else {
                        upTo = 0;
                    }

                    ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), upTo);
                    anim.setDuration(1000).start();

                }
                if (y>H*(0.7)) {

                    bottomTo = webView.getScrollY() + 850;
                    ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), bottomTo);
                    anim.setDuration(1000).start();
                }
                emotionNum = 0;
            }
        }

        if(emotionNum == 1) {
            touchLike();
            emotionView.setImageResource(R.drawable.happy);
        }
        else if(emotionNum == 2)
            emotionView.setImageResource(R.drawable.surprise);
        else if(emotionNum == 3){
            touchdisLike();
            emotionView.setImageResource(R.drawable.sad);
        }
        else if(emotionNum == 4){
            touchdisLike();
            emotionView.setImageResource(R.drawable.anger);
        }
        else if(emotionNum == 5)
            emotionView.setImageResource(R.drawable.disgust);
        else if(emotionNum == 6)
            emotionView.setImageResource(R.drawable.fear);
        else
            emotionView.setImageResource(R.drawable.neutral);


        super.onProgressUpdate(values);
    }

    public void touchLike(){
        EconUtils.gazeTouchMotion(webView, 143, 1070, MotionEvent.ACTION_DOWN);
        EconUtils.gazeTouchMotion(webView, 143, 1070, MotionEvent.ACTION_UP);
    }
    public void touchdisLike(){
        EconUtils.gazeTouchMotion(webView, 360, 1070, MotionEvent.ACTION_DOWN);
        EconUtils.gazeTouchMotion(webView, 360, 1070, MotionEvent.ACTION_UP);
    }

    @Override
    public void onPostExecute(Boolean aBoolean) {


        super.onPostExecute(aBoolean);
    }
}
