package UserControlled;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Actions.LatchSystem;
import Actions.MineralSystemV3;
import DriveEngine.HolonomicDriveSystemTesting;

/**
 * Created by robotics on 2/16/18.
 */
@TeleOp(name="Roseanna v3 Demo", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class RosannaV3Demo extends LinearOpMode {
    boolean intaking = false;
    boolean aReleased = true;

    JoystickHandler rightStick, leftStick, gamepad2RightStick, gamepad2LeftStick;
    MineralSystemV3 mineralSystem;
    LatchSystem latchSystem;

    @Override
    public void runOpMode() throws InterruptedException {
        mineralSystem = new MineralSystemV3(hardwareMap);
        latchSystem = new LatchSystem(hardwareMap);
        rightStick = new JoystickHandler(gamepad1, JoystickHandler.RIGHT_JOYSTICK);
        leftStick = new JoystickHandler(gamepad1, JoystickHandler.LEFT_JOYSTICK);
        gamepad2LeftStick = new JoystickHandler(gamepad2, JoystickHandler.LEFT_JOYSTICK);
        gamepad2RightStick = new JoystickHandler(gamepad2, JoystickHandler.RIGHT_JOYSTICK);

        telemetry.addData("Status", "Initialized!");
        telemetry.update();
        waitForStart();


        while(opModeIsActive()){
            handleMineralSystem();
            handleLatchSystem();
        }
        mineralSystem.kill();
        latchSystem.kill();
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
