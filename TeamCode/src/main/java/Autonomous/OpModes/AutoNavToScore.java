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

import Autonomous.*;
import DriveEngine.JennyNavigation;
import UserControlled.JoystickHandler;

@Autonomous(name="Auto Nav To Score", group ="Concept")
//@Disabled
public class AutoNavToScore extends LinearOpMode {
    final double movementScale = 1;
    double turningScale = .75;
    JennyNavigation navigation;
    JoystickHandler rightStick, leftStick;
    VisionHelper locationTracker;

    @Override public void runOpMode() {
        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0, "RobotConfig/RosannaV3.json");
        } catch (Exception e) {
            Log.e("Navigation error", e.toString());
            e.printStackTrace();
        }
        locationTracker = new VisionHelper(hardwareMap);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        double radius = 12;
        locationTracker.startTrackingLocation();
        locationTracker.startDetection();

        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();

        // DRIVE ON HEADING
//        navigation.driveOnHeading(0, 15);

        // DRIVE TO LOCATION
//        for(int j = 0; j < 4; j++) {
//            for (int i = 0; i <= 360; i += 30) {
//                if(opModeIsActive()) navigation.driveToLocation(new Location(radius * Math.cos(Math.toRadians(i)), radius * Math.sin(Math.toRadians(i))), 15, this);
//            }
//        }
//        navigation.driveToLocation(new Location(0, 0), 15, this);

        // DRIVE DISTANCE
//        for(int i = 0; i < 360; i += 30) {
//            navigation.driveDistance(12, i, 15, this);
//            sleep(3000);
//        }
//        navigation.driveDistance(24, 0, 15, this);
//        sleep(10);
//        navigation.driveDistance(24, 180, 15, this);

        while (opModeIsActive()) {
            handleDriving();
            if(gamepad1.x) {
                Location robotLoc = locationTracker.getRobotLocation();
                if(robotLoc != null) navigation.setLocation(robotLoc);
            } else if(gamepad1.y) {
                navigation.driveToLocation(new Location(52, 84), 20, this);
            }
            telemetry.update();
        }

        // DRIVE ON HEADING PID
//        navigation.turnController.setSp(navigation.getOrientation());
//        while (opModeIsActive()) navigation.driveOnHeadingPID(0, 15, 0, this);

        telemetry.addData("Robot Location", navigation.getRobotLocation().toString());
        telemetry.addData("Robot orientation", navigation.getOrientation());
        telemetry.addData("First angle", navigation.orientation.getFirstAngle());
        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
    }

    private void handleDriving() {
        double movementPower = movementScale * Math.abs(leftStick.magnitude());
        double turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
        navigation.driveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);
    }
}
