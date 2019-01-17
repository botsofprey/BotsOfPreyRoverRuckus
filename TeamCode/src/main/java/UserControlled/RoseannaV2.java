package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.LatchSystem;
import Actions.MineralSystem;
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
    boolean movingToPos = false;

    JoystickHandler leftStick, rightStick;
    MineralSystemV2 mineralSystem;
    LatchSystem latchSystem;
    HolonomicDriveSystemTesting navigation;

    @Override
    public void runOpMode() throws InterruptedException {
        navigation = new HolonomicDriveSystemTesting(hardwareMap,"RobotConfig/JennyV2.json");
        mineralSystem = new MineralSystemV2(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
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
            handleLatchSystem();

            if(gamepad1.x) {
                reversedDrive = !reversedDrive;
                while (opModeIsActive() && gamepad1.x);
            }
            navigation.driveOnHeadingWithTurning((reversedDrive)? leftStick.angle() + 180:leftStick.angle(), movementPower, turningPower);


            telemetry.addData("Gamepad1 left Joystick",leftStick.toString());
            telemetry.addData("Gamepad1 right Joystick", rightStick.toString());
            telemetry.addData("Arm Rotation (ticks)", mineralSystem.liftMotor.getCurrentTick());
            telemetry.addData("Arm Rotation (deg)", mineralSystem.liftMotor.getDegree());
            telemetry.addData("Arm Extension (ticks)", mineralSystem.extensionMotor.getPosition());
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
        if(gamepad1.right_trigger > 0.1) {
            mineralSystem.extendOrRetract(gamepad1.right_trigger);
            movingToPos = false;
        }
        else if(gamepad1.right_bumper) {
            mineralSystem.retractIntake();
            movingToPos = false;
        }
        else mineralSystem.pauseExtension();

        if(gamepad1.y) {
            movingToPos = !movingToPos;
            while (opModeIsActive() && gamepad1.y);
        }
        if(movingToPos && !mineralSystem.goToPosition(MineralSystemV2.FAR_DEPOSIT_POSITION)) mineralSystem.goToPosition(MineralSystemV2.FAR_DEPOSIT_POSITION);
        else movingToPos = false;
    }

    private void handleLatchSystem(){
        if(gamepad1.dpad_up || gamepad2.dpad_up) latchSystem.retract();
        else if(gamepad1.dpad_down || gamepad2.dpad_down) latchSystem.extend();
        else latchSystem.pause();
    }
}
