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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;
import UserControlled.JoystickHandler;

@Autonomous(name="Navigation tests", group ="Testers")
@Disabled
public class NavigationTests extends LinearOpMode {
    JennyNavigation navigation;
    JoystickHandler rightStick, leftStick;

    @Override public void runOpMode() {
        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 45, "RobotConfig/RosannaV4.json");
        } catch (Exception e) {
            Log.e("Navigation error", e.toString());
            e.printStackTrace();
        }
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        double radius = 12;

        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();

        Location[] path = {new Location(12, 12, 90), new Location(0, 24, 270), new Location(36, 48, 0)};

        // DRIVE ON HEADING
//        navigation.driveOnHeading(0, 15);

        // DRIVE TO LOCATION
//        for(int j = 0; j < 4; j++) {
//            for (int i = 0; i <= 360; i += 30) {
//                if(opModeIsActive()) navigation.driveToLocation(new Location(radius * Math.cos(Math.toRadians(i)), radius * Math.sin(Math.toRadians(i))), 15, this);
//            }
//        }

//        navigation.driveToLocation(path[0], 15, this);
//        navigation.navigatePath(path, 15, this);
//        navigation.turnController.setSp(90);
//        while (opModeIsActive()) {
//            navigation.correctedDriveOnHeadingIMURotation(0, 15, 0, this);
//            telemetry.addData("Robot Location", navigation.getRobotLocation().toString());
//            telemetry.addData("Robot orientation", navigation.getOrientation());
//            telemetry.addData("First angle", navigation.orientation.getFirstAngle());
//            telemetry.update();
//        }

        // DRIVE DISTANCE
//        for(int i = 0; i < 360; i += 30) {
//            navigation.driveDistance(12, i, 15, this);
//            sleep(3000);
//        }
//        navigation.driveDistance(24, 45, 15, this);
//        sleep(10);
//        navigation.driveDistance(24, 180, 15, this);

//        while (opModeIsActive()) {
//            double heading = leftStick.angle();
//            if(leftStick.x() == 0 && leftStick.y() == 0) heading = navigation.getOrientation();
//            double speed = 15 * gamepad1.right_trigger;
//            while (opModeIsActive() && heading < navigation.getOrientation() - 180) heading+=360;
//            while (opModeIsActive() && heading > navigation.getOrientation() + 180) heading-=360;
//            navigation.turnController.setSp(heading);
//            navigation.driveOnHeadingPID(180, speed, 0,this);
//            telemetry.addData("Angle", heading);
//            telemetry.addData("Speed", speed);
//            telemetry.update();
//        }

        // DRIVE ON HEADING PID
//        navigation.turnController.setSp(navigation.getOrientation());
//        while (opModeIsActive()) navigation.driveOnHeadingPID(0, 15, 0, this);

//        navigation.turnToHeading(180, this);


        while (opModeIsActive()){
            telemetry.addData("Robot Location", navigation.getRobotLocation().toString());
            telemetry.addData("Robot orientation", navigation.getOrientation());
            telemetry.addData("First angle", navigation.orientation.getFirstAngle());
            telemetry.update();
        }
        navigation.stopNavigation();
    }
}
