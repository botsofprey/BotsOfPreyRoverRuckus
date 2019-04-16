package Autonomous;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by robotics on 9/22/17.
 */

/*
    A class to help us tell what color the color sensor sees
 */
public class REVColorDistanceSensorController {
    public enum type{
        JEWEL_SNATCH_O_MATIC, GLYPH_STACK_O_TRON
    }

    public enum color{
        BLUE, RED, GREY, BROWN, UNKNOWN, NOT_IN_RANGE
    }

    type colorMode;
    ColorSensor colorSensor;
    DistanceSensor distanceSensor;
    float hsvValues[] = {0F, 0F, 0F};
    final double SCALE_FACTOR = 255;
    final double UNKNOWN_DISTANCE = -1;

    public REVColorDistanceSensorController(type m, ColorSensor clrSensor, DistanceSensor distSensor){
        setColorMode(m);
        colorSensor = clrSensor;
        distanceSensor = distSensor;
    }

    public REVColorDistanceSensorController(type m, String sensor, HardwareMap h){
        this(m, h.colorSensor.get(sensor), h.get(DistanceSensor.class, sensor));
    }

    public color getColor(){
        color color = REVColorDistanceSensorController.color.UNKNOWN;
        android.graphics.Color.RGBToHSV((int) (colorSensor.red() * SCALE_FACTOR),
                (int) (colorSensor.green() * SCALE_FACTOR),
                (int) (colorSensor.blue() * SCALE_FACTOR),
                hsvValues);
        switch (colorMode){
            case JEWEL_SNATCH_O_MATIC:
                if(!Double.isNaN(distanceSensor.getDistance(DistanceUnit.CM))) {
                    if (red() && !blue()) color = REVColorDistanceSensorController.color.RED;
                    else if (blue() && !red()) color = REVColorDistanceSensorController.color.BLUE;
                }
                else {
                    color = REVColorDistanceSensorController.color.NOT_IN_RANGE;

                }
                break;
            case GLYPH_STACK_O_TRON:
                if(!Double.isNaN(distanceSensor.getDistance(DistanceUnit.CM))) {
                    if (brown() && !grey()) color = REVColorDistanceSensorController.color.BROWN;
                    else if (grey() && !brown()) color = REVColorDistanceSensorController.color.GREY;
                }
                else color = REVColorDistanceSensorController.color.NOT_IN_RANGE;
                break;
        }
        return color;
    }

    public void setColorMode(type m) {colorMode = m;}

    public boolean red(){
        boolean red = false;
        if(hsvValues[0] < 70 || hsvValues[0] > 300) red = true;
        return red;
    }
    public boolean blue(){
        boolean blue = false;
        if(hsvValues[0] < 270 && hsvValues[0] > 80) blue = true;
        return blue;
    }
    public boolean grey(){
        boolean grey = false;
        if(hsvValues[0] > 60) grey = true;
        return grey;
    }
    public boolean brown(){
        boolean brown = false;
        if(hsvValues[0] > 20 && hsvValues[0] < 60) brown = true;
        return brown;
    }
    public double getDistance(DistanceUnit unit){
        double dist = UNKNOWN_DISTANCE;
        double actualDist = distanceSensor.getDistance(unit);
        if(actualDist > 0 && !Double.isNaN(actualDist));
        else actualDist = dist;
        return actualDist;
    }
}
