package POSCO_AI.e_con;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.LinearLayout;
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
    private final String instaUrl ="https://www.instagram.com/?hl=ko";
    private final String ytmUrl = "https://music.youtube.com/";
    private final String nmapUrl = "https://m.map.naver.com/";
    private static final int REQUEST_CAMERA = 1;

    String serverIP = "192.168.1.83";
    int serverPORT = 8200;

    private WebView webView;
    private ImageView gazePointer,emotionView;
    private FloatingActionButton fabMain,fabGaze,fabSetting;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Animation fabOpen,fabClose;

    private boolean pointerVisible ;
    private boolean socketUsage;
    private boolean fabState;

    private int WIDTH,HEIGHT;

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
        //        ActionBar ab = getSupportActionBar() ;
        permissionValidation();

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        WIDTH = dm.widthPixels;
        HEIGHT = dm.heightPixels;

        initView();
        setWeb(webView);
        initVariables();
        cameraViewInit();


    }

    public void permissionValidation(){
        int permissionCheck  = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if(permissionCheck==PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA);
    }

    private void initView(){
        webView = findViewById(R.id.webView);
        gazePointer = findViewById(R.id.gazePointer);
        emotionView = findViewById(R.id.emotionIcon);
        fabMain = findViewById(R.id.fabMain);
        fabGaze = findViewById(R.id.fabGaze);
        fabSetting = findViewById(R.id.fabSetting);
        mOpenCvCameraView = findViewById(R.id.activity_surface_view);
    }

    private void initVariables() {

        serverIP  = EconUtils.getLocalIpAddress();
        pointerVisible = false;
        socketUsage = true;
        fabState =true;

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        fabViewAni();

        cameraProcessor = new CameraProcessor(gazePointer,serverIP,serverPORT,webView,emotionView,WIDTH,HEIGHT);

    }

    private void cameraViewInit(){
        mOpenCvCameraView.setCvCameraViewListener(cameraProcessor);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setVisibility(View.INVISIBLE);
    }


    private void setWeb(WebView webView){
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
//        webView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                float tmpx = event.getX();
//                float tmpy = event.getY();
//                Toast.makeText(MainActivity.this, "Coordi :"+ tmpx+"/"+tmpy, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
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

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText ipEdit = new EditText(this);
            final EditText portEdit = new EditText(this);
            ipEdit.setText(serverIP);
            portEdit.setText(""+serverPORT);
            layout.addView(ipEdit);
            layout.addView(portEdit);
            builder.setTitle("IP Address Setting").setView(layout).setPositiveButton("확인", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){

                    serverIP = ipEdit.getText().toString();
                    serverPORT =Integer.parseInt( portEdit.getText().toString());

                    cameraProcessor.setServerIP(serverIP);
                    cameraProcessor.setServerPORT(serverPORT);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.insta_ac:
                webView.loadUrl(instaUrl);
                return true;
            case R.id.ytmusic_ac:
                webView.loadUrl(ytmUrl);
                return true;
            case R.id.nmap_ac:
                webView.loadUrl(nmapUrl);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
