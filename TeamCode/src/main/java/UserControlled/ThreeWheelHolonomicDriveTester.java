package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import DriveEngine.HolonomicDriveSystemTesting;
import DriveEngine.ThreeWheelHolonomicDriveSystem;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="3 Wheel Holonomic Drive", group="Testers")  // @Autonomous(...) is the other common choice
//@Disabled
public class ThreeWheelHolonomicDriveTester extends LinearOpMode {

    final double movementScale = 1;
    final double turningScale = 1;

    @Override
    public void runOpMode() throws InterruptedException {
        ThreeWheelHolonomicDriveSystem driveSystem = new ThreeWheelHolonomicDriveSystem(hardwareMap,"RobotConfig/ThreeWheelHolonomicDriveConfig.json");
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        waitForStart();
        double movementPower = 0;
        double turningPower = 0;

        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            driveSystem.cartesianDriveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);
            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.addData("Orientation", driveSystem.orientation.getOrientation());
            telemetry.update();
        }
    }

}
