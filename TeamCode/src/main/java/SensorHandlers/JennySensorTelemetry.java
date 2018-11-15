package SensorHandlers;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Autonomous.Location;
import Autonomous.REVColorDistanceSensorController;
import MotorControllers.JsonConfigReader;

//import static Hardware.JennyWithExtendotronHardware.*;

/**
 * Created by Jeremy on 8/26/2017.
 */

/*
    A class to handle all of our sensors
 */
public class JennySensorTelemetry implements RobotSensorTelemetry {
//    private JennyWithExtendotronHardware robot;
    private JsonConfigReader reader;
    private REVColorDistanceSensorController colorSensors[] = new REVColorDistanceSensorController[2];
    private TouchSensor[] limitSwitches = new TouchSensor[4];
    //public ServoHandler jewelJoust;
//    private ImuHandler imu;
    private volatile boolean shouldRun = true;
    private long loopTime = 100;
    private long lastLoopTime = 0;
    private HardwareMap hardwareMap;
    private int tickLocationX = 0;
    private int tickLocationY = 0;
    private double startPositionX = 0;
    private double startPositionY = 0;
//    private double wheelDiameter;
//    private double ticksPerRev;
    private double ftToTicksFactor = 12;
    public static final int RAD_LIMIT = 0;
    public static final int COLOR_DISTANCE_SENSOR = 0;
    //public static final double JEWEL_JOUST_ACTIVE_POSITION = 35;
   // public static final double JEWEL_JOUST_STORE_POSITION = 100;
    public static final double START_LOCATION_X = 0;
    public static final double START_LOCATION_Y = 0;
    public static final int NO_DETECTABLE_WALL_DISTANCE = -1;
    public static final int FLAG_SPINNER = 0;
    public static final int FLAG_WAVER = 1;
    public JennySensorTelemetry(HardwareMap h, double positionX, double positionY){
        hardwareMap = h;
//        robot = new JennyWithExtendotronHardware(hardwareMap);
        startPositionX = positionX;
        startPositionY = positionY;
        colorSensors[COLOR_DISTANCE_SENSOR] = new REVColorDistanceSensorController(REVColorDistanceSensorController.type.JEWEL_SNATCH_O_MATIC, "jewelSensor", hardwareMap);
        limitSwitches[RAD_LIMIT] = hardwareMap.touchSensor.get("radLimit");
//        try {
//            reader = new JsonConfigReader(h.appContext.getAssets().open("MotorConfig/DriveMotors/NewHolonomicDriveMotorConfig.json"));
//        } catch (Exception e){
//            Log.d("Error: ", e.toString());
//        }
//        try {
//            wheelDiameter = reader.getDouble("WHEEL_DIAMETER");
//        } catch (Exception e){
//            Log.d("Error: ", e.toString());
//        }
//        try {
//            ticksPerRev = reader.getDouble("TICKS_PER_REV");
//        } catch (Exception e){
//            Log.d("Error: ", e.toString());
//        }
    }
    @Override
    public Location getLocation() {
        Location location = new Location(0, 0);
//        tickLocationX = robot.driveMotors[FRONT_LEFT_HOLONOMIC_DRIVE_MOTOR].getCurrentPositionInTicks();
//        tickLocationY = robot.driveMotors[FRONT_RIGHT_HOLONOMIC_DRIVE_MOTOR].getCurrentPositionInTicks();
        //location.updateXY(tickLocationX + startPositionX*ticksPerRev, tickLocationY + startPositionY*ticksPerRev);
        return location;
    }

    @Override
    public void setBaseLocation(Location location) {

    }

    @Override
    public double getHeading() {
        return 0;
    }

    @Override
    public double setBaseOrientation(double degree) {
        return 0;
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    @Override
    public double getVelocity() {
        return 0;
    }

    @Override
    public void startTelemetryLogging() {

    }

    @Override
    public void stopTelemetryLogging() {

    }

    @Override
    public void resetTelemetryLogging() {

    }

    @Override
    public double getDistance(DistanceUnit unit){
        double distance = NO_DETECTABLE_WALL_DISTANCE;
        double curDist = colorSensors[COLOR_DISTANCE_SENSOR].getDistance(unit);
        if(curDist > 0 && curDist < 40 && !Double.isNaN(curDist)){
            distance = curDist;
        }
        return distance;
    }

    @Override
    public boolean isPressed(int sensor){
        return limitSwitches[sensor].isPressed();
    }


    @Override
    public void stopSensorTelemetry(){
        shouldRun = false;
        stopTelemetryLogging();
    }

    public REVColorDistanceSensorController.color getColor(){
        return colorSensors[COLOR_DISTANCE_SENSOR].getColor();
    }
}
