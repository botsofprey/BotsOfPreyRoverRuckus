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
    private MotorController intake;
    private ServoHandler intakeDoor;
    private HardwareMap hardwareMap;
    public static final int FAR_DEPOSIT_POSITION = 0;
    public static final int CLOSE_DEPOSIT_POSITION = 1;
    public static final int DEPOSIT_POSITION_NO_POLAR = 2;
    private int DEPOSIT_POSITION_EXTENSION = 6600 - 2500;
    private int DEPOSIT_POSITION_ROTATION = 7120 - 3100;
    private final double FAR_POSITION_R = 103;
    private final double FAR_POSITION_DEGREE = 142;
    private boolean movingToPosition = false;
    public final double MAX_EXTEND_INCHES = 150;
    public static final double OPEN_DOOR = 90;
    public static final double CLOSE_DOOR = 1;

    public MineralSystemV3(HardwareMap hw){
        hardwareMap = hw;
        try{
            extensionMotor = new SpoolMotor(new MotorController("extension", "MotorConfig/NeverRest40.json", hardwareMap)
                    , 50, 50, 100, hardwareMap);
            liftMotor = new MotorController("lift", "ActionConfig/LiftMotor.json", hardwareMap);
            intake = new MotorController("intake", "ActionConfig/LiftMotor.json", hardwareMap);
            extensionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            liftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            intake.setDirection(DcMotorSimple.Direction.REVERSE);
            intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            extensionMotor.setExtendPower(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        intakeDoor = new ServoHandler("intakeDoor", hardwareMap);
        intakeDoor.setDirection(Servo.Direction.FORWARD);
        intakeDoor.setServoRanges(CLOSE_DOOR-1, OPEN_DOOR+1);
        intakeDoor.setDegree(OPEN_DOOR);
    }

    public void extendIntake() {
        extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        extensionMotor.extendWithPower();
        movingToPosition = false;
    }
    public void retractIntake() {
        extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        extensionMotor.retractWithPower();
        movingToPosition = false;
    }
    public void extendOrRetract(double power) {
        extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        extensionMotor.setPower(power);
        movingToPosition = false;
    }
    public void pauseExtension() {
        if(!movingToPosition) {
            extensionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            extensionMotor.pause();
        }
    }


    public void lift() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(1);
        movingToPosition = false;
    }
    public void lower() {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(-1);
        movingToPosition = false;
    }
    public void liftOrLower(double power) {
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMotorPower(power);
        movingToPosition = false;
    }
    public void pauseLift() {
        if(!movingToPosition) {
            liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            liftMotor.holdPosition();
        }
    }

    public void intake() {intake.setMotorPower(1);}
    public void expel() {intake.setMotorPower(-1);}
    public void pauseCollection() {intake.setMotorPower(0);}

    public void openDoor() {
        intakeDoor.setDegree(OPEN_DOOR);
    }
    public void closeDoor() {
        intakeDoor.setDegree(CLOSE_DOOR);
    }
    public void setDoorDegree(double deg) {
        intakeDoor.setDegree(deg);
    }

    public boolean goToPosition(int target) {
        switch (target) {
            case FAR_DEPOSIT_POSITION:
                if(Math.abs(FAR_POSITION_DEGREE - liftMotor.getDegree()) >= 2 || Math.abs(FAR_POSITION_R - extensionMotor.getPositionInches()) >= 0.2) {
                    movingToPosition = true;
                    double slope = (FAR_POSITION_R * Math.sin(Math.toRadians(FAR_POSITION_DEGREE)) - extensionMotor.getPositionInches() *
                            Math.sin(Math.toRadians(liftMotor.getDegree()))) / (FAR_POSITION_R * Math.cos(Math.toRadians(FAR_POSITION_DEGREE)) -
                            extensionMotor.getPositionInches() * Math.cos(Math.toRadians(liftMotor.getDegree())));
                    double newR = 1 / (Math.sin(Math.toRadians(liftMotor.getDegree())) - slope * Math.cos(Math.toRadians(liftMotor.getDegree())));
                    if (extensionMotor.getMotorControllerMode() != DcMotor.RunMode.RUN_TO_POSITION)
                        extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//                    Log.d("Extension Motor Target", ""+newR);
                    extensionMotor.setPostitionInches(newR);
                    extensionMotor.setPower(1);
                    if (liftMotor.getMotorRunMode() != DcMotor.RunMode.RUN_TO_POSITION)
                        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftMotor.setPositionDegrees(FAR_POSITION_DEGREE);
                    liftMotor.setMotorPower(1);
                    return false;
                }
                else movingToPosition = false;
                return true;
            case CLOSE_DEPOSIT_POSITION:
                return true;
            case DEPOSIT_POSITION_NO_POLAR:
                if(Math.abs(DEPOSIT_POSITION_EXTENSION - extensionMotor.getPosition()) >= 100 || Math.abs(DEPOSIT_POSITION_ROTATION - liftMotor.getCurrentTick()) >= 100) {
                    movingToPosition = true;
                    if (extensionMotor.getMotorControllerMode() != DcMotor.RunMode.RUN_TO_POSITION)
                        extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    if (liftMotor.getMotorRunMode() != DcMotor.RunMode.RUN_TO_POSITION)
                        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    extensionMotor.setPostitionTicks(DEPOSIT_POSITION_EXTENSION);
                    liftMotor.setPositionTicks(DEPOSIT_POSITION_ROTATION);
                    liftMotor.setMotorPower(1);
                    extensionMotor.setPower(1);
                    return false;
                } else {
                    movingToPosition = false;
                }
                return true;
            default:
                return true;
        }
    }

    public void setDepositTargetPosition() {
        DEPOSIT_POSITION_EXTENSION = (int)extensionMotor.getPosition();
        DEPOSIT_POSITION_ROTATION = (int)liftMotor.getCurrentTick();
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
