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

import Actions.LatchSystem;
import Actions.MineralSystemV3;
import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static Autonomous.VuforiaHelper.PHONE_CAMERA;

@Autonomous(name = "Red Team Crater Side", group = "Concept")
//@Disabled
public class BlueRedTeam1AutoNoMarkerNew extends LinearOpMode {
    JennyNavigation navigation;
    LatchSystem latchSystem;
    MineralSystemV3 mineralSystem;

    private VisionHelper webcam;
    private VisionHelper camera;

    @Override
    public void runOpMode() {
        webcam = new VisionHelper(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
        mineralSystem = new MineralSystemV3(hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 45, "RobotConfig/RosannaV4.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        webcam.startTrackingLocation();
        webcam.startGoldDetection();

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();
        // START AUTONOMOUS
//        sleep(50);
//        navigation.driveOnHeading(90, 1);
//        while(opModeIsActive() && (!latchSystem.limitSwitches[LatchSystem.EXTEND_SWITCH].isPressed() || latchSystem.winchMotor.getCurrentTick() < latchSystem.UNHOOK_POSITION)) latchSystem.extend();
//        latchSystem.pause();
//        navigation.brake();
        webcam.startDetection();
        sleep(50);
        //delatch
        navigation.driveDistance(1.5, 180, 10, this);
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
        navigation.turnToHeading(165, this);
//        webcam.startTrackingLocation();
        camera = new VisionHelper(PHONE_CAMERA, hardwareMap);
        camera.startTrackingLocation();
        camera.startDetection();

        // drive to image
        if(goldPosition == LEFT) {
            navigation.driveDistance(18, 225, 25, this);
        } else if(goldPosition == RIGHT){
            navigation.driveDistance(36, 265, 25, this);
            navigation.turnToHeading(190, 5, this);
        } else {
            navigation.driveDistance(32, 265, 25, this);
            navigation.turnToHeading(190, 5, this);
//            navigation.driveDistance(4, 0, 15, this);
        }
        // determine path to depot
        Location[] path = new Location[2];
        Location robotLoc = camera.getRobotLocation();
        while (opModeIsActive() && robotLoc == null) robotLoc = camera.getRobotLocation();
        camera.kill();
        navigation.setLocation(robotLoc);
        telemetry.addData("Location", robotLoc.toString());
        telemetry.update();
        path[0] = new Location(136, navigation.getRobotLocation().getY());
        path[1] = new Location(136, 116);

        navigation.navigatePath(path, 15, this);
        navigation.driveDistance(2, 90, 15, this);
        navigation.driveDistance(53, 180, 60, this);
//        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        mineralSystem.liftMotor.setPositionTicks(3500);
//        mineralSystem.liftMotor.setMotorPower(1);
//        sleep(750);
//
//        // park
//        mineralSystem.extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        mineralSystem.extensionMotor.setPostitionTicks(6000);
//        mineralSystem.extensionMotor.setPower(1);

        /**
         * FULL AUTO BELOW, begins after finding gold
         */
        // drive to left mineral position
//        navigation.driveDistance(4, -45, 15, this);
//        idle();
//        if(goldPosition == RIGHT) navigation.driveDistance(24, 45, 15, this);
//        else if(goldPosition == CENTER) navigation.driveDistance(12, 45, 15, this);
//
//
//        navigation.turnToHeading(-30, this);
//        webcam.startDetection();
//        sleep(50);
//        Location robotLocation = null;
//        while (opModeIsActive() && robotLocation == null) {
//            robotLocation = webcam.getRobotLocation();
//            telemetry.addData("Location", robotLocation);
//            telemetry.update();
//        }
//        navigation.setLocation(robotLocation);
//        idle();
//
//        navigation.turnToHeading(0, this);
//        idle();
//        navigation.driveToLocation(new Location(134.0, 72.0), 20, this); // position to wall
//        navigation.turnToHeading(0, this);
//        idle();
//
//        navigation.driveToLocation(new Location(134.0, 100.0), 25, this); // fast to depot
//        idle();
//
//        navigation.driveToLocation(new Location(134.0, 122.0), 15, this); // position to depot
//        idle();
//
//
//        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        mineralSystem.liftMotor.setPositionTicks(3100);
//        mineralSystem.liftMotor.setMotorPower(1);
//        sleep(500);
//        navigation.driveDistance(6, 180, 25, this);
//
//        navigation.turnToHeading(180, this);
//        idle();
//
//        navigation.driveToLocation(new Location(navigation.getRobotLocation().getX(), 72.0), 25, this); // fast to crater
//        idle();
//
//        navigation.driveToLocation(new Location(navigation.getRobotLocation().getX(), 66.0), 15, this); // position to crater
//        idle();
//
//        mineralSystem.extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        mineralSystem.extensionMotor.setPostitionTicks(2500);
//        mineralSystem.extensionMotor.setPower(1);
//
//        navigation.turnToHeading(170, this);


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
        webcam.kill();
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
