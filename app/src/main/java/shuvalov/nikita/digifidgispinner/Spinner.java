package shuvalov.nikita.digifidgispinner;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.SystemClock;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class Spinner {
    private float mRpm, mAngle, mRadius;
    private PointF mCenter;
    private Bitmap mBitmap;
    private long mLastUpdateMillis;
    private Friction mFriction;

    public enum Friction{
        SLIPPERY, NORMAL, STICKY, INFINITESPIN
    }

    public Spinner(Bitmap bitmap, PointF center, float radius) {
        mBitmap = bitmap;
        mCenter = center;
        mRadius = radius;

        mRpm = 0;
        mAngle = 0;
        mLastUpdateMillis = SystemClock.elapsedRealtime();
        mFriction = Friction.NORMAL;
    }

    public void spin(long currentTime){
        long elapsedTime = currentTime - mLastUpdateMillis;
        mAngle += (mRpm/60000) *(elapsedTime);
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
        mAngle = angle;
    }

    public float getAngle() {
        return mAngle;
    }

    public PointF getCenter(){
        return mCenter;
    }



}
