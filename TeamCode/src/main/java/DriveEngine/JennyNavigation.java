package DriveEngine;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
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
 * Created by Jeremy on 8/23/2017.
 */

/*
    The base class for every opmode --- it sets up our drive system and contains all it's funcitons
 */
public class JennyNavigation extends Thread{
    public MotorController[] driveMotors = new MotorController[4];
    public static final int FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR = 0;
    public static final int FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR = 1;
    public static final int BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR = 2;
    public static final int BACK_LEFT_HOLONOMIC_DRIVE_MOTOR = 3;
    public static final double LOCATION_DISTANCE_TOLERANCE = 1;
    public static final long DEFAULT_DELAY_MILLIS = 10;
    private volatile long threadDelayMillis = 10;
    public volatile double robotHeading = 0;
    private volatile double [] lastMotorPositionsInInches = {0,0,0,0};
    public PIDController headingController, turnController, cameraPIDController;
    private volatile Location myLocation;
    private volatile HeadingVector [] wheelVectors = new HeadingVector[4];
    private volatile HeadingVector robotMovementVector = new HeadingVector();

    public ImuHandler orientation;
    private double orientationOffset = 0;
    private volatile boolean shouldRun = true;

    private final double HEADING_THRESHOLD = 1;
    private final double WHEEL_BASE_RADIUS = 20;
    private final double FL_WHEEL_HEADING_OFFSET = 45;
    private final double FR_WHEEL_HEADING_OFFSET = 315;
    private final double BR_WHEEL_HEADING_OFFSET = 45;
    private final double BL_WHEEL_HEADING_OFFSET = 315;
    private double acceleration = 0;
    private HardwareMap hardwareMap;

    public JennyNavigation(HardwareMap hw, Location startLocation, double robotOrientationOffset, String configFile) throws Exception{
        hardwareMap = hw;
        initializeUsingConfigFile(configFile);
        orientationOffset = robotOrientationOffset;
        orientation = new ImuHandler("imu", orientationOffset, hardwareMap);
        myLocation = new Location(startLocation.getX(),startLocation.getY());
        for(int i = 0; i < wheelVectors.length; i++){
            wheelVectors[i] = new HeadingVector();
        }
        for(int i = 0; i < lastMotorPositionsInInches.length; i ++){
            lastMotorPositionsInInches[i] = driveMotors[i].getInchesFromStart();
        }
        robotMovementVector = new HeadingVector();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < driveMotors.length; i++) {
                    Log.d("Inch from start", Integer.toString(i) + ": " + driveMotors[i].getInchesFromStart());
                }
                while (shouldRun) {
                    try {
                        updateData();
                    }
                    catch (Exception e){
                        shouldRun = false;
                        throw new RuntimeException(e);
                    }
                    safetySleep(threadDelayMillis);
                }
            }
        }).start();

    }

    public void setOrientationOffset(double offset){
        orientationOffset = offset;
        orientation.setOrientationOffset(orientationOffset);
    }

    private void updateLastMotorPositionsInInches(){
        for (int i = 0; i < driveMotors.length; i++){
            lastMotorPositionsInInches[i] = driveMotors[i].getInchesFromStart();
        }
    }

    private void updateHeading(){
        robotHeading = (orientation.getOrientation());
    }

    public Location getRobotLocation(){
        return new Location(myLocation.getX(),myLocation.getY());
    }

    private void updateLocation(){
        HeadingVector travelVector = wheelVectors[0].addVectors(wheelVectors);
        travelVector = new HeadingVector(travelVector.x()/2, travelVector.y()/2);
        double headingOfRobot = travelVector.getHeading();
        double magnitudeOfRobot = travelVector.getMagnitude();
        double actualHeading = (headingOfRobot + robotHeading)%360;
        robotMovementVector.calculateVector(actualHeading, magnitudeOfRobot);
        double deltaX = robotMovementVector.x();
        double deltaY = robotMovementVector.y();
        myLocation.addXY(deltaX, deltaY);
        Log.d("Location","X:" + myLocation.getX() + " Y:" + myLocation.getY());

    }

    public void setLocation(Location loc) {
        myLocation = new Location(loc.getX(), loc.getY());
    }

    private void updateData(){

        updateHeading();
        wheelVectors = getWheelVectors();
        updateLocation();
    }

    private void safetySleep(long time){
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < time && shouldRun);
    }

    public void setThreadDelayMillis(long delayMillis){
        threadDelayMillis = delayMillis;
    }

    public double getOrientation(){
        return robotHeading;
    }

    public void initializeUsingConfigFile(String file){
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
            driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = new MotorController(reader.getString("FRONT_LEFT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
            driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = new MotorController(reader.getString("FRONT_RIGHT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
            driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = new MotorController(reader.getString("BACK_LEFT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
            driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = new MotorController(reader.getString("BACK_RIGHT_MOTOR_NAME"), "MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json", hardwareMap);
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
            if(reader.getString("FRONT_LEFT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("FRONT_LEFT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            if(reader.getString("FRONT_RIGHT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("FRONT_RIGHT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            if(reader.getString("BACK_RIGHT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("BACK_RIGHT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            if(reader.getString("BACK_LEFT_MOTOR_DIRECTION").equals("REVERSE")) {
                driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.REVERSE);
            }
            else if(reader.getString("BACK_LEFT_MOTOR_DIRECTION").equals("FORWARD")) {
                driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setDirection(DcMotorSimple.Direction.FORWARD);
            }
            headingController = new PIDController(reader.getDouble("HEADING_Kp"), reader.getDouble("HEADING_Ki"), reader.getDouble("HEADING_Kd"));
            headingController.setIMax(reader.getDouble("HEADING_Ki_MAX"));
            turnController = new PIDController(reader.getDouble("TURN_Kp"), reader.getDouble("TURN_Ki"), reader.getDouble("TURN_Kd"));
            turnController.setIMax(reader.getDouble("TURN_Ki_MAX"));

        } catch(Exception e){
            Log.e(" Drive Engine Error", "Config File Read Fail: " + e.toString());
            throw new RuntimeException("Drive Engine Config Read Failed!:" + e.toString());
        }
    }
    public void correctedDriveOnHeadingIMU(double heading, double desiredVelocity, LinearOpMode mode){
        correctedDriveOnHeadingIMU(heading,desiredVelocity,DEFAULT_DELAY_MILLIS,mode);
    }



    public void correctedDriveOnHeadingIMU(double heading, double desiredVelocity, long delayTimeMillis, LinearOpMode mode) {
        desiredVelocity = Math.abs(desiredVelocity);
        double curOrientation = orientation.getOrientation();
        double distanceFromHeading = 0;
        distanceFromHeading = heading - curOrientation; // as in -1 if heading is 0 and current orientation is 1
        if(distanceFromHeading > 180) distanceFromHeading -= 360;
        if(distanceFromHeading < -180) distanceFromHeading += 360;
        if(curOrientation > 315 || curOrientation <= 45){
            headingController.setSp(0);
        }
        else if(curOrientation > 45 && curOrientation <= 135){
            headingController.setSp(90);
        }
        else if(curOrientation > 135 && curOrientation <= 225){
            headingController.setSp(180);
        }
        else if(curOrientation > 225 && curOrientation <= 315){
            headingController.setSp(270);
        }
        double distanceFromSetPoint = headingController.getSp() - curOrientation;
        if(distanceFromSetPoint < -180) distanceFromSetPoint += 360;
        else if(distanceFromSetPoint > 180) distanceFromSetPoint -= 360;
        double deltaVelocity = headingController.calculatePID(distanceFromSetPoint + headingController.getSp()); //isue with this line...
        if(distanceFromHeading < 0) distanceFromHeading += 360;
        else if(distanceFromHeading > 360) distanceFromHeading -= 360;
        double [] velocities = determineMotorVelocitiesToDriveOnHeading(heading - curOrientation,desiredVelocity);

        if(distanceFromHeading > 315 || distanceFromHeading <= 45){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
        }

        else if(distanceFromHeading > 45 && distanceFromHeading <= 135){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
        }

        else if(distanceFromHeading > 135 && distanceFromHeading <= 225){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
        }

        else if(distanceFromHeading > 225 && distanceFromHeading <= 315){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
        }

        applyMotorVelocities(velocities);
        mode.sleep(delayTimeMillis);
    }

    public void correctedDriveOnHeadingIMURotation(double heading, double desiredVelocity, long delayTimeMillis, LinearOpMode mode) {
        desiredVelocity = Math.abs(desiredVelocity);
        double curOrientation = orientation.getOrientation();
        double turnCorrection = turnController.calculatePID(curOrientation);
        if(Double.isNaN(turnCorrection)) turnCorrection = 0;
        Log.d("Delta Velocity:", "" + turnCorrection);
        double [] velocities = determineMotorVelocitiesToDriveOnHeading(heading/* - curOrientation*/, desiredVelocity);

        Log.d("Initial Velocities:", "-----------");
        for(int i = 0; i < velocities.length; i++) {
            Log.d("Motor " + " Velocity", "" + velocities[i]);
        }

        double [] rotationalCorrections = calculateTurnVelocities(turnCorrection);
        for(int i = 0; i < driveMotors.length; i++) {
            velocities[i] += rotationalCorrections[i];
        }

        Log.d("Final Velocities:", "-----------");
        for(int i = 0; i < velocities.length; i++) {
            Log.d("Motor " + " Velocity", "" + velocities[i]);
        }

        applyMotorVelocities(velocities);
        mode.sleep(delayTimeMillis);
    }

    public void driveOnHeadingPID(double heading, double desiredVelocity, LinearOpMode mode) {
        driveOnHeadingPID(heading, desiredVelocity, DEFAULT_DELAY_MILLIS, mode);
    }

    // Note: set turn Sp prior to calling
    public void driveOnHeadingPID(double heading, double desiredVelocity, long delayTimeMillis, LinearOpMode mode) {
        desiredVelocity = Math.abs(desiredVelocity);
        double curOrientation = orientation.getOrientation();
//        if(curOrientation > 315 || curOrientation <= 45){
//            headingController.setSp(0);
//        }
//        else if(curOrientation > 45 && curOrientation <= 135){
//            headingController.setSp(90);
//        }
//        else if(curOrientation > 135 && curOrientation <= 225){
//            headingController.setSp(180);
//        }
//        else if(curOrientation > 225 && curOrientation <= 315){
//            headingController.setSp(270);
//        }
//        double distanceFromSetPoint = turnController.getSp() - curOrientation + orientation.getOrientationOffset();
//        if(distanceFromSetPoint < -180) distanceFromSetPoint += 360;
//        else if(distanceFromSetPoint > 180) distanceFromSetPoint -= 360;
        double turnCorrection = turnController.calculatePID(curOrientation/*distanceFromSetPoint + headingController.getSp()*/); //issue with this line...
        if(Double.isNaN(turnCorrection)) turnCorrection = 0;
        Log.d("Delta Velocity:", "" + turnCorrection);
        double [] velocities = determineMotorVelocitiesToDriveOnHeading(heading, desiredVelocity);

        Log.d("Initial Velocities:", "-----------");
        for(int i = 0; i < velocities.length; i++) {
            Log.d("Motor " + " Velocity", "" + velocities[i]);
        }

        double [] rotationalCorrections = calculateTurnVelocities(turnCorrection);
        for(int i = 0; i < driveMotors.length; i++) {
            velocities[i] += rotationalCorrections[i];
        }
//        if(heading > 315 || heading <= 45){
//            Log.d("Wheels Affected", "-Left+Right");
//            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity; // 0
//            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity; // 1
//            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity; // 3
//            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity; // 2
//        }
//
//        else if(heading > 45 && heading <= 135){
//            Log.d("Wheels Affected", "-Front+Back");
//            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity; // 0
//            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity; // 1
//            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity; // 3
//            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity; // 2
//        }
//
//        else if(heading > 135 && heading <= 225){
//            Log.d("Wheels Affected", "-Right+Left");
//            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
//            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
//            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
//            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
//        }
//
//        else if(heading > 225 && heading <= 315){
//            Log.d("Wheels Affected", "-Back+Front");
//            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
//            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
//            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] -= deltaVelocity;
//            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] += deltaVelocity;
//        }

        Log.d("Final Velocities:", "-----------");
        for(int i = 0; i < velocities.length; i++) {
            Log.d("Motor " + " Velocity", "" + velocities[i]);
        }

        applyMotorVelocities(velocities);
        mode.sleep(delayTimeMillis);
    }

    public void driveDistance(double distanceInInches, double heading, double desiredVelocity, LinearOpMode mode){
        distanceInInches = Math.abs(distanceInInches);
        double distanceTraveled = 0;
        double [] motorPositionsInches = getMotorPositionsInches();
        double [] startPositionsInches = motorPositionsInches;
        double [] deltaInches;
        double averagePosition = 0;
        if(heading >= 360) heading -= 360;
        else if(heading < 0) heading += 360;
        double curOrientation = orientation.getOrientation();
        turnController.setSp(curOrientation);
        while(distanceTraveled < distanceInInches && mode.opModeIsActive()){
            //from our motor position, determine location
            driveOnHeadingPID(heading,desiredVelocity,0, mode);
            motorPositionsInches = getMotorPositionsInches();
            deltaInches = new double[4];
            averagePosition = 0;
            if(heading == 45 || heading == 225){
                deltaInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]);
                deltaInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
                averagePosition += deltaInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] + deltaInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR];
                averagePosition /= 2.0;
                distanceTraveled = averagePosition;
            } else if(heading == 135 || heading == 315){
                deltaInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
                deltaInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]);
                averagePosition += deltaInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] + deltaInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR];
                averagePosition /= 2.0;
                distanceTraveled = averagePosition;
            } else {
                for (int i = 0; i < motorPositionsInches.length; i++) {
                    deltaInches[i] = Math.abs(motorPositionsInches[i] - startPositionsInches[i]);
                }
                for (double i : deltaInches) {
                    averagePosition += i;
                }
                averagePosition /= (double) deltaInches.length;
                distanceTraveled = averagePosition / Math.sin(Math.toRadians(45));
            }
            Log.d("Distance Travelled", "" + distanceTraveled);
        }
        brake();
        Log.d("Location", getRobotLocation().toString());
        for(int i = 0; i < driveMotors.length; i++) {
            Log.d("Motor" + i, driveMotors[i].getInchesFromStart()+"");
        }
    }

    public void driveDistanceAccelerationBased(double distanceInInches, double heading, double desiredVelocity, LinearOpMode mode) {

    }

    public void driveDistanceNonCorrected(double distanceInInches, double heading, double desiredVelocity, LinearOpMode mode){
        distanceInInches = Math.abs(distanceInInches);
        double distanceTraveled = 0;
        double [] motorPositionsInches = getMotorPositionsInches();
        double [] startPositionsInches = motorPositionsInches;
        double [] deltaInches;
        double averagePosition = 0;
        while(distanceTraveled < distanceInInches && mode.opModeIsActive()){
            //from our motor position, determine location
            driveOnHeading((int)(heading + .5),desiredVelocity);
            motorPositionsInches = getMotorPositionsInches();
            deltaInches = new double[4];
            averagePosition = 0;
            if(heading == 45 || heading == 225){
                deltaInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]);
                deltaInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
                averagePosition += deltaInches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] + deltaInches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR];
                averagePosition /= 2;
                distanceTraveled = averagePosition;
            } else if(heading == 135 || heading == 315){
                deltaInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
                deltaInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(motorPositionsInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] - startPositionsInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]);
                averagePosition += deltaInches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] + deltaInches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR];
                averagePosition /= 2;
                distanceTraveled = averagePosition;
            } else {
                for (int i = 0; i < motorPositionsInches.length; i++) {
                    deltaInches[i] = Math.abs(motorPositionsInches[i] - startPositionsInches[i]);
                }
                for (double i : deltaInches) {
                    averagePosition += i;
                }
                averagePosition /= (double) deltaInches.length;
                distanceTraveled = averagePosition / Math.sin(Math.toRadians(45));
            }
        }
        brake();
    }

    long [] getMotorPositionsTicks(){
        long [] positions = new long[4];
        positions[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        positions[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        positions[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        positions[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentTick();
        return  positions;
    }

    public double [] getMotorPositionsInches(){
        double [] inches = new double [4];
        long [] ticks = getMotorPositionsTicks();
        inches[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].convertTicksToInches(ticks[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]));
        inches[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].convertTicksToInches(ticks[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]));
        inches[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].convertTicksToInches(ticks[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]));
        inches[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = Math.abs(driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].convertTicksToInches(ticks[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]));
        return inches;
    }

    double [] determineMotorVelocitiesToDriveOnHeading(double heading, double desiredVelocity) {
        double[] velocities = new double[4];
        velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredVelocity * Math.sin(Math.toRadians(heading + 45));
        velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = desiredVelocity * Math.cos(Math.toRadians(heading + 45));
        velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = desiredVelocity * Math.sin(Math.toRadians(heading + 45));
        velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredVelocity * Math.cos(Math.toRadians(heading + 45));
        return velocities;
    }

    public void driveOnHeading(double heading, double desiredVelocity) {
        applyMotorVelocities(determineMotorVelocitiesToDriveOnHeading(heading, desiredVelocity));
    }

    public void driveOnHeadingWithTurning(double heading, double movementPower, double turnPower){
        double [] movementPowers = calculatePowersToDriveOnHeading(heading, movementPower);
        double [] turningPowers = calculatePowersToTurn(turnPower);
        double [] total = new double[4];
        for(int i = 0; i < movementPowers.length; i ++){
            total[i] = movementPowers[i] + turningPowers[i];
        }
        normalizePowers(total);
        applyMotorPowers(total);
    }

    private double [] calculatePowersToDriveOnHeading(double heading, double desiredPower){
        double[] powers = new double[4];
        if(desiredPower == 0){
            for(int i = 0; i < powers.length; i ++){
                powers[i] = 0;
            }
            return powers;
        }
        powers[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredPower * Math.sin(Math.toRadians(heading + 45));
        powers[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = desiredPower * Math.cos(Math.toRadians(heading + 45));
        powers[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = desiredPower * Math.sin(Math.toRadians(heading + 45));
        powers[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredPower * Math.cos(Math.toRadians(heading + 45));
        //Log.d("MotorPow","" + powers[0]);
        return powers;
    }

    private double [] calculatePowersToTurn(double desiredTurnRateOfMax){
        double[] powers = new double[4];
        if(desiredTurnRateOfMax == 0){
            for(int i = 0; i < powers.length; i ++){
                powers[i] = 0;
            }
            return powers;
        }
        powers[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredTurnRateOfMax;
        powers[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -desiredTurnRateOfMax;
        powers[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = desiredTurnRateOfMax;
        powers[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -desiredTurnRateOfMax;
        for(int i = 0; i < powers.length; i ++){
            if(Double.isNaN(powers[i])) powers[i] = 0;
        }
        return powers;
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

    public void relativeDriveOnHeadingWithTurning(double heading, double driveVelocity, double magnitudeOfTurn){
        driveVelocity = Math.abs(driveVelocity);
        double curOrientation = orientation.getOrientation();
        double distanceFromHeading = 0;
        distanceFromHeading = heading - curOrientation; // as in -1 if heading is 0 and current orientation is 1
        if(distanceFromHeading > 180) distanceFromHeading -= 360;
        if(distanceFromHeading < -180) distanceFromHeading += 360;
        if(curOrientation > 315 || curOrientation <= 45){
            headingController.setSp(0);
        }
        else if(curOrientation > 45 && curOrientation <= 135){
            headingController.setSp(90);
        }
        else if(curOrientation > 135 && curOrientation <= 225){
            headingController.setSp(180);
        }
        else if(curOrientation > 225 && curOrientation <= 315){
            headingController.setSp(270);
        }
        double distanceFromSetPoint = headingController.getSp() - curOrientation;
        if(distanceFromSetPoint < -180) distanceFromSetPoint += 360;
        else if(distanceFromSetPoint > 180) distanceFromSetPoint -= 360;
        if(distanceFromHeading < 0) distanceFromHeading += 360;
        else if(distanceFromHeading > 360) distanceFromHeading -= 360;
        double [] headingVelocities = determineMotorVelocitiesToDriveOnHeading(heading - curOrientation,driveVelocity);
        //real quick, make distance from heading always positive
        double [] turnVelocities =  calculateTurnVelocitiesRelativeToMax(magnitudeOfTurn);
        double [] finalVelocities = new double[4];
        for(int i = 0; i < finalVelocities.length; i ++){
            finalVelocities[i] = turnVelocities[i] + headingVelocities[i];
        }
        //look for max and min values
        double maxValue = finalVelocities[0];
        double minValue = finalVelocities[0];
        for(int i = 1; i < finalVelocities.length; i ++){
            if(finalVelocities[i] > maxValue){
                maxValue = finalVelocities[i];
            }
            else if(finalVelocities[i] < minValue){
                minValue = finalVelocities[i];
            }
        }
        //scale all motor powers to correspond with maxVelocities
        double scaleValue = 1;
        if(Math.abs(maxValue) >= Math.abs(minValue)){
            if(maxValue > driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getMaxSpeed()){
                scaleValue = driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getMaxSpeed()/maxValue;
            }
        }
        else if(Math.abs(maxValue) < Math.abs(minValue)){
            if(Math.abs(minValue) > driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getMaxSpeed()){
                scaleValue = Math.abs(driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getMaxSpeed()/Math.abs(minValue));
            }
        }

        if(scaleValue != 1){
            for(int i = 0; i < finalVelocities.length; i ++){
                finalVelocities[i] *= scaleValue;
                Log.d("final velocity" + i,"" + finalVelocities[i]);
            }
        }

        applyMotorVelocities(finalVelocities);
    }

    public double [] calculateTurnVelocitiesRelativeToMax(double percentOfMax){
        double[] velocities = new double[4];
        double maxWheelVelocity = driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getMaxSpeed();
        if(Double.isNaN(percentOfMax)) percentOfMax = 0;
        double velocity = maxWheelVelocity * percentOfMax;
        //double velocity = rps*WHEEL_BASE_RADIUS*2.0*Math.PI;
        if(Double.isNaN(velocity)){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = 0;
        }
        else {
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = velocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -velocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = velocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -velocity;
        }
        return velocities;
    }

    public double [] calculateTurnVelocities(double rps){
        double[] velocities = new double[4];
        if(Double.isNaN(rps)) rps = 0;
        double velocity = rps*WHEEL_BASE_RADIUS*2.0*Math.PI;
        if(Double.isNaN(velocity)){
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = 0;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = 0;
        }
        else {
            velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR] = velocity;
            velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -velocity;
            velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR] = velocity;
            velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR] = -velocity;
        }
        return velocities;
    }
    public void turn(double rps) {
        double[] velocities = calculateTurnVelocities(rps);
        applyMotorVelocities(velocities);
    }

    public double getDistanceFromHeading(double targetAngle){
        double distanceFromHeading = orientation.getOrientation() - targetAngle;
        if(distanceFromHeading > 180) distanceFromHeading -= 360;
        else if(distanceFromHeading < -180) distanceFromHeading += 360;
        return distanceFromHeading;
    }

    public void turnToHeading(double desiredHeading, LinearOpMode mode){
        turnController.setSp(0);
        double curHeading = orientation.getOrientation() % 360;
        double rps;
        double distanceFromHeading = 0;
        distanceFromHeading = desiredHeading - curHeading;
        if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
        else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
        if(distanceFromHeading >= 0 && distanceFromHeading <= 180){
            while(Math.abs(distanceFromHeading) > HEADING_THRESHOLD && mode.opModeIsActive()){
                //heading always positive
                rps = turnController.calculatePID(distanceFromHeading);
                turn(-rps);
                mode.sleep(5);
                curHeading = orientation.getOrientation();
                distanceFromHeading = desiredHeading - curHeading;
                if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
                else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
            }
            brake();
        }

        else if((distanceFromHeading <= 360 && distanceFromHeading >= 180) || distanceFromHeading < 0){
            while(Math.abs(distanceFromHeading) > HEADING_THRESHOLD && mode.opModeIsActive()){
                //heading always positive
                rps = turnController.calculatePID(distanceFromHeading);
                turn(-rps);
                mode.sleep(5);
                curHeading = orientation.getOrientation();
                distanceFromHeading = desiredHeading - curHeading;
                if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
                else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
            }
            brake();
        }

    }

    public void turnToHeading(double desiredHeading, double tolerance, LinearOpMode mode){
        turnController.setSp(0);
        double curHeading = orientation.getOrientation() % 360;
        double rps;
        double distanceFromHeading = 0;
        distanceFromHeading = desiredHeading - curHeading;
        if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
        else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
        if(distanceFromHeading >= 0 && distanceFromHeading <= 180){
            while(Math.abs(distanceFromHeading) > tolerance && mode.opModeIsActive()){
                //heading always positive
                rps = turnController.calculatePID(distanceFromHeading);
                turn(-rps);
                mode.sleep(5);
                curHeading = orientation.getOrientation();
                distanceFromHeading = desiredHeading - curHeading;
                if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
                else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
            }
            brake();
        }

        else if((distanceFromHeading <= 360 && distanceFromHeading >= 180) || distanceFromHeading < 0){
            while(Math.abs(distanceFromHeading) > tolerance && mode.opModeIsActive()){
                //heading always positive
                rps = turnController.calculatePID(distanceFromHeading);
                turn(-rps);
                mode.sleep(5);
                curHeading = orientation.getOrientation();
                distanceFromHeading = desiredHeading - curHeading;
                if(distanceFromHeading > 180) distanceFromHeading = distanceFromHeading - 360;
                else if(distanceFromHeading < -180) distanceFromHeading = 360 + distanceFromHeading;
            }
            brake();
        }

    }

    public void setDrivePower(double power){
        double[] powers = new double[4];
        for(int i = 0; i < 4; i++){
            powers[i] = power;
        }
        applyMotorPowers(powers);
    }

    public void applyMotorVelocities(double [] velocities){
        driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(velocities[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(velocities[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(velocities[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setInchesPerSecondVelocity(velocities[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
    }

    public void applyMotorPowers(double [] powers){
        driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].setMotorPower(powers[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].setMotorPower(powers[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].setMotorPower(powers[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        driveMotors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].setMotorPower(powers[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
    }

    public void brake(){
        applyMotorVelocities(new double []{0,0,0,0});
    }

    public void stopNavigation(){
        shouldRun = false;
        for(int i =0; i < driveMotors.length; i ++){
            driveMotors[i].killMotorController();
        }
        orientation.stopIMU();
    }

    private void driveToLocation(Location startLocation, Location targetLocation, double desiredSpeed, LinearOpMode mode){
//        double distanceToTravel = startLocation.distanceToLocation(targetLocation);
//        double prevDistance = 0;
//        double deltaX;
//        double deltaY;
//        double heading;
//        double startHeading = restrictAngle(orientation.getOrientation(), targetLocation.getHeading(), mode);
//        Log.d("Start heading", startHeading + "");
//        double totalDistanceToTravel = distanceToTravel;
//        while(mode.opModeIsActive() && distanceToTravel > LOCATION_DISTANCE_TOLERANCE /*&& prevDistance - distanceToTravel < LOCATION_DISTANCE_TOLERANCE*4*/) {
//            prevDistance = distanceToTravel;
//            distanceToTravel = startLocation.distanceToLocation(targetLocation); // start location is updated from the robot's current location (myLocation)
//            Log.d("Distance to travel", "" + distanceToTravel);
//            deltaX = targetLocation.getX() - startLocation.getX();
//            deltaY = targetLocation.getY() - startLocation.getY();
//            heading = Math.toDegrees(Math.atan2(deltaY, deltaX)) - 90;
//            heading = 360 - heading;
//            heading = (heading - orientation.getOrientation()) % 360;
//            if (heading >= 360) heading -= 360;
//            if (heading < 0) heading += 360;
//            Log.d("Heading", ""+heading);
//            double curOrientation = restrictAngle(orientation.getOrientation(), 180, mode);
//            double fracOfDistance = distanceToTravel / (totalDistanceToTravel);
//            if(fracOfDistance > 1) fracOfDistance = 1;
//            turnController.setSp(/*(1-fracOfDistance)*(targetLocation.getHeading()) + (fracOfDistance)*(startHeading)*/targetLocation.getHeading());
//            correctedDriveOnHeadingIMU(heading - curOrientation, desiredSpeed, 10, mode);
//        }
//        brake();
        driveToLocation(startLocation, targetLocation, desiredSpeed, 10000, mode);
    }

    private void driveToLocation(Location startLocation, Location targetLocation, double desiredSpeed, double secToQuit, LinearOpMode mode){
        double distanceToTravel = startLocation.distanceToLocation(targetLocation);
        double prevDistance = 0;
        double deltaX;
        double deltaY;
        double heading;
        double startHeading = restrictAngle(orientation.getOrientation(), targetLocation.getHeading(), mode);
        Log.d("Start heading", startHeading + "");
        double totalDistanceToTravel = distanceToTravel;
        long startTime = System.currentTimeMillis();
        while(mode.opModeIsActive() && distanceToTravel > LOCATION_DISTANCE_TOLERANCE && System.currentTimeMillis() - startTime < secToQuit*1000) {
            prevDistance = distanceToTravel;
            distanceToTravel = startLocation.distanceToLocation(targetLocation); // start location is updated from the robot's current location (myLocation)
            Log.d("Distance to travel", "" + distanceToTravel);
            deltaX = targetLocation.getX() - startLocation.getX();
            deltaY = targetLocation.getY() - startLocation.getY();
            heading = Math.toDegrees(Math.atan2(deltaY, deltaX)) - 90;
            heading = 360 - heading;
            heading = (heading - orientation.getOrientation()) % 360;
            if (heading >= 360) heading -= 360;
            if (heading < 0) heading += 360;
            Log.d("Heading", ""+heading);
            double curOrientation = restrictAngle(orientation.getOrientation(), 180, mode);
            double fracOfDistance = distanceToTravel / (totalDistanceToTravel);
            if(fracOfDistance > 1) fracOfDistance = 1;
            turnController.setSp(/*(1-fracOfDistance)*(targetLocation.getHeading()) + (fracOfDistance)*(startHeading)*/targetLocation.getHeading());
            correctedDriveOnHeadingIMU(heading - curOrientation, desiredSpeed, 10, mode);
        }
        brake();
    }

    public void driveToLocation(Location targetLocation, double desiredSpeed, LinearOpMode mode){
        driveToLocation(myLocation, targetLocation, desiredSpeed, mode);
    }

    public void driveToLocation(Location targetLocation, double desiredSpeed, double secToQuit, LinearOpMode mode){
        driveToLocation(myLocation, targetLocation, desiredSpeed, secToQuit, mode);
    }

    public void navigatePath(Location[] path, double desiredSpeed, LinearOpMode mode) {
        for(int i = 0; i < path.length; i++) {
            driveToLocation(path[i], desiredSpeed, mode);
        }
    }

    public void navigatePath(Location[] path, double desiredSpeed, double[] secToQuit, LinearOpMode mode) {
        for(int i = 0; i < path.length; i++) {
            driveToLocation(path[i], desiredSpeed, secToQuit[i], mode);
        }
    }

    public HeadingVector[] getWheelVectors(){
        double [] deltaWheelPositions = {0,0,0,0};
        for(int i = 0; i < driveMotors.length; i ++){
            double a = driveMotors[i].getInchesFromStart();
            deltaWheelPositions[i] = a - lastMotorPositionsInInches[i];
            lastMotorPositionsInInches[i] = a;
        }
        //updateLastMotorPositionsInInches();
        HeadingVector [] vectors = new HeadingVector[4];
        for(int i = 0; i < vectors.length; i++){
            vectors[i] = new HeadingVector();
        }
        vectors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].calculateVector(FL_WHEEL_HEADING_OFFSET,deltaWheelPositions[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        vectors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].calculateVector(FR_WHEEL_HEADING_OFFSET,deltaWheelPositions[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
        vectors[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR].calculateVector(BL_WHEEL_HEADING_OFFSET,deltaWheelPositions[BACK_LEFT_HOLONOMIC_DRIVE_MOTOR]);
        vectors[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR].calculateVector(BR_WHEEL_HEADING_OFFSET,deltaWheelPositions[BACK_RIGHT_HOLONOMIC_DRIVE_MOTOR]);
        return vectors;
    }

    private double restrictAngle(double angleToChange, double referenceAngle, LinearOpMode mode) {
        while(mode.opModeIsActive() && angleToChange < referenceAngle - 180) angleToChange += 360;
        while (mode.opModeIsActive() && angleToChange > referenceAngle + 180) angleToChange -= 360;
        return angleToChange;
    }
}
