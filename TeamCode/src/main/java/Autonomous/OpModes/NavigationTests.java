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

import Autonomous.Location;
import Autonomous.VisionHelper;
import DriveEngine.JennyNavigation;

@Autonomous(name="Drive to location", group ="Concept")
//@Disabled
public class NavigationTests extends LinearOpMode {
    JennyNavigation navigation;

    @Override public void runOpMode() {
        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0, "RobotConfig/JennyV2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        double radius = 12;

        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        waitForStart();
        // DRIVE TO LOCATION
//        for(int j = 0; j < 4; j++) {
//            for (int i = 0; i <= 360; i += 30) {
//                if(opModeIsActive()) navigation.driveToLocation(new Location(radius * Math.cos(Math.toRadians(i)), radius * Math.sin(Math.toRadians(i))), 15, this);
//            }
//        }
//        navigation.driveToLocation(new Location(0, 0), 15, this);

        // DRIVE DISTANCE
//        navigation.driveDistance(12, 45, 15, this);

        // DRIVE ON HEADING PID
        navigation.driveOnHeadingPID(45, 15, 0, this);



        telemetry.addData("Robot Location", navigation.getRobotLocation().toString());
        telemetry.update();
        while (opModeIsActive());
        navigation.stopNavigation();
    }
}
