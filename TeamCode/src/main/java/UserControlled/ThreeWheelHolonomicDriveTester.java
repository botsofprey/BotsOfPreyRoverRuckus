package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

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
    final double turningScale = 1;
    MotorController kicker;

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
            if(gamepad1.a) kicker.setMotorPower(1);
            else if(gamepad1.b) kicker.setMotorPower(-1);
            else kicker.brake();

            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.addData("Orientation", driveSystem.orientation.getOrientation());
            telemetry.update();
        }
    }

}
