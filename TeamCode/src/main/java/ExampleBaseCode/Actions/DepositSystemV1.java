package ExampleBaseCode.Actions;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import ExampleBaseCode.Actions.HardwareWrappers.ServoHandler;
import ExampleBaseCode.Actions.HardwareWrappers.SpoolMotor;
import ExampleBaseCode.MotorControllers.MotorController;

/**
 * Created by robotics on 10/26/18.
 */

public class DepositSystemV1 implements ActionHandler {
    SpoolMotor lift;
    ServoHandler depositer;
    private static final int COLLECT_DEGREE = 150;
    private static final int DEPOSIT_DEGREE = 0;

    public DepositSystemV1(HardwareMap hw){
        try {
            lift = new SpoolMotor(new MotorController("extendotron", "MotorConfig/FunctionMotors/ArmExtender.json", hw), 5,5,50,hw);
            lift.setDirection(DcMotorSimple.Direction.FORWARD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        depositer = new ServoHandler("depositer", hw);
        depositer.setDirection(Servo.Direction.FORWARD);
        depositer.setServoRanges(DEPOSIT_DEGREE, COLLECT_DEGREE);
        depositer.setDegree(COLLECT_DEGREE);
    }

    public void depositMineral(){
        depositer.setDegree(DEPOSIT_DEGREE);
    }
    public void collectMineral(){
        depositer.setDegree(COLLECT_DEGREE);
    }
    public void extendLift(){
        lift.extendWithPower();
    }
    public void retractLift(){
        lift.retractWithPower();
    }
    public void pauseLift(){
        lift.pause();
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

    }
}