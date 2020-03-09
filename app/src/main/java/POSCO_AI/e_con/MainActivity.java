package POSCO_AI.e_con;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import POSCO_AI.e_con.threadClass.CoordinateReceiverTask;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity{

    WebView webView;
    ImageView gazePointer;
    FloatingActionButton fabMain,fabGaze,fabSetting;
    Animation fabOpen,fabClose;

    static int sPORT = 8000;
    static int imgPORT = 9000;

    boolean pointerVisible ;
    boolean socketUsage;
    boolean fabState;

    CoordinateReceiverTask coordinateReceiverTask;
    private static final int REQUEST_CAMERA = 1;

    final String startUrl = "https://www.autodraw.com/";

    final static String TAG = "Open";

    private CameraBridgeViewBase mOpenCvCameraView;


    CameraProcessor cameraProcessor;


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initVariables();
        setWeb(webView);





    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {

                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    private void initView(){
        webView = findViewById(R.id.webView);
        gazePointer = findViewById(R.id.gazePointer);
        fabMain = findViewById(R.id.fabMain);
        fabGaze = findViewById(R.id.fabGaze);
        fabSetting = findViewById(R.id.fabSetting);

        mOpenCvCameraView = findViewById(R.id.activity_surface_view);

    }

    private void initVariables() {

        pointerVisible = false;
        socketUsage = true;
        fabState =true;

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        fabViewAni();


        cameraProcessor = new CameraProcessor();


        mOpenCvCameraView.setCvCameraViewListener(cameraProcessor);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.disableView();


    }


    private void setWeb(WebView webView){
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float tmpx = event.getX();
                float tmpy = event.getY();
                Toast.makeText(MainActivity.this, "Coordi :"+ tmpx+"/"+tmpy, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(startUrl);
    }

    public void onClick(View v){
        if(v.getId() == R.id.fabMain) {
            fabViewAni();

        }

        else if(v.getId()==R.id.fabGaze){

            if(pointerVisible == true){
                gazePointer.setVisibility(View.INVISIBLE);
                coordinateReceiverTask.onPostExecute(true);
                Snackbar.make(v, "Gaze Tracking Deactivation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                fabGaze.setImageResource(R.drawable.eye_fb_icon);


                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setVisibility(View.INVISIBLE);

                pointerVisible = !pointerVisible;

            }

            else{
                gazePointer.setVisibility(View.VISIBLE);
                coordinateReceiverTask = new CoordinateReceiverTask(sPORT, gazePointer);
                coordinateReceiverTask.execute();

                Snackbar.make(v, "Gaze Tracking Activation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                fabGaze.setImageResource(R.drawable.touch_fb_icon);

                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setVisibility(View.VISIBLE);

                pointerVisible = !pointerVisible;

            }
        }

        else if (v.getId()==R.id.fabSetting){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("IP Address").setMessage(EconUtils.getLocalIpAddress()+":"+sPORT).setPositiveButton("확인", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    dialog.cancel();
                }
            }).show();
        }

    }

    private void fabViewAni(){
        if(fabState){
            fabGaze.startAnimation(fabClose);
            fabSetting.startAnimation(fabClose);

            fabGaze.setClickable(false);
            fabSetting.setClickable(false);

            fabMain.setImageResource(R.drawable.open_fb_icon);

        }
        else{
            fabGaze.startAnimation(fabOpen);
            fabSetting.startAnimation(fabOpen);

            fabGaze.setClickable(true);
            fabSetting.setClickable(true);

            fabMain.setImageResource(R.drawable.close_fb_icon);
        }
        fabState = !fabState;
    }



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }





}
