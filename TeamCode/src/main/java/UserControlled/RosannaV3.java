package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.LatchSystem;
import Actions.MineralSystem;
import Actions.MineralSystemV3;
import DriveEngine.HolonomicDriveSystemTesting;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Rosanna v3", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RosannaV3 extends LinearOpMode {
    final double movementScale = 1;
    double turningScale = .75;
    boolean intaking = false;
    boolean p1Driving = true;
    boolean aReleased = true, startReleased = true;

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
            telemetry.addData("Arm Radius (ticks)", mineralSystem.extensionMotor.getPosition());
            telemetry.addData("Arm Rotation (ticks)", mineralSystem.liftMotor.getCurrentTick());
            telemetry.addData("Winch Extention (ticks)", latchSystem.winchMotor.getCurrentTick());
            telemetry.update();
        }
        mineralSystem.kill();
        navigation.kill();
        latchSystem.kill();
    }

    private void handleDriving() {
        if(startReleased && (gamepad1.start || gamepad2.start)) {
            startReleased = false;
            p1Driving = !p1Driving;
        } else if(!startReleased && !gamepad1.start && !gamepad2.start) {
            startReleased = true;
        }

        if(p1Driving) {
            double movementPower = movementScale * Math.abs(leftStick.magnitude());
            double turningPower = turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            navigation.driveOnHeadingWithTurning(leftStick.angle(), movementPower, turningPower);
        } else {
            double movementPower = movementScale * Math.abs(gamepad2LeftStick.magnitude());
            double turningPower = turningScale * Math.abs(gamepad2RightStick.magnitude()) * Math.signum(gamepad2RightStick.x());
            navigation.driveOnHeadingWithTurning(gamepad2LeftStick.angle() + 270, movementPower, turningPower);
        }
    }

    private void handleMineralSystem() {
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(-gamepad1.left_trigger);
        else if(gamepad2.left_trigger > 0.1) mineralSystem.liftOrLower(-gamepad2.left_trigger);
        else if(gamepad1.left_bumper || gamepad2.left_bumper) mineralSystem.lift();
        else mineralSystem.pauseLift();

        if(aReleased && (gamepad1.a || gamepad2.a)) {
            aReleased = false;
            intaking = !intaking;
        } else if(!aReleased && !gamepad1.a && !gamepad2.a) {
            aReleased = true;
        }


        if(intaking && !gamepad1.b && !gamepad2.b) mineralSystem.intake();
        else if(gamepad1.b || gamepad2.b) {
            mineralSystem.expel();
         } else mineralSystem.pauseCollection();


        if(gamepad1.dpad_down) mineralSystem.openDoor();
        else mineralSystem.closeDoor();

        if(gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) mineralSystem.extendIntake();
        else if(gamepad1.right_bumper || gamepad2.right_bumper) mineralSystem.retractIntake();
        else mineralSystem.pauseExtension();

        if(gamepad1.x) mineralSystem.goToPosition(MineralSystemV3.DEPOSIT_POSITION_NO_POLAR);
    }

    private void handleLatchSystem(){
        if(gamepad2.dpad_up) latchSystem.retract();
        else if(gamepad2.dpad_down) latchSystem.extend();
        else if(gamepad2.x) latchSystem.extendUnsafe();
        else if(gamepad2.y) latchSystem.retractUnsafe();
        else latchSystem.pause();
    }
}
