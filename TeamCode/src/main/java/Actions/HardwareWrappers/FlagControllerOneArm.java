package Actions.HardwareWrappers;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by robotics on 1/5/18.
 */

/*
    A class to control the robots
 */
public class FlagControllerOneArm extends Thread{
    ServoHandler flagWaver;
    public static final int[] FLAG_WAVER_MIN_MAX_DEG = {10,120};
    public final long PERIOD = 500;
    volatile boolean shouldRun = true;
    volatile boolean flagShouldMove = false;
    HardwareMap hardwareMap;

    public FlagControllerOneArm(HardwareMap hw){
        hardwareMap = hw;
        flagWaver = new ServoHandler("flagWaver", hardwareMap);
        flagWaver.setServoRanges(FLAG_WAVER_MIN_MAX_DEG[0]-1, FLAG_WAVER_MIN_MAX_DEG[1]+1);
        flagWaver.setPosition(FLAG_WAVER_MIN_MAX_DEG[0]);
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
        Log.d("Waver deg","" + waver);
        flagWaver.setDegree(waver);
    }



    public void setFlagWaverPosition(double positionInDeg){
        flagWaver.setDegree(positionInDeg);
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
