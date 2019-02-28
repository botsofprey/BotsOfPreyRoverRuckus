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

import static Autonomous.VisionHelper.CENTER;
import static Autonomous.VisionHelper.LEFT;
import static Autonomous.VisionHelper.NOT_DETECTED;
import static Autonomous.VisionHelper.RIGHT;
import static Autonomous.VisionHelper.WEBCAM;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_SILVER_MINERAL;

@Autonomous(name = "Closest Mineral Test", group = "Testers")
@Disabled
public class ClosestMineralTest extends LinearOpMode {
    private VisionHelper robotVision;

    @Override
    public void runOpMode() {
        robotVision = new VisionHelper(WEBCAM, hardwareMap);

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        telemetry.addData("Status", "Running...");
        telemetry.update();
        // START AUTONOMOUS

//        robotVision.startGoldDetection();
//        robotVision.startDetection();
        robotVision.resetPositionVotes();
        while (opModeIsActive()) {
            sleep(250);
            String closestMineral = robotVision.getClosestMineral().getLabel();
            switch (closestMineral) {
                case LABEL_GOLD_MINERAL:
                    telemetry.addData("Closest Mineral", "gold");
                    break;
                case LABEL_SILVER_MINERAL:
                    telemetry.addData("Closest Mineral", "silver");
                    break;
                default:
                    telemetry.addData("Closest Mineral", "unknown");
                    break;
            }
            telemetry.update();
        }
        robotVision.kill();
    }
}
