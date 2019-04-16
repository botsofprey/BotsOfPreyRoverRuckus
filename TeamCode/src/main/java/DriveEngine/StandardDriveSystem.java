package DriveEngine;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RollingAverage;

import java.io.InputStream;

import MotorControllers.JsonConfigReader;
import MotorControllers.MotorController;
import MotorControllers.PIDController;
import SensorHandlers.ImuHandler;

/**
 * Created by robotics on 11/20/18.
 */

public class StandardDriveSystem {
    MotorController[] driveMotors = new MotorController[2];
    public final int LEFT_MOTOR = 0;
    public final int RIGHT_MOTOR = 1;
    private PIDController headingController;
    private ImuHandler orientation;
    private HardwareMap hardwareMap;

    private double maxMotorVelocity = 0;

    //TODO: test everything
    public StandardDriveSystem(HardwareMap hw, String configFileLoc){
        hardwareMap = hw;
        readConfigAndInitialize(configFileLoc);
        double avg = 0;
        for(int i = 0; i < driveMotors.length; i ++){
            avg += driveMotors[i].getMaxSpeed();
        }
        avg /= driveMotors.length;
        maxMotorVelocity = avg;
    }

    public void drive(double[] velocities){
        applyMotorVelocities(velocities);
    }

    public void driveWithTurning(double power, double turnPower){
        double[] motorPowers = {power, power};
        motorPowers[LEFT_MOTOR] = Range.clip(power + turnPower, -1, 1);
        motorPowers[RIGHT_MOTOR] = Range.clip(power - turnPower, -1, 1);
        applyMotorPowers(motorPowers);
    }

    public void driveDistance(double distanceInInches, double velocity){
        for(int i = 0; i < driveMotors.length; i++){
            driveMotors[i].setPositionInches(distanceInInches);
        }
        applyMotorVelocities(new double[] {velocity, velocity});
    }

    public void turn(double turnVelocity){
        applyMotorVelocities(new double[] {turnVelocity, -turnVelocity});
    }

    //TODO: finish
    public void turnToHeading(double heading, LinearOpMode mode){
        double curOrientation = orientation.getOrientation();
        double rps;
        double distToHeading = 0;
        headingController.setSp(0);
        distToHeading = heading - curOrientation;
        if(distToHeading > 180) distToHeading -=360;
        else if(distToHeading < -180) distToHeading =+ 360;
        if(distToHeading > -180 && distToHeading < 180){
            while(Math.abs(distToHeading) > 1 && mode.opModeIsActive()){
                rps = headingController.calculatePID(distToHeading);
                turn(rps);//pos or neg
                mode.sleep(5);
                distToHeading = heading - curOrientation;
                if(distToHeading > 180) distToHeading -=360;
                else if(distToHeading < -180) distToHeading =+ 360;
            }
            brake();
        }


    }

    private void applyMotorVelocities(double [] velocities){
        driveMotors[LEFT_MOTOR].setInchesPerSecondVelocity(velocities[LEFT_MOTOR]);
        driveMotors[RIGHT_MOTOR].setInchesPerSecondVelocity(velocities[RIGHT_MOTOR]);
    }

    private void applyMotorPowers(double [] powers){
        normalizePowers(powers);
        double [] velocities = new double[4];
        for(int i = 0; i < powers.length; i ++){
            //Log.d("Motor " + i + "Power", "" + powers[i]);
            velocities[i] = powers[i]*maxMotorVelocity;
        }
        applyMotorVelocities(velocities);
    }

    private void normalizePowers(double [] toNormalize){
        //get the min and max powers
        double min = toNormalize[0], max = toNormalize[0];
        for(int i = 0; i < toNormalize.length; i ++){
            if(toNormalize[i] < min) min = toNormalize[i];
            else if(toNormalize[i] > max) max = toNormalize[i];
        }
        //assign toScaleAgainst to the largest (abs) value
        double toScaleAgainst = 0;
        if(Math.abs(min) < Math.abs(max)) toScaleAgainst = Math.abs(max);
        else toScaleAgainst = Math.abs(min);
        //if the largest (abs) is greater than 1, scale all values appropriately
        if(toScaleAgainst > 1){
            for(int i = 0; i < toNormalize.length; i ++){
                toNormalize[i] = toNormalize[i]/toScaleAgainst;
            }
        }
    }

    private void readConfigAndInitialize(String file){
        InputStream stream = null;
        try {
            stream = hardwareMap.appContext.getAssets().open(file);
        }
        catch(Exception e){
            Log.d("Drive Engine Error: ",e.toString());
            throw new RuntimeException("Drive Engine Open Config File Fail: " + e.toString());
        }
        JsonConfigReader reader = new JsonConfigReader(stream);
        try{
            driveMotors[LEFT_MOTOR] = new MotorController(reader.getString("LEFT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
            driveMotors[RIGHT_MOTOR] = new MotorController(reader.getString("RIGHT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
            for (int i = 0; i < driveMotors.length; i++) {
                driveMotors[i].setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if(reader.getString("DRIVE_MOTOR_BRAKING_MODE").equals("BRAKE")){
                for (int i = 0; i < driveMotors.length; i++) {
                    driveMotors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                }
            }
            else if(reader.getString("DRIVE_MOTOR_BRAKING_MODE").equals("FLOAT")){
                for (int i = 0; i < driveMotors.length; i++) {
                    driveMotors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                }
            }
            if(reader.getString("LEFT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[LEFT_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("LEFT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[LEFT_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            if(reader.getString("RIGHT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[RIGHT_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("RIGHT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[RIGHT_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            headingController = new PIDController(reader.getDouble("HEADING_Kp"), reader.getDouble("HEADING_Ki"), reader.getDouble("HEADING_Kd"));

        } catch(Exception e){
            Log.e(" Drive Engine Error", "Config File Read Fail: " + e.toString());
            throw new RuntimeException("Drive Engine Config Read Failed!:" + e.toString());
        }
    }
    public void brake(){
        applyMotorVelocities(new double[] {0,0});
    }
    public void kill(){
        for(int i = 0; i < driveMotors.length; i++){
            driveMotors[i].killMotorController();
        }
    }
}
