package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.MecanumBotMineralSystem;
import DriveEngine.HolonomicDriveSystemTesting;
import DriveEngine.HolonomicDriveSystemTestingNoIMU;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Mecanum Bot User Controlled", group="Linear Opmode")  // @Autonomous(...) is the other common choice
@Disabled
public class mecanumDrive extends LinearOpMode {

    final double movementScale = 1;
    final double turningScale = .5;
    MecanumBotMineralSystem mineralSystem;

    @Override
    public void runOpMode() throws InterruptedException {
        HolonomicDriveSystemTestingNoIMU driveSystem = new HolonomicDriveSystemTestingNoIMU(hardwareMap,"RobotConfig/JennyV2.json");
        JoystickHandler leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        JoystickHandler rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        mineralSystem = new MecanumBotMineralSystem(hardwareMap);
        waitForStart();
        double movementPower = 0;
        double turningPower = 0;

        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            telemetry.addData("Left Stick Angle", leftStick.angle());
            telemetry.addData("Movement Power", movementPower);
            telemetry.addData("Turning Power", turningPower);
            driveSystem.driveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);

            handleMineralSystem();
            /*
             * Controls:
             * left stick - regular drive
             * right stick - turning
             * a button - collect
             * b button - spit
             * right trigger - rotate arm upward
             * right bumper - rotate arm downward
             * left trigger - extend arm
             * left bumper - retract arm
             */

            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.update();
        }
    }

    private void handleMineralSystem(){
        if(gamepad1.a) mineralSystem.collect();
        else if(gamepad1.b) mineralSystem.spit();
        else if(gamepad2.a) mineralSystem.collect();
        else if(gamepad2.b) mineralSystem.spit();
        else mineralSystem.pauseCollection();

        if(gamepad1.right_trigger > 0.1) mineralSystem.liftOrLowerArm(gamepad1.right_trigger);
        else if(gamepad1.right_bumper) mineralSystem.lowerArm();
        else if(gamepad2.right_trigger > 0.1) mineralSystem.liftOrLowerArm(gamepad2.right_trigger);
        else if(gamepad2.right_bumper) mineralSystem.lowerArm();
        else mineralSystem.pauseRotator();

        if(gamepad1.left_trigger > 0.1) mineralSystem.extendArm();
        else if(gamepad1.left_bumper) mineralSystem.retractArm();
        else if(gamepad2.left_trigger > 0.1) mineralSystem.extendArm();
        else if(gamepad2.left_bumper) mineralSystem.retractArm();
        else mineralSystem.pauseExtender();
    }

}
