package edu.sdsu.cs.ramya.circles;

import android.graphics.PointF;
import android.util.Log;


public class Circle {
    private PointF center;
    private int radius;
    private float vx;   //velocity in x direction
    private float vy;   //velocity in y direction
    private boolean shouldEnlarge;

    public boolean shouldCircleEnlarge() {
        return shouldEnlarge;
    }

    public void setShouldEnlarge(boolean shouldEnlarge) {
        this.shouldEnlarge = shouldEnlarge;
    }

    public Circle(PointF centerCoordinates,int radius)
    {
        this.center=centerCoordinates;
        this.radius=radius;
        vx=0;
        vy=0;
        shouldEnlarge = false;
    }
    public void setCenter(PointF center)
    {
        this.center=center;
    }
    public void setCenter(float xCoordinate,float yCoordinate)
    {
        this.center.x = xCoordinate;
        this.center.y = yCoordinate;

    }
    public PointF getCenter()
    {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public boolean collide(Circle otherCircle,float timeDelta)
    {
        if(getVx()!= 0 || otherCircle.getVx()!=0
                || getVy() !=0 || otherCircle.getVy() !=0 )
        {
            double xSquare = Math.pow(center.x +  - (otherCircle.center.x), 2);
            double ySquare = Math.pow(center.y  - (otherCircle.center.y), 2);
            double radiusSquare = Math.pow(radius + otherCircle.getRadius(), 2);
            double distanceSquare = xSquare+ySquare;
            return distanceSquare <= radiusSquare;
        }
        else
        {
            return false;
        }
    }
}
