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
    MotorController rackAndPinion;
    public TouchSensor[] limitSwitches = new TouchSensor[2];
    HardwareMap hardwareMap;

    public LatchSystem(HardwareMap hw) {
        hardwareMap = hw;
        limitSwitches[RETRACT_SWITCH] = hardwareMap.touchSensor.get("retractSwitch");
        limitSwitches[EXTEND_SWITCH] = hardwareMap.touchSensor.get("extendSwitch");
        try {
            rackAndPinion = new MotorController("rackAndPinion", "ActionConfig/RackAndPinion.json", hardwareMap);
            rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rackAndPinion.setDirection(DcMotorSimple.Direction.REVERSE);
            rackAndPinion.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void extend() {
        rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if(!limitSwitches[EXTEND_SWITCH].isPressed()) rackAndPinion.setMotorPower(1);
        else rackAndPinion.brake();
    }

    public void retract() {
        rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if(!limitSwitches[RETRACT_SWITCH].isPressed()) rackAndPinion.setMotorPower(-1);
        else rackAndPinion.brake();
    }

    public void extendUnsafe() {
        rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rackAndPinion.setMotorPower(1);
    }

    public void retractUnsafe() {
        rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rackAndPinion.setMotorPower(-1);
    }

    public void pause() {
        rackAndPinion.holdPosition();
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
        rackAndPinion.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rackAndPinion.killMotorController();
    }
}
