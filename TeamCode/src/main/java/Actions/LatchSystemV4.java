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

public class LatchSystemV4 implements ActionHandler {
    public static final int UNHOOK_POSITION = 3525, HOOK_POSITION = 10;
    private int extendPosition = UNHOOK_POSITION, retractPosition = HOOK_POSITION;
    public MotorController winchMotor;
    HardwareMap hardwareMap;

    public LatchSystemV4(HardwareMap hw) {
        hardwareMap = hw;
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

    public void setExtendPosition(int pos) {
        extendPosition = pos;
    }

    public void setRetractPosition(int pos) {
        retractPosition = pos;
    }

    public void extend() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.setMotorPower(1);
    }

    public void retract() {
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.setMotorPower(-1);
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

    public void coastLatchMotor() {
        winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
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
