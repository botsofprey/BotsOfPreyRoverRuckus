package DriveEngine;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.InputStream;

import Autonomous.HeadingVector;
import Autonomous.Location;
import MotorControllers.JsonConfigReader;
import MotorControllers.MotorController;
import MotorControllers.PIDController;
import SensorHandlers.ImuHandler;

/**
 * Created by robotics on 2/15/18.
 */

public class ThreeWheelHolonomicDriveSystem {
    public MotorController[] driveMotors = new MotorController[3];
    public static final int TOP_MOTOR = 0;
    public static final int LEFT_MOTOR = 1;
    public static final int RIGHT_MOTOR = 2;
    public PIDController headingController;
    public ImuHandler orientation;
    private HardwareMap hardwareMap;
    private Location robotLocation = new Location(0,0);

    private double maxMotorVelocity = 0;


    public ThreeWheelHolonomicDriveSystem(HardwareMap hw, String configFile){
        hardwareMap = hw;
        readConfigAndInitialize(configFile);
        double avg = 0;
        for(int i = 0; i < driveMotors.length; i ++){
            avg += driveMotors[i].getMaxSpeed();
        }
        avg /= driveMotors.length;
        maxMotorVelocity = avg;
    }

    public ThreeWheelHolonomicDriveSystem(HardwareMap hw, Location startLocation, String configFile){
        this(hw, configFile);
        robotLocation = startLocation;
    }

    public ThreeWheelHolonomicDriveSystem(HardwareMap hw, Location startLocation, double robotOrientationOffset, String configFile){
        this(hw, configFile);
        orientation.setOrientationOffset(robotOrientationOffset);
        robotLocation = startLocation;
    }

    public ThreeWheelHolonomicDriveSystem(HardwareMap hw, double robotOrientationOffset, String configFile){
        this(hw, configFile);
        orientation.setOrientationOffset(robotOrientationOffset);
    }

    /**
     * simplest way to drive the robot. This is not a heading corrected drive
     * @param heading heading relative to the front of the robot
     * @param desiredPower power from 0 to 1 representing power
     */
    public void driveOnHeadingRelativeToRobot(double heading, double desiredPower){
        double [] powers = calculatePowersToDriveOnHeading(heading, desiredPower);
        applyMotorPowers(powers);
    }


    /**
     * way to drive the robot using cartesian driving
     * @param heading the desired heading of the robot with 0 being north
     * @param movementPower power from 0 to 1 representing desired movement power
     * @param turnPower power from -1 to 1 representing desired turning power
     */
    public void cartesianDriveOnHeadingWithTurning(double heading, double movementPower, double turnPower){
        double distanceToHeading = calculateDistanceFromHeading(orientation.getOrientation(),heading);
        Log.d("Dist To Head","" + distanceToHeading);
        driveOnHeadingWithTurning(distanceToHeading, movementPower,turnPower);
    }

    /**
     * way to drive the robot using cartesian driving while reducing angular drift
     * @param heading the desired heading of the robot with 0 being north
     * @param movementPower power from 0 to 1 representing desired movement power
     * @param turnPower power from -1 to 1 representing desired turning power
     * @param initialOrientation heading of the robot when it has stopped turning
     */
    public void cartesianDriveOnHeadingWithTurningPID(double heading, double movementPower, double turnPower, double initialOrientation) {
        double distanceToHeading = calculateDistanceFromHeading(orientation.getOrientation(), heading);
        Log.d("Dist to Head", "" + distanceToHeading);
        driveOnHeadingWithTurningPID(distanceToHeading, movementPower, turnPower, initialOrientation);
    }

    /**
     * way to drive the robot and turn at same time. This is not a heading corrected drive
     * @param heading heading relative to the front of the robot
     * @param movementPower power from 0 to 1 representing the movement speed
     * @param turnPower power from -1 to 1 representing the turning speed
     */
    public void driveOnHeadingWithTurning(double heading, double movementPower, double turnPower){
        double [] movementPowers = calculatePowersToDriveOnHeading(heading, movementPower);
        double [] turningPowers = calculatePowersToTurn(turnPower);
        double [] total = new double[3];
        for(int i = 0; i < movementPowers.length; i ++){
            total[i] = movementPowers[i] + turningPowers[i];
        }
        normalizePowers(total);
        applyMotorPowers(total);
    }

    /**
     * way to drive the robot and turn at the same time while reducing angular drift
     * @param heading heading relative to the front of the roboto
     * @param movementPower power from 0 to 1 representing the movement speed
     * @param turnPower power from -1 to 1 representing the turning speed
     * @param initialOrientation heading of the robot when it has stopped turning
     */
    public void driveOnHeadingWithTurningPID(double heading, double movementPower, double turnPower, double initialOrientation){
        double [] movementPowers = calculatePowersToDriveOnHeadingPID(heading, movementPower, initialOrientation);
        double [] turningPowers = calculatePowersToTurn(turnPower);
        double [] total = new double[3];
        for(int i = 0; i < movementPowers.length; i ++){
            total[i] = movementPowers[i] + turningPowers[i];
        }
        normalizePowers(total);
        applyMotorPowers(total);
    }

    /**
     * turns the robot at the desired proportion of max
     * @param percentOfMaxTurnRate value from -1 to 1 corresponding to the max turn rate
     */
    public void turn(double percentOfMaxTurnRate) {
        double[] velocities = calculatePowersToTurn(percentOfMaxTurnRate);
        applyMotorVelocities(velocities);
    }


    /**
     * brakes the robot
     */
    public void brake(){
        applyMotorPowers(new double[] {0,0,0,0});
    }

    /**
     * normalizes all powers to -1 and 1, scales appropriately
     * @param toNormalize an array of doubles that have a wanted value of -1 to 1
     */
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

    /**
     * calculates the powers required to make the robot move on a heading
     * @param heading from 0 to 360, represents the desired heading of the robot relative to the front of the robot
     * @param desiredPower from 0 to 1 that represents the desired proportion of max velocity for the robot to move at
     * @return a double array with the calculated motor powers for each wheel
     */
    private double [] calculatePowersToDriveOnHeading(double heading, double desiredPower){
        double[] powers = new double[3];
        if(desiredPower == 0){
            for(int i = 0; i < powers.length; i ++){
                powers[i] = 0;
            }
            return powers;
        }
        powers[TOP_MOTOR] = desiredPower * Math.sin(Math.toRadians(heading));
        powers[LEFT_MOTOR] = desiredPower * Math.sin(Math.toRadians(120 + heading));
        powers[RIGHT_MOTOR] = desiredPower * Math.sin(Math.toRadians(240 + heading));
        Log.d("TopPow", "" + powers[TOP_MOTOR]);
        Log.d("LeftPow", "" + powers[LEFT_MOTOR]);
        Log.d("RightPow", "" + powers[RIGHT_MOTOR]);
        return powers;
    }

    /**
     * calculates the powers required to make the robot move on a heading
     * @param heading from 0 to 360, represents the desired heading of the robot relative to the front of the robot
     * @param desiredPower from 0 to 1 that represents the desired proportion of max velocity for the robot to move at
     * @return a double array with the calculated motor powers for each wheel
     */
    private double [] calculatePowersToDriveOnHeadingPID(double heading, double desiredPower, double initialOrientation){
        double[] powers = calculatePowersToDriveOnHeading(heading, desiredPower);
        Log.d("GetOrientation", "" + orientation.getOrientation());
        Log.d("InitOrientation", "" + initialOrientation);
        double distanceToInitialOrientation = calculateDistanceFromHeading(orientation.getOrientation(), initialOrientation);
        Log.d("DisttoInitOrientation", "" + distanceToInitialOrientation);
        double[] deltaPowers = new double[3];
        if(headingController.getSp() != normalizeAngle(initialOrientation)) headingController.setSp(normalizeAngle(initialOrientation));
        double powerToAdd = headingController.calculatePID(normalizeAngle(orientation.getOrientation()));
//        if(distanceToInitialOrientation > 0) {
            deltaPowers[TOP_MOTOR] = powerToAdd;
            deltaPowers[LEFT_MOTOR] = powerToAdd;
            deltaPowers[RIGHT_MOTOR] = powerToAdd;
//        } else {
//            deltaPowers[TOP_MOTOR] = -powerToAdd;
//            deltaPowers[LEFT_MOTOR] = -powerToAdd;
//            deltaPowers[RIGHT_MOTOR] = -powerToAdd;
//        }
        for(int i = 0; i < powers.length; i++) {
            powers[i] += deltaPowers[i];
        }
        normalizePowers(powers);
        return powers;
    }

    /**
     * calculates the powers of each motor to turn at the desired rate
     * @param desiredTurnRateOfMax a value from -1 to 1 that represents the rate of max turn to turn at
     * @return a double array with the calculated motor powers for each wheel
     */
    private double [] calculatePowersToTurn(double desiredTurnRateOfMax){
        double[] powers = new double[3];
        if(desiredTurnRateOfMax == 0){
            for(int i = 0; i < powers.length; i ++){
                powers[i] = 0;
            }
            return powers;
        }
        powers[TOP_MOTOR] = desiredTurnRateOfMax;
        powers[LEFT_MOTOR] = desiredTurnRateOfMax;
        powers[RIGHT_MOTOR] = desiredTurnRateOfMax;
        for(int i = 0; i < powers.length; i ++){
            if(Double.isNaN(powers[i])) powers[i] = 0;
        }
        return powers;
    }

    /**
     * sets each motor at the desired motor power, -1 to 1 corresponding to max velocities
     * @param powers a double array of length 4 with values -1 to 1
     */
    private void applyMotorPowers(double [] powers){
        normalizePowers(powers);
        double [] velocities = new double[4];
        for(int i = 0; i < powers.length; i ++){
            //Log.d("Motor " + i + "Power", "" + powers[i]);
            velocities[i] = powers[i]*maxMotorVelocity;
        }
        applyMotorVelocities(velocities);
    }

    /**
     * sets each motor to turn at the desired velocity
     * @param velocities a double array of length 4 with the desired motor velocities to set the motors at
     */
    private void applyMotorVelocities(double [] velocities){
        driveMotors[TOP_MOTOR].setInchesPerSecondVelocity(velocities[TOP_MOTOR]);
        driveMotors[LEFT_MOTOR].setInchesPerSecondVelocity(velocities[LEFT_MOTOR]);
        driveMotors[RIGHT_MOTOR].setInchesPerSecondVelocity(velocities[RIGHT_MOTOR]);
    }


    /**
     * calculates the distance between two angle. For example, if curAngle was 350 deg and target was 45 deg, 55 degs would be returned
     * @param curAngle current or base angle to compare from. Should be a value from 0 to 360
     * @param targetAngle angle to compare to, should be a value from 0 to 360;
     * @return the distance from the curAngle to the target angle. Is + if target angle is to the Right, - if to the left
     */
    public double calculateDistanceFromHeading(double curAngle, double targetAngle){
        double distanceFromHeading = targetAngle - curAngle;
        Log.d("RawDist To Heading", "" + distanceFromHeading);
        return normalizeAngle(distanceFromHeading);
    }


    /**
     * normalizes a degree to a value between -180 and 180
     * @param angle angle to normalize, can be any degree
     * @return the normalized angle value from -180 to 180
     */
    private double normalizeAngle(double angle){
        angle %= 360;
        if(angle > 180) angle -= 360;
        else if(angle < -180) angle += 360;
        return angle;
    }


    /**
     * kills all parts of the robot for a safe shutdown
     */
    public void kill(){
        orientation.stopIMU();
        for (MotorController driveMotor : driveMotors) {
            driveMotor.killMotorController();
        }
    }

    /**
     * reads the robot config file and initializes all motors using applicable data
     * @param file the location of the robot config file, in the assets folder
     */
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
            driveMotors[TOP_MOTOR] = new MotorController(reader.getString("TOP_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
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
            if(reader.getString("TOP_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[TOP_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("TOP_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[TOP_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
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
            orientation = new ImuHandler(reader.getString("IMU_NAME"), reader.getDouble("ORIENTATION_OFFSET"), hardwareMap);
            headingController = new PIDController(reader.getDouble("HEADING_Kp"), reader.getDouble("HEADING_Ki"), reader.getDouble("HEADING_Kd"));
            headingController.setIMax(reader.getDouble("HEADING_Ki_MAX"));
        } catch(Exception e){
            Log.e(" Drive Engine Error", "Config File Read Fail: " + e.toString());
            throw new RuntimeException("Drive Engine Config Read Failed!:" + e.toString());
        }
    }
}
