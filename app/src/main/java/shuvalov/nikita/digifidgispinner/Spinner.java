package shuvalov.nikita.digifidgispinner;

import android.graphics.PointF;
import android.os.SystemClock;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class Spinner {
    //(RPM = "Rotations per millisecond" in this instance
    private float mRpm, mMasterAngle, mRadius;
    private PointF mCenter;
    private long mLastUpdateMillis;
    private Friction mFriction;
    private float mBearingRadius;
    private PointF[] mBearingCenters;
    private PointF mLastTouch;

    public enum Friction{
        SLIPPERY, NORMAL, STICKY, INFINITESPIN
    }

    public Spinner(PointF center, float radius, int corners) {
        mCenter = center;
        mRadius = radius;
        mRpm = 0;
        mMasterAngle = 0;
        mLastUpdateMillis = SystemClock.elapsedRealtime();
        mFriction = Friction.NORMAL;
        mBearingRadius = mRadius/2.5f;
        mBearingCenters = new PointF[corners];
        placePoints(corners);
    }

    private void placePoints(int corners){
        float angle = mMasterAngle;
        for(int i = 0 ; i< corners; i ++){
            mBearingCenters[i] = AppConstants.getCoords(mCenter, angle, mRadius);
            angle+= 360f/corners;
        }
    }

    public PointF[] getBearingCenters(){
        return mBearingCenters;
    }

    public void spin(long currentTime){
        long elapsedTime = currentTime - mLastUpdateMillis;
        float angleDelta = mRpm *(elapsedTime);
        mMasterAngle += angleDelta;
        if(Math.abs(mRpm) < .1){
            mRpm = 0;
        }else{
            applyFriction(elapsedTime);
        }
        placePoints(mBearingCenters.length);
        mLastUpdateMillis = currentTime;
    }

    public void addTorque(long startActionTime, long endActionTime, PointF touchEventPoint){
        float deltaX = 0;
        float deltaY = 0;
        long elapsedTime = endActionTime- startActionTime;
        if(mLastTouch!=null){
            deltaX = touchEventPoint.x - mLastTouch.x;
            deltaY = touchEventPoint.y - mLastTouch.y;
        }
        if(Math.abs(deltaX)> Math.abs(deltaY)) {
            mLastTouch = touchEventPoint;
            float horizontalTorque = (elapsedTime * deltaX) / 10000;
            mRpm += touchEventPoint.y > mCenter.y + mBearingRadius ?
                    horizontalTorque :
                    -horizontalTorque;
        }else{
            mLastTouch = touchEventPoint;
            float verticalTorque = (elapsedTime * deltaY) /10000;
            mRpm += touchEventPoint.x > mCenter.x + mBearingRadius ?
                    -verticalTorque:
                    verticalTorque;
        }
    }

    private void applyFriction(long elapsedTime){
        float frictionForce;
        switch (mFriction){
            case SLIPPERY:
                frictionForce = mRpm>0 ? .0001f * elapsedTime :
                        .0001f * -elapsedTime;
                mRpm-= frictionForce;
                break;
            case NORMAL:
                frictionForce = mRpm>0 ? .0002f * elapsedTime :
                        .0002f * -elapsedTime;
                mRpm-= frictionForce;
                break;
            case STICKY:
                frictionForce = mRpm>0 ? .0004f * elapsedTime :
                        .0004f * -elapsedTime;
                mRpm-= frictionForce;
                break;
        }
    }

    public void setAngle(float angle){
        mMasterAngle = angle;
        placePoints(mBearingCenters.length);
    }

    public float getAngle() {
        return mMasterAngle;
    }

    public PointF getCenter(){
        return mCenter;
    }

    public float getRadius() {
        return mRadius;
    }

    public float getBearingRadius() {
        return mBearingRadius;
    }

    public float getRpm() {
        return mRpm;
    }

    public void releaseLastTouch(){
        mLastTouch = null;
    }

    public boolean centerClicked(PointF clickedPoint){
        float xDistance = Math.abs(mCenter.x - clickedPoint.x);
        float yDistance = Math.abs(mCenter.y - clickedPoint.y);
        return Math.sqrt((xDistance * xDistance) + (yDistance*yDistance)) <= mBearingRadius;
    }

    public boolean bodyClicked(PointF clickedPoint){
        float xDistance = Math.abs(mCenter.x - clickedPoint.x); //xVector distance from center
        float yDistance = Math.abs(mCenter.y - clickedPoint.y); //yVector distance from center
        float totalDistance = (float)Math.sqrt((xDistance * xDistance) + (yDistance*yDistance));
        if(totalDistance >= mRadius + mBearingRadius){  //Crudely check hitRect, this makes the hit rect a little longer/wider than it should be near the edges.
            return false;
        }
        float angle = AppConstants.getAngle(mCenter, clickedPoint.x, clickedPoint.y); //Angle of the touch relative to the spinner center.
        float marginAngle = (float)Math.toDegrees(mBearingRadius/mRadius); //Calculates the angle produced by the arc.
        //Find the angle with the least distance to the click point

        float[] bearingAngles = new float[mBearingCenters.length];
        for(int i = 0; i< mBearingCenters.length; i++){
            PointF centerPoint = mBearingCenters[i];
            bearingAngles[i] = AppConstants.getAngle(mCenter, centerPoint.x, centerPoint.y);
        }

        float closestAngle = 1000;
        float smallestAngleDifference = 360;
        for (float bearingAngle : bearingAngles) {
            float angleDifference = Math.abs(bearingAngle - angle);
            if (smallestAngleDifference > angleDifference) {
                smallestAngleDifference = angleDifference;
                closestAngle = bearingAngle;
            }
        }

        float topRange = (marginAngle/2 + angle) + 360; //Ranges become less confusing if we take negative numbers out of the equation
        float botRange = (angle - marginAngle/2) + 360;

        if (topRange> closestAngle + 360 && closestAngle +360 > botRange){ //User clicked within the arc range.
            return true;
        }
        return false;
    }

    public void stop(){
        mRpm = 0;
    }
}
