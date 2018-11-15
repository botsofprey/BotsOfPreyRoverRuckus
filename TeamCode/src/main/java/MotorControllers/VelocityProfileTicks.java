package MotorControllers;

import android.util.Log;

/**
 * Created by robotics on 11/7/17.
 */

/*
    ~~~
 */
//TODO: ask what this is for
public class VelocityProfileTicks {
    public enum VelocityProfileType{
        ACCEL_AND_DECCEL, DECCEL_ONLY, ACCEL_ONLY, NO_ACCEL_AND_DECCEL
    }

    private long tickToDecelAt;
    private long decelerationTicksPerSecPerSec;
    private long maxSpeedTicksPerSecond;
    private long minSpeedTicksPerSecond;
    private double timeToDecel;
    private long totalTicksToTravel;
    private boolean goodProfile;

    //TODO, implement profile types

    public VelocityProfileTicks(long decelTicksPerSecPerSec, long maxSpeed, long minSpeed, long ticksToTravel){
        decelerationTicksPerSecPerSec = decelTicksPerSecPerSec;
        maxSpeedTicksPerSecond = maxSpeed;
        minSpeedTicksPerSecond = minSpeed;
        totalTicksToTravel = ticksToTravel;
        Log.v("Decel Profile", "Decel:" + Double.toString(decelerationTicksPerSecPerSec));
        Log.v("Decel Profile", "Max Speed:" + Double.toString(maxSpeedTicksPerSecond));
        Log.v("Decel Profile", "Min Speed:" + Double.toString(minSpeedTicksPerSecond));
        Log.v("Decel Profile", "Total Ticks:" + Double.toString(totalTicksToTravel));
        calculateProfile();
    }

    private void calculateProfile(){
        timeToDecel = ((double)maxSpeedTicksPerSecond - minSpeedTicksPerSecond) / (double)decelerationTicksPerSecPerSec;
        Log.v("Decel Profile", "Decel Time:" + Double.toString(timeToDecel));
        long ticksToDecel = (long)(timeToDecel*(maxSpeedTicksPerSecond - minSpeedTicksPerSecond) / 2.0); //integration of a triangle
        Log.v("Decel Profile", "Ticks to decel: " + Long.toString(ticksToDecel));
        tickToDecelAt = totalTicksToTravel - ticksToDecel;
        if(tickToDecelAt > 0) goodProfile = true;
        else goodProfile = false;
    }

    public long calculateSpeed(long currentTickFromStart){
        if(!goodProfile) return minSpeedTicksPerSecond;
        else if(currentTickFromStart < tickToDecelAt) return maxSpeedTicksPerSecond;
        long speed = maxSpeedTicksPerSecond - (decelerationTicksPerSecPerSec * (currentTickFromStart - tickToDecelAt));
        if(speed < minSpeedTicksPerSecond) speed = minSpeedTicksPerSecond;
        return speed;
    }
}