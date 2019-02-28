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

import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

import static Autonomous.VisionHelper.BOTH;
import static Autonomous.VisionHelper.CENTER;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static Autonomous.VisionHelper.WEBCAM;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;

@Autonomous(name = "Track Gold Test", group = "Concept")
@Disabled
public class TrackGold extends LinearOpMode {
    JennyNavigation navigation;

    private VisionHelper robotVision;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(WEBCAM, BOTH, hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0,"RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();
        // START AUTONOMOUS

        robotVision.startGoldDetection();
        robotVision.startDetection();
        while (opModeIsActive()) {
            int pos = robotVision.getGoldMineralPosition();



            telemetry.addData("Left Count", robotVision.getPositionVote(LEFT));
            telemetry.addData("Center Count", robotVision.getPositionVote(CENTER));
            telemetry.addData("Right Count", robotVision.getPositionVote(RIGHT));
            telemetry.update();
        }
//        int goldPosition = findGold();
//        knockGold(goldPosition);


        //TODO: park with arm extended

        telemetry.addData("Status", "Waiting to end...");
        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
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
                navigation.turnToHeading(25,this);
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
        navigation.driveDistance(12, 90, 25, this);
        if (goldPosition == LEFT) {
            telemetry.addData("driving...", "left");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, 0, 25, this);
        } else if (goldPosition == RIGHT) {
            telemetry.addData("driving...", "right");
            telemetry.update();
            sleep(500);
            navigation.driveDistance(12, 180, 25, this);
        }

        telemetry.addData("driving...", "forward");
        telemetry.update();
        sleep(500);
        navigation.driveDistance(8, 90,25, this);
    }
}
