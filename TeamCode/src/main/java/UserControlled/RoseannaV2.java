package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import java.io.IOException;

import Actions.HardwareWrappers.SpoolMotor;
import Actions.RNBMineralSystem;
import Actions.RNBMineralSystemV2;
import DriveEngine.HolonomicDriveSystem;
import DriveEngine.HolonomicDriveSystemTesting;
import MotorControllers.MotorController;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Roseanna v2", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RoseannaV2 extends LinearOpMode {
    final double movementScale = 1;
    final double turningScale = .75;

    RNBMineralSystemV2 mineralSystem;
    HolonomicDriveSystemTesting driveSystem;

    @Override
    public void runOpMode() throws InterruptedException {
        driveSystem = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
        mineralSystem = new RNBMineralSystemV2(hardwareMap);
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);

        double movementPower;
        double turningPower;

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();


        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());

            handleMineralSystem();
            driveSystem.cartesianDriveOnHeadingWithTurning(leftStick.angle() + 45, movementPower, turningPower);


            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.update();
        }
        mineralSystem.kill();
        driveSystem.kill();
    }

    private void handleMineralSystem() {
        if(gamepad1.a) mineralSystem.intake();
        else if(gamepad1.b) mineralSystem.expel();
        else mineralSystem.pauseCollection();
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad1.left_trigger);
        else if(gamepad1.left_bumper) mineralSystem.lower();
        else mineralSystem.pauseLift();
        if(gamepad1.right_trigger > 0.1) mineralSystem.extendOrRetract(gamepad1.right_trigger);
        else if(gamepad1.right_bumper) mineralSystem.retractIntake();
        else mineralSystem.pauseExtension();
    }
}
