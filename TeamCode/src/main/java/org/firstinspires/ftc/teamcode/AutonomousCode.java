package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;

import ExampleBaseCode.Autonomous.VuforiaHelper;

/**
 * Created by robotics on 1/10/18.
 */
@Autonomous(name = "Autonomous Code", group = "Autonomous")
@Disabled
public class AutonomousCode extends LinearOpMode {
    long startTime = 0;
    DcMotor leftMotor;
    DcMotor rightMotor;
    @Override
    public void runOpMode() throws InterruptedException {
        VuforiaHelper vuforia = new VuforiaHelper();
        vuforia.loadCipherAssets();
        waitForStart();
        leftMotor = hardwareMap.dcMotor.get("leftMotor");
        rightMotor = hardwareMap.dcMotor.get("rightMotor");
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        waitForStart();

        startTime = System.currentTimeMillis();
        while(opModeIsActive()){

//            while (opModeIsActive() && System.currentTimeMillis() - startTime >= 5000) {
//                leftMotor.setPower(1);
//                rightMotor.setPower(1);
//            }
//            leftMotor.setPower(0);
//            rightMotor.setPower(0);
            driveDistance(5, 0.5, 10000);

            telemetry.update();
        }

    }
    public void driveDistance(double distance, double power, long runTime) {
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        idle();
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        final int TICK_PER_REV = 1120;
        final double WHEEL_DIAMETER_INCHES = 4;
        double curDistance = 0;
        long startTime = System.currentTimeMillis();

        while(curDistance < distance && opModeIsActive() && System.currentTimeMillis() - startTime < runTime) {
            leftMotor.setPower(power);
            rightMotor.setPower(power);
            curDistance = (Math.PI*WHEEL_DIAMETER_INCHES*(leftMotor.getCurrentPosition()+rightMotor.getCurrentPosition()))/(2*TICK_PER_REV);
        }
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }

}
