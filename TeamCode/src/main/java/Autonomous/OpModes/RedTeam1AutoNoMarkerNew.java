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

import Actions.HardwareWrappers.ServoHandler;
import Actions.LatchSystem;
import Actions.LatchSystemV4;
import Actions.MineralSystemV3;
import Actions.MineralSystemV4;
import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

import static Autonomous.VisionHelper.BOTH;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.LOCATION;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.PHONE_CAMERA;
import static Autonomous.VisionHelper.RIGHT;
import static Autonomous.VisionHelper.WEBCAM;

@Autonomous(name = "Red Team Crater Side", group = "Concept")
//@Disabled
public class RedTeam1AutoNoMarkerNew extends LinearOpMode {
    JennyNavigation navigation;
    LatchSystemV4 latchSystem;
    MineralSystemV4 mineralSystem;
    ServoHandler markerDeployer;

    private VisionHelper webcam;
    private VisionHelper camera;

    @Override
    public void runOpMode() {
        webcam = new VisionHelper(WEBCAM, hardwareMap);
        latchSystem = new LatchSystemV4(hardwareMap);
        mineralSystem = new MineralSystemV4(hardwareMap);
        markerDeployer = new ServoHandler("markerDeployer", hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 45, "RobotConfig/RosannaV4.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        webcam.startTrackingLocation();
        webcam.startGoldDetection();
        markerDeployer.setDegree(0.0);

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
//        sleep(50);
        navigation.driveOnHeading(90, 1);
        long startTime = System.currentTimeMillis();
        while(opModeIsActive() && latchSystem.winchMotor.getCurrentTick() < LatchSystemV4.UNHOOK_POSITION && System.currentTimeMillis() - startTime < 2000) latchSystem.extend();
        latchSystem.pause();
        latchSystem.coastLatchMotor();
//        navigation.brake();
        webcam.startDetection();
        //delatch
        navigation.driveDistance(2, 180, 10, this);
        navigation.driveDistance(1, 90, 10, this);
//        navigation.driveDistance(1, 0, 10, this);


        //find gold & sample
//        navigation.turnToHeading(135, this);
        sleep(100);
        int goldPosition = -1;
        if(opModeIsActive()) goldPosition = findGold();
        idle();
        if(opModeIsActive()) knockGoldV4(goldPosition);

        // turn to face image
        navigation.turnToHeading(345, this);
        webcam.startTrackingLocation();

        // drive to image
        if(goldPosition == LEFT) {
            navigation.driveDistance(18, 45, 25, this);
        } else if(goldPosition == RIGHT){
            navigation.driveDistance(36, 85, 25, this);
//            navigation.turnToHeading(10, 5, this);
        } else {
            navigation.driveDistance(32, 85, 25, this);
//            navigation.turnToHeading(10, 5, this);
//            navigation.driveDistance(4, 0, 15, this);
        }

        // determine path to depot
        Location[] path = new Location[2];
        Location robotLoc = webcam.getRobotLocation();
        while (opModeIsActive() && robotLoc == null) robotLoc = webcam.getRobotLocation();
        navigation.setLocation(robotLoc);
        telemetry.addData("Location", robotLoc.toString());
        telemetry.update();
        navigation.turnToHeading(180, 5, this);
        idle();
        path[0] = new Location(138, navigation.getRobotLocation().getY(), 180);
        path[1] = new Location(138, 116, 180);

        navigation.navigatePath(path, 15, this);
        // MARKER
        markerDeployer.setDegree(90.0);
        navigation.driveDistance(3, 270, 15, this);
        navigation.driveDistance(40, 0, 60, this);

        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.liftMotor.setPositionTicks(-2700);
        mineralSystem.liftMotor.setMotorPower(1);
        sleep(750);

        // park
        mineralSystem.extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.extensionMotor.setPostitionTicks(5600);
        mineralSystem.extensionMotor.setPower(1);

        while (opModeIsActive()) {
            telemetry.addData("Final location", navigation.getRobotLocation().toString());
            telemetry.addData("Status", "Waiting to end...");
            telemetry.update();
        }
        navigation.stopNavigation();
        latchSystem.kill();
    }

    private int findGold() {
//        webcam.startDetection();
//        webcam.startGoldDetection();
        int goldPosition = NOT_DETECTED;
        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && goldPosition == NOT_DETECTED && System.currentTimeMillis() - startTime < 5000) goldPosition = webcam.getGoldMineralPosition();
        webcam.stopGoldDetection();
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
        navigation.driveDistance(3.5, 90, 25, this);
        sleep(10);
        if (goldPosition == LEFT) {
            Log.d("Mineral", "LEFT");
            telemetry.addData("driving...", "left");
            telemetry.update();
            navigation.driveDistance(20, 45, 25, this);
        } else if (goldPosition == RIGHT) {
            Log.d("Mineral", "RIGHT");
            telemetry.addData("driving...", "right");
            telemetry.update();
            navigation.driveDistance(20, 135, 25, this);
            sleep(10);
            navigation.driveDistance(20, 315, 15, this);
        } else {
            Log.d("Mineral", "CENTER");
            telemetry.addData("driving...", "forward");
            telemetry.update();
            navigation.driveDistance(15, 90, 25, this);
            sleep(10);
            navigation.driveDistance(10, 270, 15, this);
        }
        idle();
    }

    private void knockGoldV4(int goldPosition) {
        navigation.driveDistance(3.5, 90, 25, this);
        sleep(10);
        if (goldPosition == LEFT) { // TODO: LEFT LEFT LEFT LEFT LEFT
            Log.d("Mineral", "LEFT");
            telemetry.addData("driving...", "left");
            telemetry.update();
            navigation.driveDistance(20, 45, 25, this);
        } else if (goldPosition == RIGHT) { // TODO: RIGHT RIGHT RIGHT RIGHT RIGHT
            Log.d("Mineral", "RIGHT");
            telemetry.addData("driving...", "right");
            telemetry.update();
            navigation.driveDistance(25, 135, 25, this);
            sleep(10);
            navigation.driveDistance(23, 315, 15, this);
        } else { // TODO: CENTER CENTER CENTER CENTER CENTER
            Log.d("Mineral", "CENTER");
            telemetry.addData("driving...", "forward");
            telemetry.update();
            navigation.driveDistance(23, 90, 20, this);
            sleep(10);
            navigation.driveDistance(22, 270, 15, this);
        }
        idle();
    }
}
