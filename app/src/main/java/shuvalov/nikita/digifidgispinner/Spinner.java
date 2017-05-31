package shuvalov.nikita.digifidgispinner;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class Spinner {
    private float mRpm, mMasterAngle, mRadius;
    private PointF mCenter;
    private long mLastUpdateMillis;
    private Friction mFriction;
    private float mBearingRadius;
    private PointF[] mBearingCenters;

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
        float angleDelta = (mRpm/60000) *(elapsedTime);
        mMasterAngle += angleDelta;
        if(Math.abs(mRpm) < .1){
            mRpm = 0;
        }else{
            applyFriction(elapsedTime);
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
}
