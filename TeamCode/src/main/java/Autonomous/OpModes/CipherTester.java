package Autonomous.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;

import Autonomous.VuforiaHelper;

/**
 * Created by robotics on 1/10/18.
 */
@Autonomous(name = "Cipher Tester", group = "visual autonomous")
@Disabled
public class CipherTester extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        VuforiaHelper vuforia = new VuforiaHelper();
        vuforia.loadCipherAssets();
        waitForStart();
        while(opModeIsActive()){
            RelicRecoveryVuMark mark = vuforia.getMark();
            switch (mark){
                case LEFT:
                    telemetry.addData("VuMark","LEFT");
                    break;
                case RIGHT:
                    telemetry.addData("VuMark","RIGHT");
                    break;
                case CENTER:
                    telemetry.addData("VuMark","CENTER");
                    break;
                case UNKNOWN:
                    telemetry.addData("VuMark","UNKOWN");
                    break;
                default:
                    telemetry.addData("VuMark","HUH");
                    break;
            }
            telemetry.update();
        }

    }
}
