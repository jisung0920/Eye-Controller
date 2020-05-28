package POSCO_AI.e_con;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import POSCO_AI.e_con.threadClass.CameraProcessor;

public class MainActivity extends AppCompatActivity{

    private final String startUrl = "https://www.youtube.com/";
    private static final int REQUEST_CAMERA = 1;

    String serverIP = "192.168.43.89";
    int serverPORT = 8400;

    private WebView webView;
    private ImageView gazePointer;
    private FloatingActionButton fabMain,fabGaze,fabSetting;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Animation fabOpen,fabClose;

    private boolean pointerVisible ;
    private boolean socketUsage;
    private boolean fabState;

    private CameraProcessor cameraProcessor;



    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissionValidation();

        initView();
        initVariables();
        cameraViewInit();
        setWeb(webView);


    }

    public void permissionValidation(){
        int permissionCheck  = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if(permissionCheck==PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA);
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

        cameraProcessor = new CameraProcessor(gazePointer,serverIP,serverPORT);

    }

    private void cameraViewInit(){
        mOpenCvCameraView.setCvCameraViewListener(cameraProcessor);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCameraPermissionGranted();
//        mOpenCvCameraView.setMaxFrameSize(resolutionWidth ,resolutionHeight);
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setVisibility(View.INVISIBLE);
    }


    private void setWeb(WebView webView){
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
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
                Snackbar.make(v, "Gaze Tracking Deactivation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setVisibility(View.GONE);
                cameraProcessor.onPostExecute(true);

                fabGaze.setImageResource(R.drawable.eye_fb_icon);
                pointerVisible = !pointerVisible;

            }

            else{
                gazePointer.setVisibility(View.VISIBLE);

                Snackbar.make(v, "Gaze Tracking Activation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setVisibility(View.VISIBLE);
//                cameraProcessor.execute(IP,PORT+"");

                fabGaze.setImageResource(R.drawable.touch_fb_icon);
                pointerVisible = !pointerVisible;


            }
        }

        else if (v.getId()==R.id.fabSetting){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText portEdit = new EditText(this);
            portEdit.setText(""+serverPORT);
            builder.setTitle("IP Address").setMessage(EconUtils.getLocalIpAddress()).setView(portEdit).setPositiveButton("확인", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    serverPORT =Integer.parseInt( portEdit.getText().toString());

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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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



}
