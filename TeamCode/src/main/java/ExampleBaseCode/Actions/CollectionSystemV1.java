package ExampleBaseCode.Actions;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import ExampleBaseCode.Actions.HardwareWrappers.ServoHandler;
import ExampleBaseCode.Actions.HardwareWrappers.SpoolMotor;
import ExampleBaseCode.MotorControllers.MotorController;

/**
 * Created by robotics on 10/26/18.
 */

public class CollectionSystemV1 implements ActionHandler{
    ServoHandler collector;
    SpoolMotor collectLift;

    public CollectionSystemV1(HardwareMap hw){
        try {
            collectLift = new SpoolMotor(new MotorController("lifter", "MotorConfig/FunctionMotors/Lifter.json", hw), 3,5,50, hw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        collector = new ServoHandler("collector",hw);
        collector.setDirection(Servo.Direction.FORWARD);
//        collector.
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
