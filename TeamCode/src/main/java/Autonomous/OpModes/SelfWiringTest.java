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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import Actions.LatchSystem;
import Actions.MineralSystemV3;
import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

import static Actions.LatchSystem.EXTEND_SWITCH;
import static Actions.LatchSystem.RETRACT_SWITCH;
import static DriveEngine.JennyNavigation.BACK_LEFT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR;
import static DriveEngine.JennyNavigation.FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR;

@Autonomous(name = "Self Wiring Test", group = "Concept")
@Disabled
public class SelfWiringTest extends LinearOpMode {
    private JennyNavigation navigation;
    private MineralSystemV3 mineralSystem;
    private LatchSystem latchSystem;
    private VisionHelper robotVision;
    private int driveMotorCount[] = {0, 0, 0, 0};
    private int liftMotorCount = 0, extensionMotorCount = 0, winchMotorCount = 0;
    private boolean extendSwitchGood = false, retractSwitchGood = false, tflowDetectionGood = false, vuforiaNavigationGood = false;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(VisionHelper.WEBCAM, hardwareMap);
        mineralSystem = new MineralSystemV3(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0, "RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Wait for the game to begin */
        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();

        checkDriveMotors();
        checkMineralSystem();
        checkLatchSystem();
        checkCamera();

        reportRobotStatus();
        while (opModeIsActive());
        navigation.stopNavigation();
        latchSystem.kill();
        robotVision.kill();
    }

    private void checkDriveMotors() {
        telemetry.addData("Drive Motor", "Checking... Front Left");
        telemetry.update();
        navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        long tick = navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        if(Math.abs(tick) > 30) driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]++;

        telemetry.addData("Drive Motor", "Checking... Front Right");
        telemetry.update();
        navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;

        telemetry.addData("Drive Motor", "Checking... Back Left");
        telemetry.update();
        navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]++;

        telemetry.addData("Drive Motor", "Checking... Back Right");
        telemetry.update();
        navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick()) > 30) driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;
        navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(-5);
        sleep(200);
        navigation.brake();
        if(Math.abs(navigation.driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick() - tick) > 30) driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]++;
    }

    private void checkMineralSystem() {
        telemetry.addData("Mineral System", "Checking... Arm");
        telemetry.update();
        mineralSystem.lift();
        sleep(200);
        mineralSystem.pauseLift();
        long tick = mineralSystem.liftMotor.getCurrentTick();
        if(Math.abs(tick) > 30) liftMotorCount++;
        mineralSystem.lower();
        sleep(200);
        mineralSystem.pauseLift();
        if(Math.abs(mineralSystem.liftMotor.getCurrentTick() - tick) > 30) liftMotorCount++;

        telemetry.addData("Mineral System", "Checking... Extension");
        telemetry.update();
        mineralSystem.extendIntake();
        sleep(200);
        mineralSystem.pauseExtension();
        tick = mineralSystem.extensionMotor.getPosition();
        if(Math.abs(tick) > 30) extensionMotorCount++;
        mineralSystem.retractIntake();
        sleep(200);
        mineralSystem.pauseExtension();
        if(Math.abs(mineralSystem.extensionMotor.getPosition() - tick) > 30) extensionMotorCount++;

        telemetry.addData("Mineral System", "Checking... Intake");
        telemetry.update();
        mineralSystem.intake();
        sleep(500);
        mineralSystem.expel();
        sleep(500);
        mineralSystem.pauseCollection();

        // more???
    }

    private void checkLatchSystem() {
        telemetry.addData("Latch System", "Checking... Extend Switch");
        telemetry.addData("Extend Switch", "Press and hold the extend switch until told to stop");
        telemetry.update();
        sleep(1000);
        if(latchSystem.limitSwitches[EXTEND_SWITCH].isPressed()) extendSwitchGood = true;
        telemetry.addData("Extend Switch", "Release the extend switch");
        telemetry.update();
        sleep(1000);

        telemetry.addData("Latch System", "Checking... Retract Switch");
        telemetry.addData("Retract Switch", "Press and hold the retracts switch until told to stop");
        telemetry.update();
        sleep(1000);
        if(latchSystem.limitSwitches[RETRACT_SWITCH].isPressed()) retractSwitchGood = true;
        telemetry.addData("Retract Switch", "Release the retract switch");
        telemetry.update();
        sleep(1000);

        telemetry.addData("Latch System", "Checking... Winch Motor");
        telemetry.update();
        latchSystem.retractUnsafe();
        sleep(200);
        latchSystem.winchMotor.brake();
        long tick = latchSystem.winchMotor.getCurrentTick();
        if(Math.abs(tick) > 30) winchMotorCount++;
        latchSystem.extend();
        sleep(200);
        latchSystem.pause();
        if(Math.abs(latchSystem.winchMotor.getCurrentTick() - tick) > 30) winchMotorCount++;
    }

    private void checkCamera() {
        telemetry.addData("Robot Vision", "Checking... TensorFlow");
        telemetry.addData("TensorFlow", "Put gold and silver minerals into the camera view");
        telemetry.update();
        sleep(1000);
        if(robotVision.getClosestMineral() != null) tflowDetectionGood = true;

        telemetry.addData("Robot Vision", "Checking... Vuforia");
        telemetry.addData("Vuforia", "Put a navigation target into the camera view");
        telemetry.update();
        robotVision.startTrackingLocation();
        robotVision.startDetection();
        sleep(1000);
        if(robotVision.getRobotLocation() != null) vuforiaNavigationGood = true;
        robotVision.stopDetection();
    }

    private void reportRobotStatus() {
        // Drive Motors
        if(driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] == 2) {
            telemetry.addData("Drive Motor", "FL - Good! 2/2");
        } else if(driveMotorCount[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] == 1) {
            telemetry.addData("Drive Motor", "FL - Error! 1/2");
        } else telemetry.addData("Drive Motor", "FL - Error! 0/2");
        if(driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 2) {
            telemetry.addData("Drive Motor", "FR - Good! 2/2");
        } else if(driveMotorCount[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 1) {
            telemetry.addData("Drive Motor", "FR - Error! 1/2");
        } else telemetry.addData("Drive Motor", "FR - Error! 0/2");
        if(driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] == 2) {
            telemetry.addData("Drive Motor", "BL - Good! 2/2");
        } else if(driveMotorCount[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] == 1) {
            telemetry.addData("Drive Motor", "BL - Error! 1/2");
        } else telemetry.addData("Drive Motor", "BL - Error! 0/2");
        if(driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 2) {
            telemetry.addData("Drive Motor", "BR - Good! 2/2");
        } else if(driveMotorCount[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] == 1) {
            telemetry.addData("Drive Motor", "BR - Error! 1/2");
        } else telemetry.addData("Drive Motor", "BR - Error! 0/2");
        telemetry.update();
        sleep(3000);

        // Mineral System
        if(liftMotorCount == 2) telemetry.addData("Mineral System", "Arm - Good! 2/2");
        else if(liftMotorCount == 1) telemetry.addData("Mineral System", "Arm - Error! 1/2");
        else telemetry.addData("Mineral System", "Arm - Error! 0/2");
        if(extensionMotorCount == 2) telemetry.addData("Mineral System", "Extension - Good! 2/2");
        else if(extensionMotorCount == 1) telemetry.addData("Mineral System", "Extension - Error! 1/2");
        else telemetry.addData("Mineral System", "Extension - Error! 0/2");
        telemetry.update();
        sleep(3000);

        // Latch System
        if(extendSwitchGood) telemetry.addData("Latch System", "Extend Switch - Good!");
        else telemetry.addData("Latch System", "Extend Switch - Bad!");
        if(retractSwitchGood) telemetry.addData("Latch System", "Retract Switch - Good!");
        else telemetry.addData("Latch System", "Retract Switch - Bad!");
        if(winchMotorCount == 2) telemetry.addData("Latch System", "Winch Motor - Good!");
        else if(winchMotorCount == 1) telemetry.addData("Latch System", "Winch Motor - Error! 1/2");
        else telemetry.addData("Latch System", "Winch Motor - Error!");
        telemetry.update();
        sleep(3000);

        // Camera
        if(tflowDetectionGood) telemetry.addData("Robot Vision", "TensorFlow - Good!");
        else telemetry.addData("Robot Vision", "TesnsorFlow - Bad!");
        if(vuforiaNavigationGood) telemetry.addData("Robot Vision", "Vuforia - Good!");
        else telemetry.addData("Robot Vision", "Vuforia - Bad!");
        sleep(3000);

        // Misc

    }
}
