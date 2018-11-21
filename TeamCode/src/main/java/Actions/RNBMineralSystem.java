package Actions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import Actions.HardwareWrappers.ServoHandler;
import Actions.HardwareWrappers.SpoolMotor;
import MotorControllers.MotorController;

public class RNBMineralSystem implements ActionHandler{
    public final int COLLECT_DEGREE = 0;
    public final int DEPOSIT_DEGREE = 150;
    private MotorController intakeMotor;
    private SpoolMotor extensionMotor;
    private SpoolMotor extendotron;
    private ServoHandler depositor;
    private HardwareMap hardwareMap;

    public RNBMineralSystem(HardwareMap hw){
        hardwareMap = hw;
        try {
            intakeMotor = new MotorController("collector", "MotorConfig/NeverRest40.json", hardwareMap);
            extensionMotor = new SpoolMotor(new MotorController("extension", "MotorConfig/NeverRest40.json", hardwareMap), 50, 50, 100, hardwareMap);
            extendotron = new SpoolMotor(new MotorController("extendotron", "MotorConfig/NeverRest40.json", hardwareMap), 50, 50, 100, hardwareMap);
            intakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            extensionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            extendotron.setDirection(DcMotorSimple.Direction.REVERSE);
            intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            extensionMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            extendotron.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            extensionMotor.setExtendPower(1);
            extendotron.setExtendPower(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        depositor = new ServoHandler("depositor", hardwareMap);
        depositor.setDirection(Servo.Direction.REVERSE);
        depositor.setServoRanges(COLLECT_DEGREE,DEPOSIT_DEGREE);
        depositor.setDegree(COLLECT_DEGREE);
    }

    public void extendIntake() {extensionMotor.extendWithPower();}
    public void retractIntake() {extensionMotor.retractWithPower();}
    public void pauseExtension() {extensionMotor.pause();}

    public void collect() {intakeMotor.setMotorPower(1);}
    public void spit() {intakeMotor.setMotorPower(-1);}
    public void pauseCollection() {intakeMotor.setMotorPower(0);}

    public void liftMinerals() {extendotron.extendWithPower();}
    public void lowerMinerals() {extendotron.retractWithPower();}
    public void pauseMineralLift() {extendotron.holdPosition();}

    public void depositMinerals() {depositor.setDegree(DEPOSIT_DEGREE);}
    public void readyForCollection() {depositor.setDegree(COLLECT_DEGREE);}

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
        pauseMineralLift();
        pauseExtension();
        pauseCollection();
        readyForCollection();
        intakeMotor.killMotorController();
        extendotron.kill();
        extensionMotor.kill();
    }
}
