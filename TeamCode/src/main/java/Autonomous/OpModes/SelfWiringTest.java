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

import java.io.InputStream;

import Actions.LatchSystem;
import Actions.MineralSystemV3;
import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;
import MotorControllers.JsonConfigReader;

import static DriveEngine.JennyNavigation.BACK_LEFT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR;

@Autonomous(name = "Self Wiring Test", group = "Concept")
//@Disabled
public class SelfWiringTest extends LinearOpMode {
    JennyNavigation navigation;
    MineralSystemV3 mineralSystem;
    LatchSystem latchSystem;
    VisionHelper robotVision;
    int driveMotorCount[] = {0, 0, 0, 0};
    int liftMotorCount = 0, extensionMotorCount = 0;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(hardwareMap);
        mineralSystem = new MineralSystemV3(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 45, "RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();

        checkDriveMotors();
        checkMineralSystem();
        //TODO: add telemetry, latch system check, and camera check


        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
        latchSystem.kill();
        robotVision.kill();
    }

    private void checkDriveMotors() {
        navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        long tick = navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        if(Math.abs(tick) > 30) driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]++;

        navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;

        navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]++;

        navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;

        if(driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] == 2) {

        }
        if(driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 2) {

        }
        if(driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] == 2) {

        }
        if(driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 2) {

        }
    }

    private void checkMineralSystem() {
        mineralSystem.lift();
        sleep(200);
        mineralSystem.pauseLift();
        long tick = mineralSystem.liftMotor.getCurrentTick();
        if(Math.abs(tick) > 30) liftMotorCount++;
        mineralSystem.lower();
        sleep(200);
        mineralSystem.pauseLift();
        if(Math.abs(mineralSystem.liftMotor.getCurrentTick() - tick) > 30) liftMotorCount++;

        mineralSystem.extendIntake();
        sleep(200);
        mineralSystem.pauseExtension();
        tick = mineralSystem.extensionMotor.getPosition();
        if(Math.abs(tick) > 30) extensionMotorCount++;
        mineralSystem.retractIntake();
        sleep(200);
        mineralSystem.pauseExtension();
        if(Math.abs(mineralSystem.extensionMotor.getPosition() - tick) > 30) extensionMotorCount++;

        mineralSystem.intake();
        sleep(500);
        mineralSystem.expel();
        sleep(500);
        mineralSystem.pauseCollection();
    }
}