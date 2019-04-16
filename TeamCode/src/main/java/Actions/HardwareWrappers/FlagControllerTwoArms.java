package Actions.HardwareWrappers;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by robotics on 1/5/18.
 */

/*
    A class to control the robots
 */
public class FlagControllerTwoArms extends Thread{
    ServoHandler[] flagWavers = new ServoHandler[2];
    public static final int[] FLAG_WAVER_MIN_MAX_DEG = {25,120};
    public static final int[] FLAG_TWISTER_MIN_MAX_DEG = {20,160};
    public final long PERIOD = 500;
    volatile boolean shouldRun = true;
    volatile boolean flagShouldMove = false;
    HardwareMap hardwareMap;

    public FlagControllerTwoArms(HardwareMap hw){
        hardwareMap = hw;
        flagWavers[0] = new ServoHandler("flagWaver", hardwareMap);
        flagWavers[0].setServoRanges(FLAG_WAVER_MIN_MAX_DEG[0]-1, FLAG_WAVER_MIN_MAX_DEG[1]+1);
        flagWavers[0].setDegree(FLAG_WAVER_MIN_MAX_DEG[0]);
        flagWavers[1] = new ServoHandler("flagTwister", hardwareMap);
        flagWavers[1].setServoRanges(FLAG_TWISTER_MIN_MAX_DEG[0]-1, FLAG_TWISTER_MIN_MAX_DEG[1]+1);
        flagWavers[1].setDegree(FLAG_TWISTER_MIN_MAX_DEG[0]);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(shouldRun){
                    if(flagShouldMove) moveFlag();
                }
            }
        }).start();
    }

    private void moveFlag(){
        double waver = FLAG_WAVER_MIN_MAX_DEG[0] + (FLAG_WAVER_MIN_MAX_DEG[1] - FLAG_WAVER_MIN_MAX_DEG[0])*(.5+.5*Math.cos((double)System.currentTimeMillis()/(double)PERIOD));
        double twist = FLAG_TWISTER_MIN_MAX_DEG[0] + (FLAG_TWISTER_MIN_MAX_DEG[1] - FLAG_TWISTER_MIN_MAX_DEG[0])*(.5+.5*Math.sin((double)System.currentTimeMillis()/(double)PERIOD));
        Log.d("Waver deg","" + waver);
        flagWavers[0].setDegree(waver);
        flagWavers[1].setDegree(twist);
    }



    public void setFlagWaverPosition(double positionInDeg){
        flagWavers[0].setDegree(positionInDeg);
    }

    public void setFlagTwisterPosition(double positionInDeg){
        flagWavers[1].setDegree(positionInDeg);
    }

    public void startFlag(){
        flagShouldMove = true;
    }

    public void pauseFlag() {
        flagShouldMove = false;
    }

    public void stopFlag(){
        flagShouldMove = false;
    }

    public void killFlag(){
        shouldRun = false;
    }
}
