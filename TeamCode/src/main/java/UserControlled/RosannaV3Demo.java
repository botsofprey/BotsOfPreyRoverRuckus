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
        }
        else mineralSystem.pauseCollection();

        if(Math.abs(rightStick.y()) > 0.1) mineralSystem.extendOrRetract(rightStick.y());
        else if(Math.abs(gamepad2RightStick.y()) > 0.1) mineralSystem.extendOrRetract(gamepad2RightStick.y());
        else mineralSystem.pauseExtension();
    }

    private void handleLatchSystem(){
        if(gamepad1.dpad_up || gamepad2.dpad_up) latchSystem.retract();
        else if(gamepad1.dpad_down || gamepad2.dpad_down) latchSystem.extend();
        else latchSystem.pause();
    }
}
