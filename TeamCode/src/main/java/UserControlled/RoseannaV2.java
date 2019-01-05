package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.MineralSystemV2;
import DriveEngine.HolonomicDriveSystemTesting;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Roseanna v2", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RoseannaV2 extends LinearOpMode {
    final double movementScale = 1;
    final double turningScale = .75;
    boolean reversedDrive = false;

    JoystickHandler leftStick, rightStick;
    MineralSystemV2 mineralSystem;
    HolonomicDriveSystemTesting navigation;

    @Override
    public void runOpMode() throws InterruptedException {
        navigation = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
        mineralSystem = new MineralSystemV2(hardwareMap);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);

        double movementPower;
        double turningPower;

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();


        while(opModeIsActive()){
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());

            handleMineralSystem();

            if(gamepad1.x) reversedDrive = !reversedDrive;
            navigation.driveOnHeadingWithTurning((reversedDrive)? leftStick.angle() + 180:leftStick.angle(), movementPower, turningPower);


            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.update();
        }
        mineralSystem.kill();
        navigation.kill();
    }

    private void handleMineralSystem() {
        if(gamepad1.a) mineralSystem.intake();
        else if(gamepad1.b) mineralSystem.expel();
        else mineralSystem.pauseCollection();
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad1.left_trigger);
        else if(gamepad1.left_bumper) mineralSystem.lower();
        else mineralSystem.pauseLift();
        if(rightStick.y() > 0.1 || rightStick.y() < -0.1) mineralSystem.extendOrRetract(rightStick.y());
        else mineralSystem.pauseExtension();
    }
}
