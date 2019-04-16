package Actions.HardwareWrappers;

import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by robotics on 11/7/17.
 */

/*
    A general class for anything that uses a combination of two servos to grab something
 */
public class ServoGrippers {
    Servo myServos[] = new Servo[2];
    public final int LEFT_SERVO = 0;
    public final int RIGHT_SERVO = 1;
    public double angleOffset[] = {0,0};
    private double minAngles[] = {0,0};
    private double maxAngles[] = {0,0};

    public ServoGrippers(Servo servos[], double minAngle[], double maxAngle[], double offsets[]){
        myServos = servos;
        minAngles = minAngle;
        maxAngles = maxAngle;
        angleOffset = offsets;
    }

    public void setAngle(double angle, int servo){
        myServos[servo].setPosition(angle/180.0);
    }

    public void setAngle(double angle){
        double rightAngle = angle + angleOffset[RIGHT_SERVO], leftAngle = angle + angleOffset[LEFT_SERVO];
        if(rightAngle < minAngles[RIGHT_SERVO]) rightAngle = minAngles[RIGHT_SERVO];
        else if(rightAngle > maxAngles[RIGHT_SERVO]) rightAngle = maxAngles[RIGHT_SERVO];
        if(leftAngle < minAngles[LEFT_SERVO]) leftAngle = minAngles[LEFT_SERVO];
        else if(leftAngle > maxAngles[LEFT_SERVO]) leftAngle = maxAngles[LEFT_SERVO];
        myServos[LEFT_SERVO].setPosition(leftAngle/180.0);
        myServos[RIGHT_SERVO].setPosition(rightAngle/180.0);
    }

    public double [] getAngles(){
        return new double[] {getPosition()[0]*180.0, getPosition()[1]*180};
    }

    public double [] getPosition(){
        return new double[] {myServos[0].getPosition()*180.0,myServos[1].getPosition()*180.0};
    }


    public void setDirection(Servo.Direction direction, int servo){
        myServos[servo].setDirection(direction);
    }

}
