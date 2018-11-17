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

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.io.IOException;

import Actions.CurrentBotMineralSystem;
import Actions.HardwareWrappers.SpoolMotor;
import MotorControllers.MotorController;

@TeleOp(name="TysonBrosCode", group="Linear Opmode")
//@Disabled
public class TysonBrosTeam extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    DcMotor leftMotor;
    DcMotor rightMotor;

    CurrentBotMineralSystem mineralSystem;

    @Override
    public void runOpMode() {

        leftMotor = hardwareMap.dcMotor.get("leftMotor");
        rightMotor = hardwareMap.dcMotor.get("rightMotor");
        mineralSystem = new CurrentBotMineralSystem(hardwareMap);



        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        telemetry.addData("Status", "Initialized");
        telemetry.update();


        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            if(gamepad1.dpad_up){
                mineralSystem.tiltBucketDown();
            }
            else if (gamepad1.dpad_down){
                mineralSystem.tiltBucketUp();
            }
            else {
                mineralSystem.pauseBucket();
            }
            if (gamepad1.left_bumper) {
                leftMotor.setPower(-1); // WHAT IS WRONG HERE?????
            } else if (gamepad1.left_trigger > 0.1) {
                leftMotor.setPower(gamepad1.left_trigger); // AND HERE????
            } else {
                leftMotor.setPower(0);
            }
            if (gamepad1.right_bumper) {
                rightMotor.setPower(-1); // AND HERE????
            } else if (gamepad1.right_trigger > 0.1) {
                rightMotor.setPower(gamepad1.right_trigger); // AND HERE????
            } else {
                rightMotor.setPower(0);
            }
            if(gamepad1.left_stick_y >= 0.1) {
                mineralSystem.liftMinerals();
            }
            else if (gamepad1.left_stick_y <= -0.1){
                mineralSystem.lowerMinerals();
            }
            else mineralSystem.pauseMineralLift();

            if (gamepad1.a) mineralSystem.collect();
            else if (gamepad1.b) mineralSystem.spit();
            else mineralSystem.pauseCollection();

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.update();

        }
    }
}

