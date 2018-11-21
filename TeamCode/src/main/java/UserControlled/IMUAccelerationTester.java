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

package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Autonomous.Location;
import DriveEngine.HolonomicDriveSystemTesting;
import DriveEngine.JennyNavigation;

@TeleOp(name="Accelerometer Tester", group="Linear Opmode")
//@Disabled
public class IMUAccelerationTester extends LinearOpMode {
    JennyNavigation navigation;
    JoystickHandler leftStick, rightStick;
    double[] accelerations = new double[3];

    @Override
    public void runOpMode() {
        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0, "RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);

        double movementPower = 0;
        double turningPower = 0;
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            accelerations = navigation.orientation.getAccelerations();
            movementPower = 1 * leftStick.magnitude();
            turningPower = 0.5 * (Math.abs(rightStick.magnitude())) * Math.abs(rightStick.x())/rightStick.x();
            navigation.relativeDriveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);

            telemetry.addData("X Accel", accelerations[0]);
            telemetry.addData("Y Accel", accelerations[1]);
            telemetry.addData("Z Accel", accelerations[2]);
            telemetry.addData("IMU Location", navigation.orientation.getLocation());
            telemetry.addData("Robot Location", navigation.getRobotLocation());
            telemetry.update();
        }
        navigation.stopNavigation();
    }
}
