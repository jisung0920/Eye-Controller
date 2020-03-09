package POSCO_AI.e_con;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class CameraProcessor implements CameraBridgeViewBase.CvCameraViewListener2 {


    private Mat matInput;
    private Mat matResult;


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matResult =  inputFrame.rgba().t();
        return matResult;
    }
}
