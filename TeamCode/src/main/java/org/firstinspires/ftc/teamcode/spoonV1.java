package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import ExampleBaseCode.Actions.DepositSystemV1;
import ExampleBaseCode.Actions.HardwareWrappers.ServoHandler;
import ExampleBaseCode.Actions.HardwareWrappers.SpoolMotor;
import ExampleBaseCode.DriveEngine.HolonomicDriveSystemTesting;
import ExampleBaseCode.MotorControllers.MotorController;
import ExampleBaseCode.UserControlled.JoystickHandler;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Spoon V1", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class spoonV1 extends LinearOpMode {

    final double movementScale = 1;
    final double turningScale = .75;
    final double RELEASE_ANGLE = 41;
    final double LATCH_ANGLE = 69;
    final double OFF = 0.5;
//    int COLLECT_POSITION = 150;
//    int DEPOSIT_POSITION = 0;
    DepositSystemV1 depositSystem;
//    SpoolMotor extendotron;
    SpoolMotor armLifter;
//    ServoHandler depositer;
    ServoHandler collector;
//    ServoHandler deadbolt;

    @Override
    public void runOpMode() throws InterruptedException {
        HolonomicDriveSystemTesting driveSystem = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
        try {
//            extendotron = new SpoolMotor(new MotorController("extendotron", "MotorConfig/FunctionMotors/ArmExtender.json", hardwareMap), 5, 5, 50, hardwareMap);
            armLifter = new SpoolMotor(new MotorController("lifter", "MotorConfig/FunctionMotors/Lifter.json", hardwareMap), 3, 5, 50, hardwareMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        depositSystem = new DepositSystemV1(hardwareMap);
//        depositer = new ServoHandler("depositer", hardwareMap);
//        depositer.setDirection(Servo.Direction.FORWARD);
//        depositer.setServoRanges(DEPOSIT_POSITION, COLLECT_POSITION);
//        depositer.setDegree(COLLECT_POSITION);
        collector = new ServoHandler("collector", hardwareMap);
        collector.setDirection(Servo.Direction.REVERSE);
        collector.setPosition(OFF);
//        deadbolt = new ServoHandler("deadbolt", hardwareMap);
//        deadbolt.setDirection(Servo.Direction.FORWARD);
//        deadbolt.setServoRanges(40, 70);
//        deadbolt.setDegree(LATCH_ANGLE);
//        extendotron.setDirection(DcMotorSimple.Direction.FORWARD);
        armLifter.setDirection(DcMotorSimple.Direction.REVERSE);
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
//        extendotron.setExtendPower(1);

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();
        double movementPower;
        double turningPower;


        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.abs(rightStick.x())/rightStick.x();
            if(gamepad1.a) collector.setPosition(1);
            else if(gamepad1.b) collector.setPosition(0);
            else collector.setPosition(OFF);
            if(gamepad1.left_trigger > 0.1) armLifter.extendWithPower();
            else if(gamepad1.left_bumper) armLifter.retractWithPower();
            else armLifter.pause();
            if(gamepad1.dpad_up) depositSystem.depositMineral();
            else if(gamepad1.dpad_down) depositSystem.collectMineral();
            if(gamepad1.right_trigger > 0.1) depositSystem.extendLift();
            else if(gamepad1.right_bumper) depositSystem.retractLift();
            else depositSystem.pauseLift();
//            if(gamepad1.dpad_right) deadbolt.setDegree(RELEASE_ANGLE);
//            else if(gamepad1.dpad_left) deadbolt.setDegree(LATCH_ANGLE);
            driveSystem.cartesianDriveOnHeadingWithTurning(leftStick.angle()+45, movementPower, turningPower);
//            driveSystem.driveOnHeadingPID(0, 1, initialOrientation);
            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
//            telemetry.addData("Depositer Degree", COLLECT_POSITION);
//            telemetry.addData("Deadbolt Degree", deadbolt.getActualDegree());
            telemetry.update();
        }
    }

}
