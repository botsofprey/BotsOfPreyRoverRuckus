package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.RNBMineralSystem;
import Actions.RNBMineralSystemV2;
import DriveEngine.HolonomicDriveSystemTesting;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Spoon V2", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class spoonV2 extends LinearOpMode {
    final double movementScale = 1;
    final double turningScale = .75;

    RNBMineralSystemV2 mineralSystem;

    @Override
    public void runOpMode() throws InterruptedException {
        HolonomicDriveSystemTesting driveSystem = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
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
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.abs(rightStick.x())/rightStick.x();

            handleMineralSystem();
            driveSystem.driveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);


            telemetry.addData("Gamepad1 left Joystick",leftStick.y());
            telemetry.addData("Gamepad1 right Joystick", rightStick.y());
            telemetry.addData("Gamepad1 right Trigger", gamepad1.right_trigger);
            telemetry.update();
        }
        mineralSystem.kill();
        driveSystem.kill();
    }

    private void handleMineralSystem() {
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad1.left_trigger);
        else if(gamepad1.left_bumper) mineralSystem.lower();
        else if(gamepad2.right_stick_y > 0.1 || gamepad2.right_stick_y < -0.1) mineralSystem.liftOrLower(gamepad2.right_stick_y);
        else mineralSystem.pauseLift();

        if(gamepad1.a) mineralSystem.intake();
        else if(gamepad1.b) mineralSystem.expel();
        else if(gamepad2.a) mineralSystem.intake();
        else if(gamepad2.b) mineralSystem.expel();
        else mineralSystem.pauseCollection();

        if(gamepad1.right_trigger > 0.1) mineralSystem.extendOrRetract(gamepad1.right_trigger);
        else if(gamepad1.right_bumper) mineralSystem.retractIntake();
        else if(-gamepad2.left_stick_y > 0.1 || -gamepad2.left_stick_y < -0.1) mineralSystem.extendOrRetract(gamepad2.left_stick_y);
        else mineralSystem.pauseExtension();
    }

    private void handleLatchSystem(){

    }
}
