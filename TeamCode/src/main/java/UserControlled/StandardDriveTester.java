package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import DriveEngine.StandardDriveSystem;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Standard Drive Tester", group="Testers")  // @Autonomous(...) is the other common choice
@Disabled
public class StandardDriveTester extends LinearOpMode {

    final double movementScale = 1;
    final double turningScale = .5;
    StandardDriveSystem navigation;

    @Override
    public void runOpMode() throws InterruptedException {
        navigation = new StandardDriveSystem(hardwareMap, "RobotConfig/JennyV2.json");
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        waitForStart();
        double movementPower = 0;
        double turningPower = 0;

        while(opModeIsActive()){
             movementPower = movementScale * Math.abs(leftStick.magnitude());
             turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());

             navigation.driveWithTurning(movementPower, turningPower);

            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.update();
        }
    }

}
