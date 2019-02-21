package Autonomous;

/**
 * Created by root on 8/22/17.
 */

/*
    A class for a location object
 */
public class Location {
    double x;
    double y;
    double heading;

    public Location(double x1, double y1){
        x = x1;
        y = y1;
    }
    public Location(double x1, double y1, double h1){
        x = x1;
        y = y1;
        heading = h1;
    }
    public Location(Location target) {
        x = target.getX();
        y = target.getY();
        heading = target.getHeading();
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getHeading() {
        return heading;
    }
    public void setX(double nX){
        x= nX;
    }
    public void setY(double nY){
        y= nY;
    }
    public void setHeading(double nH) {
        heading = nH;
    }

    public void updateXY(double x1, double y1){
        x = x1;
        y = y1;
    }
    public void updateXYH(double x1, double y1, double h1) {
        x = x1;
        y = y1;
        heading = h1;
    }

    public void addX(double dx){
        x += dx;
    }

    public void addY(double dy){
        y += dy;
    }

    public void addXY(double dx, double dy){
        x += dx;
        y += dy;
    }


    public double distanceToLocation(Location location){
        double distance = 0;
        distance = Math.sqrt(Math.pow((location.getX() - getX()), 2) + Math.pow((location.getY() - getY()), 2));
        return distance;
    }

    public String toString() {
        String toReturn = "X: " + x + ", Y: " + y;
        return toReturn;
    }
}
