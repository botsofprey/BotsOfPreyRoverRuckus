package Actions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import Actions.HardwareWrappers.ServoHandler;
import Actions.HardwareWrappers.SpoolMotor;
import MotorControllers.MotorController;

public class CurrentBotMineralSystem implements ActionHandler{
    MotorController intake;
    SpoolMotor extendotron;
    ServoHandler depositor;
    HardwareMap hardwareMap;

    //TODO: check motor direction, extend and retract speeds, extension limits, and config file locations
    public CurrentBotMineralSystem(HardwareMap hw){
        hardwareMap = hw;
        try {
            intake = new MotorController("intakeMotor", "MotorConfig/NeverRest40.json", hardwareMap);
            extendotron = new SpoolMotor(new MotorController("liftMotor", "MotorConfig/NeverRest40.json", hardwareMap), 50, 50, 100, hardwareMap);
            intake.setDirection(DcMotorSimple.Direction.FORWARD);
            extendotron.setDirection(DcMotorSimple.Direction.FORWARD);
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            extendotron.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        } catch (IOException e){
            e.printStackTrace();
        }
        depositor = new ServoHandler("spool", hardwareMap);
        depositor.setDirection(Servo.Direction.FORWARD);
    }

    public void liftMinerals() {extendotron.extend();}
    public void lowerMinerals() {extendotron.retract();}
    public void pauseMineralLift() {extendotron.pause();}

    public void collect() {intake.setMotorPower(.5);}
    public void spit() {intake.setMotorPower(-.5);}
    public void pauseCollection() {intake.setMotorPower(0);}

    public void tiltBucketDown() {depositor.setPosition(1);}
    public void tiltBucketUp() {depositor.setPosition(0);}
    public void pauseBucket() {depositor.setPosition(0.52);}

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
        pauseBucket();
        pauseCollection();
        pauseMineralLift();
        intake.killMotorController();
        extendotron.kill();
    }
}
