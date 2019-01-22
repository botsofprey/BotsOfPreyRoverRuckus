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
import Autonomous.Location;
import DriveEngine.JennyNavigation;
import MotorControllers.JsonConfigReader;
import Autonomous.VisionHelper;

import static Autonomous.VisionHelper.CENTER;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;

@Autonomous(name = "Testing...", group = "Concept")
//@Disabled
public class DelatchAutoTest extends LinearOpMode {
    JennyNavigation navigation;
    LatchSystem latchSystem;

    private VisionHelper robotVision;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
        InputStream stream = null;
        try {
            stream = hardwareMap.appContext.getAssets().open("FieldConfig/BlueLocations.json");
        }
        catch(Exception e){
            Log.d("Drive Engine Error: ",e.toString());
            throw new RuntimeException("Drive Engine Open Config File Fail: " + e.toString());
        }
        JsonConfigReader reader = new JsonConfigReader(stream);

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

        sleep(50);
        navigation.driveOnHeading(90, 5);
        while(opModeIsActive() && !latchSystem.limitSwitches[LatchSystem.EXTEND_SWITCH].isPressed()) latchSystem.extend();
        latchSystem.pause();
        navigation.brake();
        sleep(50);
        navigation.driveDistanceNonCorrected(4, 180, 10, this);
        idle();
        navigation.driveDistanceNonCorrected(2, 90, 10, this);
        idle();
        navigation.driveDistanceNonCorrected(4, 0, 10, this);
        idle();

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
        idle();
        navigation.driveDistanceNonCorrected(2, 190,15, this);
        sleep(100);
        int goldPosition = findGold();
        knockGold(goldPosition);


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
        navigation.turnToHeading(45 + 0, this);
        idle();
        return goldPosition;
    }

    private void knockGold(int goldPosition) {
        navigation.driveDistance(11, 45 + 90, 25, this);
        if (goldPosition == LEFT) {
            telemetry.addData("driving...", "left");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, 45 + 0, 25, this);
        } else if (goldPosition == RIGHT) {
            telemetry.addData("driving...", "right");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, 45 + 180, 25, this);
        }

        telemetry.addData("driving...", "forward");
        telemetry.update();
        sleep(500);
        navigation.driveDistance(8, 45 + 90,25, this);
    }
}
