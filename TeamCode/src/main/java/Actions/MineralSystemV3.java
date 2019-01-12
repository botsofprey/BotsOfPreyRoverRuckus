package Actions;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.io.IOException;

import Actions.HardwareWrappers.ServoHandler;
import Actions.HardwareWrappers.SpoolMotor;
import MotorControllers.MotorController;

public class MineralSystemV3 implements ActionHandler{
    public SpoolMotor extensionMotor;
    public MotorController liftMotor;
    private ServoHandler intake;
    private HardwareMap hardwareMap;
    public static final int FAR_DEPOSIT_POSITION = 0;
    public static final int CLOSE_DEPOSIT_POSITION = 1;
    private final double FAR_POSITION_R = 103;
    private final double FAR_POSITION_DEGREE = 142;
    private boolean movingToPosition = false;
    public final double MAX_EXTEND_INCHES = 120;

    public MineralSystemV3(HardwareMap hw){
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

    public void extendIntake() {extensionMotor.extendWithPower(); movingToPosition = false;}
    public void retractIntake() {extensionMotor.retractWithPower(); movingToPosition = false;}
    public void extendOrRetract(double power) {extensionMotor.setPower(power); movingToPosition = false;}
    public void pauseExtension() {if(!movingToPosition) extensionMotor.pause();}


    public void lift() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(1);
//        movingToPosition = false;
    }
    public void lower() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(-1);
//        movingToPosition = false;
    }
    public void liftOrLower(double power) {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(power);
//        movingToPosition = false;
    }
    public void pauseLift() {/*if(!movingToPosition)*/ liftMotor.holdPosition();}


    public void intake() {intake.setPosition(1);}
    public void expel() {intake.setPosition(0);}
    public void pauseCollection() {intake.setPosition(0.5);}

    public boolean goToPosition(int target) {
        switch (target) {
            case FAR_DEPOSIT_POSITION:
                if(Math.abs(FAR_POSITION_DEGREE - liftMotor.getDegree()) >= 2 || Math.abs(FAR_POSITION_R - extensionMotor.getPositionInches()) >= 0.2) {
                    movingToPosition = true;
                    double slope = (FAR_POSITION_R * Math.sin(Math.toRadians(FAR_POSITION_DEGREE)) - extensionMotor.getPositionInches() * Math.sin(Math.toRadians(liftMotor.getDegree()))) / (FAR_POSITION_R * Math.cos(Math.toRadians(FAR_POSITION_DEGREE)) - extensionMotor.getPositionInches() * Math.cos(Math.toRadians(liftMotor.getDegree())));
                    double newR = 1 / (Math.sin(Math.toRadians(liftMotor.getDegree())) - slope * Math.cos(Math.toRadians(liftMotor.getDegree())));
                    if (extensionMotor.getMotorControllerMode() != DcMotor.RunMode.RUN_TO_POSITION)
                        extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    Log.d("Extension Motor Target", ""+newR);
//                    extensionMotor.setPostitionInches(newR);
//                    extensionMotor.setPower(1);
//                    if (liftMotor.getMotorRunMode() != DcMotor.RunMode.RUN_TO_POSITION)
//                        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//                    liftMotor.setPositionDegrees(FAR_POSITION_DEGREE);
//                    liftMotor.setMotorPower(1);
                    return false;
                }
                else movingToPosition = false;
                return true;
            case CLOSE_DEPOSIT_POSITION:
                return true;
            default:
                return true;
        }
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
        pauseExtension();
        pauseCollection();
        extensionMotor.kill();
        liftMotor.killMotorController();
    }
}
