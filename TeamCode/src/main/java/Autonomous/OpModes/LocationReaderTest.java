/* Copyright (c) 2017 FIRST. All rights reserved.
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

import java.io.InputStream;

import DriveEngine.HolonomicDriveSystemTesting;
import MotorControllers.JsonConfigReader;
import Autonomous.*;
import UserControlled.JoystickHandler;

import static Autonomous.VuforiaHelper.PHONE_CAMERA;

@Autonomous(name="Location Reader Test", group="Linear Opmode")
//@Disabled
public class LocationReaderTest extends LinearOpMode {
    // create objects and locally global variables here
    JsonConfigReader reader;
    VisionHelper robotVision;
    HolonomicDriveSystemTesting navigation;
    JoystickHandler leftStick, rightStick;

    // TODO: test navigatePath with getPath

    @Override
    public void runOpMode() {
        InputStream stream;
        try {
            stream = hardwareMap.appContext.getAssets().open("FieldConfig/BlueLocations.json");
            navigation = new HolonomicDriveSystemTesting(hardwareMap, "RobotConfig/RosannaV4.json");
        }
        catch(Exception e){
            Log.d("Drive Engine Error: ",e.toString());
            throw new RuntimeException("Drive Engine Open Config File Fail: " + e.toString());
        }
        robotVision = new VisionHelper(PHONE_CAMERA, hardwareMap);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        // initialize objects and variables here
        // also create and initialize function local variables here
        reader = new JsonConfigReader(stream);
        try {
            Location first = reader.getLocation("BLUE_DEPOT");
            telemetry.addData("Location", first.toString());
            Location[] path = reader.getPath("AUTO_PATH_1");
            telemetry.addData("Path length", path.length);
            for(int i = 0; i < path.length; i++) {
                telemetry.addData("Path Location " + i, path[i].toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        robotVision.startTrackingLocation();
        robotVision.startDetection();
        // add any other useful telemetry data or logging data here
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // nothing goes between the above and below lines
        waitForStart();
        // should only be used for a time keeper or other small things, avoid using this space when possible
        while (opModeIsActive()) {
            handleDriving();
            Location loc = robotVision.getRobotLocation();
            telemetry.addData("Location", (loc == null)? "null":loc.toString());
            telemetry.update();
        }
        navigation.kill();
        // disable/kill/stop objects here
    }

    private void handleDriving() {
        double movementPower = 1 * Math.abs(leftStick.magnitude());
        double turningPower = .75 * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
        navigation.driveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);
        telemetry.addData("Joystick angle", leftStick.angle());
    }
    // misc functions here
}
