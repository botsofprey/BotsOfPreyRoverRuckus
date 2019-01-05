package Actions;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import Actions.HardwareWrappers.ServoHandler;
import Actions.HardwareWrappers.SpoolMotor;
import MotorControllers.MotorController;

public class MineralSystemV2 implements ActionHandler{
    private SpoolMotor extensionMotor;
    private MotorController liftMotor;
    private ServoHandler intake;
    private HardwareMap hardwareMap;

    public MineralSystemV2(HardwareMap hw){
        hardwareMap = hw;
        try{
            extensionMotor = new SpoolMotor(new MotorController("extension", "MotorConfig/NeverRest40.json", hardwareMap), 50, 50, 100, hardwareMap);
            liftMotor = new MotorController("lift", "ActionConfig/LiftMotor.json", hardwareMap);
            extensionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            liftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            extensionMotor.setExtendPower(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        intake = new ServoHandler("intake", hardwareMap);
        intake.setDirection(Servo.Direction.FORWARD);
    }

    public void extendIntake() {extensionMotor.extendWithPower();}
    public void retractIntake() {extensionMotor.retractWithPower();}
    public void extendOrRetract(double power) {extensionMotor.setPower(power);}
    public void pauseExtension() {extensionMotor.pause();}


    public void lift() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(0.5);
    }
    public void lower() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(-0.5);
    }
    public void liftOrLower(double power) {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(power);
    }
    public void pauseLift() {liftMotor.holdPosition();}


    public void intake() {intake.setPosition(1);}
    public void expel() {intake.setPosition(0);}
    public void pauseCollection() {intake.setPosition(0.5);}

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
        pauseExtension();
        pauseCollection();
        extensionMotor.kill();
        liftMotor.killMotorController();
    }
}
