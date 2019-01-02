package Actions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.IOException;

import Actions.HardwareWrappers.SpoolMotor;
import DriveEngine.JennyNavigation;
import MotorControllers.MotorController;

/**
 * Created by robotics on 1/2/19.
 */

public class JeremyController implements ActionHandler{
    private SpoolMotor tiltMotor;
    private SpoolMotor extendMotor;
    private HardwareMap hardwareMap;

    public JeremyController(HardwareMap hw) {
        hardwareMap = hw;
        try {
            tiltMotor = new SpoolMotor(new MotorController("tilt", "MotorConfig/NeverRest40.json", hardwareMap), 10, 10, 100, hardwareMap);
            extendMotor = new SpoolMotor(new MotorController("extend", "MotorConfig/NeverRest40.json", hardwareMap), 10, 10, 100, hardwareMap);
            tiltMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            extendMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            tiltMotor.setExtendPower(1);
            extendMotor.setExtendPower(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lift(){
        tiltMotor.extendWithPower();
    }
    public void lower(){
        tiltMotor.retractWithPower();
    }
    public void setTiltMotorPower(double power){
        tiltMotor.setPower(power);
    }
    public void pauseLifter(){
        tiltMotor.holdPosition();
    }

    public void extend(){
        extendMotor.extendWithPower();
    }
    public void retract(){
        extendMotor.retractWithPower();
    }
    public void setExtendMotorPower(double power){
        extendMotor.setPower(power);
    }
    public void pauseExtension(){
        extendMotor.holdPosition();
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
        tiltMotor.kill();
        extendMotor.kill();
    }
}
