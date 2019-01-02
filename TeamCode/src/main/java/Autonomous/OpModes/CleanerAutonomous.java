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

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import Autonomous.Location;
import Autonomous.TensorFlowHelper;
import DriveEngine.JennyNavigation;

@Autonomous(name = "Silver Side Autonomous Holonomic Drive", group = "Concept")
//@Disabled
public class CleanerAutonomous extends LinearOpMode {
    JennyNavigation navigation;

    private TensorFlowHelper tflow;

    @Override
    public void runOpMode() {
        tflow = new TensorFlowHelper(hardwareMap);

        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 180,"RobotConfig/JennyV2.json");
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

        tflow.startDetection();
        int goldPosition = tflow.getGoldMineralPosition();

        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && goldPosition == TensorFlowHelper.NOT_DETECTED && System.currentTimeMillis() - startTime <= 25000) goldPosition = tflow.getGoldMineralPosition();

        navigation.driveDistance(15, 0, 25, this);

        if (goldPosition == TensorFlowHelper.LEFT) {
            telemetry.addData("driving...", "left");
            telemetry.update();
            navigation.driveDistance(21, 90, 25, this);
        } else if (goldPosition == TensorFlowHelper.RIGHT) {
            telemetry.addData("driving...", "right");
            telemetry.update();
            navigation.driveDistance(21, 270, 25, this);
        }

        telemetry.addData("driving...", "forward");
        telemetry.update();
        navigation.driveDistance(26, 0,25, this);

        telemetry.addData("Status", "Waiting to end...");
        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
        tflow.kill();
    }
}
