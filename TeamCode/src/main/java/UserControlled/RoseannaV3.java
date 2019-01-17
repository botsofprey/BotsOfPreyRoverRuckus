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
    boolean p1Driving = true;

    JoystickHandler leftStick, rightStick, gamepad2LeftStick, gamepad2RightStick;
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
        gamepad2LeftStick = new JoystickHandler(gamepad2, JoystickHandler.LEFT_JOYSTICK);
        gamepad2RightStick = new JoystickHandler(gamepad2, JoystickHandler.RIGHT_JOYSTICK);

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();


        while(opModeIsActive()){
            turningScale = (mineralSystem.MAX_EXTEND_INCHES - mineralSystem.extensionMotor.getPositionInches()) / mineralSystem.MAX_EXTEND_INCHES;



            handleDriving();
            handleMineralSystem();
            handleLatchSystem();



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

    private void handleDriving() {
        if(gamepad1.start || gamepad2.start) {
            p1Driving = !p1Driving;
            while (opModeIsActive() && (gamepad1.start || gamepad2.start));
        }

        if(p1Driving) {
            double movementPower = movementScale * Math.abs(leftStick.magnitude());
            double turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            if (gamepad1.x) {
                reversedDrive = !reversedDrive;
                while (opModeIsActive() && gamepad1.x);
            }
            navigation.driveOnHeadingWithTurning((reversedDrive) ? leftStick.angle() + 180 : leftStick.angle(), movementPower, turningPower);
        } else {
            double movementPower = movementScale * Math.abs(gamepad2LeftStick.magnitude());
            double turningPower = turningScale * Math.abs(gamepad2RightStick.magnitude()) * Math.signum(gamepad2RightStick.x());
            if (gamepad2.x) {
                reversedDrive = !reversedDrive;
                while (opModeIsActive() && gamepad2.x);
            }
            navigation.driveOnHeadingWithTurning((reversedDrive) ? gamepad2LeftStick.angle() + 180 : gamepad2LeftStick.angle(), movementPower, turningPower);
        }
    }

    private void handleMineralSystem() {
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad1.left_trigger);
        else if(gamepad2.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad2.left_trigger);
        else if(gamepad1.left_bumper || gamepad2.left_bumper) mineralSystem.lower();
        else mineralSystem.pauseLift();

        if(gamepad1.a || gamepad2.a) {
            intaking = !intaking;
            while (opModeIsActive() && (gamepad1.a || gamepad2.a));
        }

        if(intaking && !gamepad1.b && !gamepad2.b) mineralSystem.intake();
        else if(gamepad1.b || gamepad2.b) {
            mineralSystem.expel();
            intaking = false;
        } else mineralSystem.pauseCollection();

        if(Math.abs(rightStick.y()) > 0.1) mineralSystem.extendOrRetract(rightStick.y());
        else if(Math.abs(gamepad2LeftStick.y()) > 0.1) mineralSystem.extendOrRetract(gamepad2LeftStick.y());
        else mineralSystem.pauseExtension();
    }

    private void handleLatchSystem(){
        if(gamepad1.dpad_up || gamepad2.dpad_up) latchSystem.retract();
        else if(gamepad1.dpad_down || gamepad2.dpad_down) latchSystem.extend();
        else latchSystem.pause();
    }
}
