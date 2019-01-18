package Autonomous;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import Autonomous.OpModes.HolonomicSampleAndPark;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_SILVER_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.TFOD_MODEL_ASSET;

/**
 * Created by robotics on 12/18/18.
 */

public class TensorFlowHelper extends Thread {
    public final static int LEFT = 0, CENTER = 1, RIGHT = 2, NOT_DETECTED = -1;
    private final int POSITION_VOTE_MINIMUM_COUNT = 15;
    private volatile TFObjectDetector tfod;
    private volatile double[] positionVotes = {0, 0, 0};
    private volatile boolean running = true;

    public TensorFlowHelper(HardwareMap hardwareMap) {
        try {
            int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                    "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
            tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, VuforiaHelper.initVuforia());
            tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
            tfod.activate();
        } catch (Exception e) {
            Log.e("TensorFlowHelper Error", e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        if(tfod != null) {
            while (running) {
                updatePositionVotes();
            }
            resetPositionVotes();
        }
    }

    public void startDetection() {
        resetPositionVotes();
        running = true;
        this.start();
    }

    public void stopDetection() {
        running = false;
    }

    public int getGoldMineralPosition() {
        int position = NOT_DETECTED;
        if(positionVotes[LEFT] + positionVotes[CENTER] + positionVotes[RIGHT] >= POSITION_VOTE_MINIMUM_COUNT) {
            if(positionVotes[LEFT] > positionVotes[CENTER] && positionVotes[LEFT] > positionVotes[RIGHT]) {
                position = LEFT;
            } else if(positionVotes[RIGHT] > positionVotes[CENTER] && positionVotes[RIGHT] > positionVotes[LEFT]) {
                 position = RIGHT;
            } else {
                position = CENTER;
            }
        }
        return position;
    }

    public void resetPositionVotes() {
        for(int i = 0; i < positionVotes.length; i++) {
            positionVotes[i] = 0;
        }
    }

    private void updatePositionVotes() {
        List<Recognition> recognitions = tfod.getRecognitions();
        if(recognitions != null) {
            Recognition[] minerals = filterMineralsOnScreen(recognitions);
            if(minerals.length >= 3) {
                int goldMineralX = -1;
                int silverMineral1X = -1;
                int silverMineral2X = -1;
                for (Recognition recognition : minerals) {
                    if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                        goldMineralX = (int) recognition.getLeft();
                    } else if (silverMineral1X == -1) {
                        silverMineral1X = (int) recognition.getLeft();
                    } else {
                        silverMineral2X = (int) recognition.getLeft();
                    }
                }
                if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                    if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                        positionVotes[LEFT]++;
                    } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                        positionVotes[RIGHT]++;
                    } else {
                        positionVotes[CENTER]++;
                    }
                }
            }
        }
    }

    private Recognition[] filterMineralsOnScreen(List<Recognition> minerals) {
        Recognition[] mineralsArray = minerals.toArray(new Recognition[0]);
        Arrays.sort(mineralsArray, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition r1, Recognition r2) {
                return (int) (r1.getBottom() - r2.getBottom());
            }
        });
        return mineralsArray;
    }

    public void kill() {
        stopDetection();
        tfod.shutdown();
    }
}
