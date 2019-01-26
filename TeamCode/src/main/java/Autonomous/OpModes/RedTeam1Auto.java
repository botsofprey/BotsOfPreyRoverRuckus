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
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;


import Actions.LatchSystem;
import Actions.MineralSystemV3;
import Autonomous.Location;
import DriveEngine.JennyNavigation;
import Autonomous.VisionHelper;

import static Autonomous.VisionHelper.CENTER;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;

@Autonomous(name = "Red Team 1 Auto Test", group = "Concept")
//@Disabled
public class RedTeam1Auto extends LinearOpMode {
    JennyNavigation navigation;
    LatchSystem latchSystem;
    MineralSystemV3 mineralSystem;

    private VisionHelper robotVision;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
        mineralSystem = new MineralSystemV3(hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 45, "RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        robotVision.startTrackingLocation();

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();
        // START AUTONOMOUS
        //TODO: test driveDistance and adjust PID values

        sleep(50);
        navigation.driveOnHeading(90, 1);
        while(opModeIsActive() && !latchSystem.limitSwitches[LatchSystem.EXTEND_SWITCH].isPressed()) latchSystem.extend();
        latchSystem.pause();
        navigation.brake();
        sleep(50);
        navigation.driveDistanceNonCorrected(4, 180, 10, this);
        navigation.driveDistanceNonCorrected(2, 90, 10, this);
        navigation.driveDistanceNonCorrected(4, 0, 10, this);

//        navigation.turnToHeading(70 - 90, this);
//        robotVision.startDetection();
//        sleep(50);
//        Location robotLocation = null;
//        while (opModeIsActive() && robotLocation == null) {
//            robotLocation = robotVision.getRobotLocation();
//            telemetry.addData("Location", robotLocation);
//            telemetry.update();
//        }
//        navigation.setLocation(robotVision.getRobotLocation());
//        telemetry.addData("Navigation Location", navigation.getRobotLocation());
//        telemetry.update();
//        idle();
        navigation.turnToHeading(45, this);
        sleep(100);
        int goldPosition = -1;
        if(opModeIsActive()) goldPosition = findGold();
        idle();
        if(opModeIsActive()) knockGold(goldPosition);

        // DRIVE TO LEFT POSITION
        navigation.driveDistance(4, -45, 15, this);
        idle();
        if(goldPosition == RIGHT) navigation.driveDistance(24, 45, 15, this);
        else if(goldPosition == CENTER) navigation.driveDistance(12, 45, 15, this);

        navigation.turnToHeading(-30, this);
        robotVision.startDetection();
        sleep(50);
        Location robotLocation = null;
        while (opModeIsActive() && robotLocation == null) {
            robotLocation = robotVision.getRobotLocation();
            telemetry.addData("Location", robotLocation);
            telemetry.update();
        }
        navigation.setLocation(robotLocation);
        idle();

        navigation.turnToHeading(0, this);
        idle();
        navigation.driveToLocation(new Location(134.0, 72.0), 20, this); // position to wall
        navigation.turnToHeading(0, this);
        idle();

        navigation.driveToLocation(new Location(134.0, 100.0), 25, this); // fast to depot
        idle();

        navigation.driveToLocation(new Location(134.0, 122.0), 15, this); // position to depot
        idle();


        mineralSystem.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.liftMotor.setPositionTicks(3100);
        mineralSystem.liftMotor.setMotorPower(1);
        sleep(500);
        navigation.driveDistance(6, 180, 25, this);

        navigation.turnToHeading(180, this);
        idle();

        navigation.driveToLocation(new Location(navigation.getRobotLocation().getX(), 72.0), 25, this); // fast to crater
        idle();

        navigation.driveToLocation(new Location(navigation.getRobotLocation().getX(), 66.0), 15, this); // position to crater
        idle();

        mineralSystem.extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        mineralSystem.extensionMotor.setPostitionTicks(2500);
        mineralSystem.extensionMotor.setPower(1);

        navigation.turnToHeading(170, this);

        telemetry.addData("Final location", navigation.getRobotLocation());
        telemetry.addData("Status", "Waiting to end...");
        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
        latchSystem.kill();
        robotVision.kill();
    }

    private int findGold() {
        robotVision.resetPositionVotes();
        int goldPosition = robotVision.getGoldMineralPosition();
        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && goldPosition == NOT_DETECTED && System.currentTimeMillis() - startTime <= 25000) {
            sleep(250);
            if(robotVision.getClosestMineral().getLabel().equals(LABEL_GOLD_MINERAL)) goldPosition = CENTER;
            else {
                navigation.turnToHeading(45 + 25,this);
                sleep(500);
                if(robotVision.getClosestMineral().getLabel().equals(LABEL_GOLD_MINERAL)) goldPosition = RIGHT;
                else goldPosition = LEFT;
            }
        }
        navigation.turnToHeading(0, this);
        idle();
        return goldPosition;
    }

    private void knockGold(int goldPosition) {
        navigation.driveDistance(3.5, 135, 25, this);
        idle();
        if (goldPosition == LEFT) {
            telemetry.addData("driving...", "left");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, 45, 25, this);
            idle();
        } else if (goldPosition == RIGHT) {
            telemetry.addData("driving...", "right");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, -135, 25, this);
            idle();
        }

        telemetry.addData("driving...", "forward");
        telemetry.update();
        sleep(500);
        navigation.driveDistance(7.5, 135,25, this);
        idle();
    }
}
