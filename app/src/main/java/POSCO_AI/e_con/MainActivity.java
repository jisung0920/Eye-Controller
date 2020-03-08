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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

import POSCO_AI.e_con.threadClass.CoordinateReceiverTask;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat matInput;
    private Mat matResult;

    WebView webView;
    ImageView gazePointer;
    FloatingActionButton fabMain,fabGaze,fabSetting;
    Animation fabOpen,fabClose;

    static int sPORT = 8000;

    boolean pointerVisible ;
    boolean socketUsage;
    boolean fabState;

    CoordinateReceiverTask coordinateReceiverTask;
    private static final int REQUEST_CAMERA = 1;

    final String startUrl = "https://www.autodraw.com/";

    final static String TAG = "Open";

    private CameraBridgeViewBase mOpenCvCameraView;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);


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


        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1);


    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_CAMERA:
//                for (int i = 0; i < permissions.length; i++) {
//                    String permission = permissions[i];
//                    int grantResult = grantResults[i];
//                    if (permission.equals(Manifest.permission.CAMERA)) {
//                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
//
//                        } else {
//                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
//                            finish();
//                        }
//                    }
//                }
//                break;
//        }
//    }

    private void initView(){
        webView = findViewById(R.id.webView);
        gazePointer = findViewById(R.id.gazePointer);
        fabMain = findViewById(R.id.fabMain);
        fabGaze = findViewById(R.id.fabGaze);
        fabSetting = findViewById(R.id.fabSetting);

    }

    private void initVariables(){

        pointerVisible = false;
        socketUsage = true;
        fabState =true;

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        fabViewAni();



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
                pointerVisible = !pointerVisible;
                fabGaze.setImageResource(R.drawable.eye_fb_icon);


            }

            else{
                gazePointer.setVisibility(View.VISIBLE);
                coordinateReceiverTask = new CoordinateReceiverTask(sPORT, gazePointer);
                coordinateReceiverTask.execute();
                Snackbar.make(v, "Gaze Tracking Activation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                pointerVisible = !pointerVisible;

                fabGaze.setImageResource(R.drawable.touch_fb_icon);

                onCameraPermissionGranted();
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


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();

//        if ( matResult == null )
//            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
//        ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
        matResult =  matInput.t();

        Log.d("check","here");
        return matResult;
    }
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission) {
//            onCameraPermissionGranted();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        }else{
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

}
