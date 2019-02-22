package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.HardwareWrappers.FlagControllerTwoArms;
import Actions.LatchSystemV4;
import Actions.MineralSystemV4;
import Autonomous.Location;
import DriveEngine.JennyNavigation;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Rosanna v4", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RosannaV4 extends LinearOpMode {
    final double movementScale = 1;
    double turningScale = .75;
    boolean intaking = false, slowMode = false, latched = false;
    boolean p1Driving = true, flagWaving = false, trackingLocation = false;
    boolean aReleased = true, startReleased = true, dpadLReleased = true, a2Released = true, slowToggle = true, xReleased = true;
    long initialLatchPos = 0;

    JoystickHandler leftStick, rightStick, gamepad2LeftStick, gamepad2RightStick;
    MineralSystemV4 mineralSystem;
    LatchSystemV4 latchSystem;
    JennyNavigation navigation;
    FlagControllerTwoArms flagController;
    Location targetDeposit = new Location(54, 82, 325);

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            navigation = new JennyNavigation(hardwareMap, new Location(0, 0), 0, "RobotConfig/RosannaV4.json");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        mineralSystem = new MineralSystemV4(hardwareMap);
        latchSystem = new LatchSystemV4(hardwareMap);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        gamepad2LeftStick = new JoystickHandler(gamepad2, JoystickHandler.LEFT_JOYSTICK);
        gamepad2RightStick = new JoystickHandler(gamepad2, JoystickHandler.RIGHT_JOYSTICK);
        flagController = new FlagControllerTwoArms(hardwareMap);
        initialLatchPos = latchSystem.winchMotor.getCurrentTick();

//        camera.startTrackingLocation();

        telemetry.addData("Status", "Initialized!");
        telemetry.addData("OpMode Active", opModeIsActive());
        telemetry.update();
        while (!opModeIsActive()) latchSystem.pause();
        waitForStart();


        while(opModeIsActive()){
            turningScale = (mineralSystem.MAX_EXTEND_INCHES - mineralSystem.extensionMotor.getPositionInches()) / mineralSystem.MAX_EXTEND_INCHES;

            handleDriving();
            handleMineralSystem();
            handleLatchSystem();

            if(dpadLReleased && gamepad2.dpad_left) {
                dpadLReleased = false;
                flagWaving = !flagWaving;
            } else if(!dpadLReleased && !gamepad2.dpad_left) {
                dpadLReleased = true;
            }

            if(flagWaving) flagController.startFlag();
            else flagController.pauseFlag();

            telemetry.addData("Gamepad1 left Joystick",leftStick.y());
            telemetry.addData("Gamepad1 right Joystick", rightStick.y());
            telemetry.addData("Gamepad1 right Trigger", gamepad1.right_trigger);
            telemetry.addData("Arm Radius (ticks)", mineralSystem.extensionMotor.getPosition());
            telemetry.addData("Arm Rotation (ticks)", mineralSystem.liftMotor.getCurrentTick());
            telemetry.addData("Latch Extention (ticks)", latchSystem.winchMotor.getCurrentTick());

            telemetry.update();
        }
        mineralSystem.kill();
        navigation.stopNavigation();
        latchSystem.kill();
        flagController.killFlag();
    }

    private void handleDriving() {
        if(startReleased && (gamepad1.start || gamepad2.start)) {
            startReleased = false;
            p1Driving = !p1Driving;
        } else if(!startReleased && !gamepad1.start && !gamepad2.start) {
            startReleased = true;
        }

        if(slowToggle && !gamepad1.dpad_down) {
            slowMode = false;
            slowToggle = false;
        }
        else if(!slowToggle && gamepad1.dpad_down) {
            slowToggle = true;
        }

        if(xReleased && gamepad1.x) {
            xReleased = false;
            slowMode = !slowMode;
        } else if(!xReleased && !gamepad1.x) {
            xReleased = true;
        }

        if(p1Driving) {
            double movementPower = (slowMode)? 0.25 * movementScale * Math.abs(leftStick.magnitude()):movementScale * Math.abs(leftStick.magnitude());
            double turningPower = (slowMode)? 0.25 * turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x()): 0.5 * turningScale * Math.abs(rightStick.magnitude()) * Math.signum(rightStick.x());
            navigation.driveOnHeadingWithTurning(leftStick.angle() + 180, movementPower, turningPower);
            telemetry.addData("Joystick angle", leftStick.angle());
        } else {
            double movementPower =  0.65 * movementScale * Math.abs(gamepad2LeftStick.magnitude());
            double turningPower = 0.5 * turningScale * Math.abs(gamepad2RightStick.magnitude()) * Math.signum(gamepad2RightStick.x());
            navigation.driveOnHeadingWithTurning(gamepad2LeftStick.angle() + 270, movementPower, turningPower);
        }
    }

    private void handleMineralSystem() {
        if(gamepad1.left_trigger > 0.1) mineralSystem.liftOrLower(-gamepad1.left_trigger * 0.5);
        else if(gamepad2.left_trigger > 0.1) mineralSystem.liftOrLower(gamepad2.left_trigger);
        else if(gamepad1.left_bumper) mineralSystem.lift();
        else if(gamepad2.left_bumper) mineralSystem.liftOrLower(-0.75);
        else mineralSystem.pauseLift();

        if(aReleased && (gamepad1.a)) {
            aReleased = false;
            intaking = !intaking;
        } else if(!aReleased && !gamepad1.a) {
            aReleased = true;
        }


        if(intaking && !gamepad1.b) mineralSystem.intake();
        else if(gamepad1.b) {
            mineralSystem.expel();
         } else mineralSystem.pauseCollection();


        if(gamepad1.dpad_down) mineralSystem.openDoor();
        else mineralSystem.closeDoor();

        if(gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) mineralSystem.extendIntake();
        else if(gamepad1.right_bumper || gamepad2.right_bumper) mineralSystem.retractIntake();
        else mineralSystem.pauseExtension();

        if(gamepad2.a) mineralSystem.goToPosition(MineralSystemV4.DEPOSIT_POSITION_NO_POLAR);

        if(gamepad2.dpad_right) {
            targetDeposit = new Location(navigation.getRobotLocation());
            mineralSystem.setDepositTargetPosition();
        }
    }

    private void handleLatchSystem(){
        if(gamepad2.dpad_up) {
            latchSystem.retract();
            latched = true;
        }
        else if(gamepad2.dpad_down) {
            latchSystem.extend();
            latched = false;
        }
        else if(gamepad2.x) latchSystem.extendUnsafe();
        else if(gamepad2.y) latchSystem.retractUnsafe();
        else if(latched) latchSystem.pause();
        else latchSystem.winchMotor.brake();
    }
}
