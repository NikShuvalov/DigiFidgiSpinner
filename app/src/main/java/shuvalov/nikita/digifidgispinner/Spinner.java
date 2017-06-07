package shuvalov.nikita.digifidgispinner;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class Spinner {
    //RPM = "Rotations per millisecond" in this instance
    private float mRpm, mMasterAngle, mRadius;
    private PointF mCenter;
    private long mLastUpdateMillis;
    private Friction mFriction;
    private float mBearingRadius;
    private PointF[] mBearingCenters;
    private PointF mLastTouch;
    private Paint mBodyPaint, mPrimaryPaint, mSecondaryPaint;
    private float mVelocityY;

    public static final int MAX_CORNERS = 7;

    public enum Friction{
        SLIPPERY, NORMAL, STICKY, INFINITESPIN
    }

    public Spinner(PointF center, float radius, int corners, Paint... paints) {
        mCenter = center;
        mRadius = radius;
        mRpm = 0;
        mMasterAngle = 0;
        mLastUpdateMillis = SystemClock.elapsedRealtime();
        mFriction = Friction.NORMAL;
        mBearingRadius = mRadius/2.5f;
        placePoints(corners);
        mBodyPaint = paints[0];
        mPrimaryPaint = paints[1];
        mSecondaryPaint = paints[2];
        mVelocityY = 0;
    }

    private void placePoints(int corners){
        mBearingCenters = new PointF[corners];
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

    public void applyRunnerMotion(long startActionTime, long endActionTime, PointF touchEventPoint){
        float deltaY;
        long elapsedTime = endActionTime - startActionTime;
        if(mLastTouch!=null){
            deltaY = touchEventPoint.x - mLastTouch.x;
            mRpm -=deltaY * elapsedTime/10000;
            if(mRpm > 0){
                mRpm = 0;
            }
        }
        mLastTouch = touchEventPoint;
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
            float horizontalTorque = touchEventPoint.y > mCenter.y + mBearingRadius ?
                    (elapsedTime * deltaX) / 10000 :
                    - (elapsedTime * deltaX) / 10000;
            if(mRpm > 0 && horizontalTorque > 0){
                mRpm += horizontalTorque > mRpm/10 ?
                        horizontalTorque :
                        0;
            }else if (mRpm < 0 && horizontalTorque < 0){
                mRpm += Math.abs(horizontalTorque) > Math.abs(mRpm/10) ?
                        horizontalTorque:
                        0;
            }else{
                mRpm += horizontalTorque;
            }
        }else{
            mLastTouch = touchEventPoint;
            float verticalTorque =
                    touchEventPoint.x > mCenter.x + mBearingRadius ?
                            -(elapsedTime * deltaY) /10000:
                            (elapsedTime * deltaY) /10000;
            if(mRpm > 0 && verticalTorque > 0){
                mRpm += verticalTorque > mRpm/10 ?
                        verticalTorque :
                        0;
            }else if (mRpm < 0 && verticalTorque < 0){
                mRpm += Math.abs(verticalTorque) > Math.abs(mRpm/10) ?
                         verticalTorque:
                        0;
            }else{
                mRpm += verticalTorque;
            }
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

    public void addCorner(){
        int corners = mBearingCenters.length+1;
        if(corners>MAX_CORNERS){
            corners = MAX_CORNERS;
        }
        placePoints(corners);
    }

    public void removeCorner(){
        int corners = mBearingCenters.length-1;
        if(corners<=1){
            corners = 2;
        }
        placePoints(corners);
    }

    public void setRadius(float radius){
        mRadius =  radius;
        mBearingRadius = radius/2.5f;
        mBodyPaint.setStrokeWidth(mBearingRadius*1.5f);
        placePoints(mBearingCenters.length);
    }

    public void drawOnToCanvas(Canvas canvas){

        //Draw connectors
        for(int i =0; i < mBearingCenters.length; i++) {
            PointF bearingCenter = mBearingCenters[i];
            canvas.drawLine(mCenter.x, mCenter.y, bearingCenter.x, bearingCenter.y, mBodyPaint);
        }

        //Draw Bearings
        for(int i =0; i< mBearingCenters.length; i++){
            PointF bearingCenter = mBearingCenters[i];
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, mBearingRadius, mPrimaryPaint);
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, mBearingRadius/2, mSecondaryPaint);
        }
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius/4, mSecondaryPaint);
    }

    public float getYVelocity() {
        return mVelocityY;
    }

    public void addYVelocity(float velocity){
        mVelocityY += velocity;
    }

    public void clearYVelocity(){
        mVelocityY = 0;
    }

    public void setCenter(PointF center) {
        mCenter = center;
    }

    public float getCombinedRadius(){
        return mBearingRadius + mRadius;
    }
}
