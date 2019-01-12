package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.LatchSystem;
import Actions.MineralSystemV3;
import DriveEngine.HolonomicDriveSystemTesting;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Roseanna v3", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RoseannaV3 extends LinearOpMode {
    final double movementScale = 1;
    double turningScale = .75;
    boolean reversedDrive = false;
    boolean intaking = false;

    JoystickHandler leftStick, rightStick;
    MineralSystemV3 mineralSystem;
    LatchSystem latchSystem;
    HolonomicDriveSystemTesting navigation;

    @Override
    public void runOpMode() throws InterruptedException {
        navigation = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
        mineralSystem = new MineralSystemV3(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);

        double movementPower;
        double turningPower;

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();


        while(opModeIsActive()){
            turningScale = (mineralSystem.MAX_EXTEND_INCHES - mineralSystem.extensionMotor.getPositionInches()) / mineralSystem.MAX_EXTEND_INCHES;
            movementPower = movementScale * Math.abs(leftStick.magnitude());
            turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());

            handleMineralSystem();
            handleLatchSystem();

            if(gamepad1.x) reversedDrive = !reversedDrive;
            navigation.driveOnHeadingWithTurning((reversedDrive)? leftStick.angle() + 180:leftStick.angle(), movementPower, turningPower);


            telemetry.addData("Gamepad1 left Joystick",leftStick.y());
            telemetry.addData("Gamepad1 right Joystick", rightStick.y());
            telemetry.addData("Gamepad1 right Trigger", gamepad1.right_trigger);
            telemetry.addData("Extend Switch", latchSystem.limitSwitches[LatchSystem.EXTEND_SWITCH].isPressed());
            telemetry.addData("Retract Switch", latchSystem.limitSwitches[LatchSystem.RETRACT_SWITCH].isPressed());
            telemetry.addData("Arm Radius (in)", mineralSystem.extensionMotor.getPositionInches());
            telemetry.addData("Arm Rotation (ticks)", mineralSystem.liftMotor.getCurrentTick());
            telemetry.update();
        }
        mineralSystem.kill();
        navigation.kill();
        latchSystem.kill();
    }

    private void handleMineralSystem() {
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad1.left_trigger);
        else if(gamepad1.left_bumper) mineralSystem.lower();
        else if(gamepad2.right_stick_y > 0.1 || gamepad2.right_stick_y < -0.1) mineralSystem.liftOrLower(gamepad2.right_stick_y);
        else mineralSystem.pauseLift();

        if(gamepad1.a) {
            intaking = !intaking;
            while (gamepad1.a);
        }
        if(intaking) mineralSystem.intake();
        else if(gamepad1.b) mineralSystem.expel();
        else if(gamepad2.a) mineralSystem.intake();
        else if(gamepad2.b) mineralSystem.expel();
        else mineralSystem.pauseCollection();

        if(Math.abs(rightStick.y()) > 0.1) mineralSystem.extendOrRetract(rightStick.y());
        else if(gamepad1.right_trigger > 0.1) mineralSystem.extendOrRetract(gamepad1.right_trigger);
        else if(gamepad1.right_bumper) mineralSystem.retractIntake();
        else if(-gamepad2.left_stick_y > 0.1 || -gamepad2.left_stick_y < -0.1) mineralSystem.extendOrRetract(gamepad2.left_stick_y);
        else mineralSystem.pauseExtension();
    }

    private void handleLatchSystem(){
        if(gamepad1.dpad_up) latchSystem.extend();
        else if(gamepad1.dpad_down) latchSystem.retract();
        else latchSystem.pause();
    }
}
