package Actions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import java.io.IOException;

import MotorControllers.MotorController;

/**
 * Created by robotics on 12/21/18.
 */

public class LatchSystem implements ActionHandler {
    public static final int RETRACT_SWITCH = 0, EXTEND_SWITCH = 1;
    public static final int UNHOOK_POSITION = 0;
    public MotorController winchMotor;
    public TouchSensor[] limitSwitches = new TouchSensor[2];
    HardwareMap hardwareMap;

    public LatchSystem(HardwareMap hw) {
        hardwareMap = hw;
        limitSwitches[RETRACT_SWITCH] = hardwareMap.touchSensor.get("retractSwitch");
        limitSwitches[EXTEND_SWITCH] = hardwareMap.touchSensor.get("extendSwitch");
        try {
            winchMotor = new MotorController("winchMotor", "ActionConfig/RackAndPinion.json", hardwareMap);
            winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            winchMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            pause();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void extend() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if(!limitSwitches[EXTEND_SWITCH].isPressed()) winchMotor.setMotorPower(1);
        else pause();
    }

    public void retract() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if(!limitSwitches[RETRACT_SWITCH].isPressed()) winchMotor.setMotorPower(-1);
        else pause();
    }

    public void extendUnsafe() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.setMotorPower(1);
    }

    public void retractUnsafe() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.setMotorPower(-1);
    }

    public void pause() {
        winchMotor.holdPosition();
    }

    @Override
    public boolean doAction(String action, long maxTimeAllowed) {
        return false;
    }

    @Override
    public boolean stopAction(String action) {
        return false;
    }

    @Override
    public boolean startDoingAction(String action) {
        return false;
    }

    @Override
    public void kill() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.killMotorController();
    }
}
