package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import Actions.HardwareWrappers.ServoHandler;
import DriveEngine.HolonomicDriveSystemTesting;
import DriveEngine.ThreeWheelHolonomicDriveSystem;
import MotorControllers.MotorController;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="3 Wheel Holonomic Drive", group="Testers")  // @Autonomous(...) is the other common choice
//@Disabled
public class ThreeWheelHolonomicDriveTester extends LinearOpMode {

    final double movementScale = 1;
    final double turningScale = 0.8;
    MotorController kicker;
    ServoHandler leftIntake, rightIntake;

    @Override
    public void runOpMode() throws InterruptedException {
        ThreeWheelHolonomicDriveSystem driveSystem = new ThreeWheelHolonomicDriveSystem(hardwareMap,"RobotConfig/ThreeWheelHolonomicDriveConfig.json");
        try {
            kicker = new MotorController("kicker", "MotorConfig/NeverRest40.json", hardwareMap);
            kicker.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            kicker.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            kicker.setDirection(DcMotorSimple.Direction.FORWARD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        leftIntake = new ServoHandler("leftIntake", hardwareMap);
        rightIntake = new ServoHandler("rightIntake", hardwareMap);
        leftIntake.setDirection(Servo.Direction.REVERSE);
        rightIntake.setDirection(Servo.Direction.FORWARD);
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        waitForStart();
        double movementPower = 0;
        double turningPower = 0;

        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            driveSystem.cartesianDriveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);

            // NOTE: winch servo is, as it says, a winch... it is programmed like a CR servo but goes until it reaches a certain amount of rotations, then it can go back
            if(gamepad1.right_trigger > 0.1) kicker.setMotorPower(1);
            else if(gamepad1.right_bumper) kicker.setMotorPower(-1);
            else kicker.brake();

            if(gamepad1.a) {
                leftIntake.setPosition(1);
                rightIntake.setPosition(1);
            } else if(gamepad1.b) {
                leftIntake.setPosition(0);
                rightIntake.setPosition(0);
            } else {
                leftIntake.setPosition(0.5);
                rightIntake.setPosition(0.5);
            }

            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.addData("Orientation", driveSystem.orientation.getOrientation());
            telemetry.update();
        }
        driveSystem.kill();
        kicker.killMotorController();
    }

}
