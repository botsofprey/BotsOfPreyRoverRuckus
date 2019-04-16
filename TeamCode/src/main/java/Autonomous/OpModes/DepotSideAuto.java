/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package Autonomous.OpModes;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import Actions.HardwareWrappers.ServoHandler;
import Actions.LatchSystemV4;
import Actions.MineralSystemV4;
import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

import static Autonomous.VisionHelper.CENTER;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static Autonomous.VisionHelper.WEBCAM;

@Autonomous(name = "Depot Side", group = "Autonomous")
//@Disabled
public class DepotSideAuto extends LinearOpMode {
    JennyNavigation navigation;
    LatchSystemV4 latchSystem;
    MineralSystemV4 mineralSystem;
    ServoHandler markerDeployer;

    private VisionHelper webcam;

    @Override
    public void runOpMode() {
        webcam = new VisionHelper(WEBCAM, hardwareMap);
        latchSystem = new LatchSystemV4(hardwareMap);
        mineralSystem = new MineralSystemV4(hardwareMap);
        markerDeployer = new ServoHandler("markerDeployer", hardwareMap);
        markerDeployer.setDirection(Servo.Direction.REVERSE);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 315, "RobotConfig/RosannaV4.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        webcam.startTrackingLocation();
        webcam.startGoldDetection();
        markerDeployer.setDegree(30.0);

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();

        while (!opModeIsActive()) {
            latchSystem.pause();
        }
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();
        // START AUTONOMOUS
        sleep(50);
        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.liftMotor.setPositionTicks(-1100);
        mineralSystem.liftMotor.setMotorPower(1);
        navigation.driveOnHeading(90, 1);
        long startTime = System.currentTimeMillis();
        while(opModeIsActive() && latchSystem.winchMotor.getCurrentTick() < LatchSystemV4.UNHOOK_POSITION && System.currentTimeMillis() - startTime < 2000) latchSystem.extend();
        latchSystem.winchMotor.brake();
        latchSystem.coastLatchMotor();
        navigation.brake();

        //delatch
        navigation.driveDistance(2.5, 180, 15, this);
        navigation.driveDistance(2.5, 90, 15, this);


        //find gold & sample
        idle();
        int goldPosition = -1;
        webcam.startDetection();
        if(opModeIsActive()) goldPosition = findGold();
        sleep(100);
        if(opModeIsActive()) knockGold(goldPosition);

        // turn to face image
        navigation.turnToHeading(255, this);
        webcam.startTrackingLocation();

        // drive to image
        if(goldPosition == CENTER) {
            navigation.driveDistance(32, 82, 30, this);
        } else if(goldPosition == RIGHT){
            navigation.driveDistance(34, 82, 30, this);
        } else {
            navigation.driveDistance(16, 45, 30, this);
        }

        navigation.turnToHeading(270, 10, this);
        idle();

        navigation.driveDistance(13, 90, 20, this);
        if(goldPosition == LEFT) {
            navigation.driveDistance(26, 180, 40, this);
            navigation.driveDistance(13, 180, 15, this);
        } else if(goldPosition == CENTER) {
            navigation.driveDistance(24, 180, 40, this);
            navigation.driveDistance(13, 180, 15, this);
        } else {
            navigation.driveDistance(20, 180, 40, this);
            navigation.driveDistance(13, 180, 15, this);
        }

        // MARKER
        markerDeployer.setDegree(150.0);
        navigation.driveDistance(3, 90, 20, this);
        mineralSystem.liftMotor.brake();
        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.liftMotor.setPositionTicks(-2700); // -2700
        mineralSystem.liftMotor.setMotorPower(1);
        if(goldPosition == LEFT) {
            navigation.driveDistance(45, 0, 35, this);
        } else if(goldPosition == RIGHT) {
            navigation.driveDistance(45, 0, 35, this);
        } else {
            navigation.driveDistance(45, 0, 35, this);
        }

        // park
        mineralSystem.extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.extensionMotor.setPostitionTicks(5600);
        mineralSystem.extensionMotor.setPower(1);
        navigation.driveDistance(2, 270, 15, this);

        while (opModeIsActive()) {
            telemetry.addData("Final location", navigation.getRobotLocation().toString());
            telemetry.addData("Status", "Waiting to end...");
            telemetry.update();
        }
        navigation.stopNavigation();
        latchSystem.kill();
        mineralSystem.kill();
        webcam.kill();
    }

    private int findGold() {
//        webcam.startDetection();
//        webcam.startGoldDetection();
        navigation.turnToHeading(315, this);
        int goldPosition = NOT_DETECTED;
        long startTime = System.currentTimeMillis();
//        int turnCount = 0;
//        double rps = 10.0/360.0;
        while (opModeIsActive() && goldPosition == NOT_DETECTED && System.currentTimeMillis() - startTime < 5000) {
            goldPosition = webcam.getGoldMineralPosition();
//            navigation.turn(rps);
//            if((rps > 0 && navigation.getOrientation() > 50) || (rps < 0 && navigation.getOrientation() < 40)) rps = -rps;
        }
        goldPosition = webcam.getGoldMineralPosition();
        if(goldPosition == NOT_DETECTED) {
            goldPosition = webcam.getLargestPositionVote();
        }
        if(goldPosition == NOT_DETECTED) goldPosition = LEFT;
        webcam.stopGoldDetection();
        telemetry.addData("Left Count", webcam.getPositionVote(LEFT));
        telemetry.addData("Center Count", webcam.getPositionVote(CENTER));
        telemetry.addData("Right Count", webcam.getPositionVote(RIGHT));
//        goldPosition = LEFT;
//        long startTime = System.currentTimeMillis();
//        while (opModeIsActive() && goldPosition == NOT_DETECTED && System.currentTimeMillis() - startTime <= 25000) {
//            sleep(250);
//            if(webcam.getClosestMineral().getLabel().equals(LABEL_GOLD_MINERAL)) goldPosition = CENTER;
//            else {
//                navigation.turnToHeading(45 + 20,this);
//                sleep(500);
//                if(webcam.getClosestMineral().getLabel().equals(LABEL_GOLD_MINERAL)) goldPosition = RIGHT;
//                else goldPosition = LEFT;
//            }
//        }
//        navigation.turnToHeading(45, this);
//        idle();
        return goldPosition;
    }

    private void knockGold(int goldPosition) {
        navigation.driveDistance(2, 90, 25, this);
        sleep(10);
        if (goldPosition == CENTER) { // TODO: CENTER CENTER CENTER CENTER CENTER
            Log.d("Mineral", "CENTER");
            telemetry.addData("driving...", "forward");
            telemetry.update();
            navigation.driveDistance(24, 80, 20, this);
            sleep(10);
            navigation.driveDistance(23, 270, 15, this);
        } else if (goldPosition == RIGHT) { // TODO: RIGHT RIGHT RIGHT RIGHT RIGHT
            Log.d("Mineral", "RIGHT");
            telemetry.addData("driving...", "right");
            telemetry.update();
            navigation.driveDistance(26, 135, 25, this);
            sleep(10);
            navigation.driveDistance(28, 315, 15, this);
        } else { // TODO: LEFT LEFT LEFT LEFT LEFT
            Log.d("Mineral", "LEFT");
            telemetry.addData("driving...", "left");
            telemetry.update();
//            navigation.driveDistance(20, 45, 25, this);
            navigation.driveDistance(18, 10, 20, this);
            navigation.driveDistance(12, 90, 20, this);
            navigation.driveDistance(4, 270, 15, this);
        }
        idle();
    }
}
