package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static android.content.ContentValues.TAG;


/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class FidgiSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicThread mGraphicThread;
    private Paint mPaint, mPaint2;
    private Paint mBodyPaint;
    private Paint mDebugTextPaint;
    private PointF mCirclePosition;
    private Spinner mSpinner;
    private long mStartActionTime;
    private SpeedListener mSpeedListener;
    private boolean mSpinnerHeld;


    public FidgiSurfaceView(Context context, SpeedListener speedListener) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        mDebugTextPaint = new Paint();
        mDebugTextPaint.setColor(Color.GREEN);
        mDebugTextPaint.setTextSize(30);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.BLACK);

        mSpeedListener = speedListener;
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Rect screenBounds = new Rect();
        getHitRect(screenBounds);
        mCirclePosition = new PointF(screenBounds.centerX(),screenBounds.centerY());

        float radius = screenBounds.width()/2;
        radius = radius - radius/2.5f;
        mSpinner = new Spinner(mCirclePosition,radius, 3);//At 8 corners it beings to overlap.


        mBodyPaint = new Paint();
        mBodyPaint.setColor(Color.argb(255,100,100,100));
        mBodyPaint.setStrokeWidth(mSpinner.getBearingRadius()*1.5f);
        mBodyPaint.setStyle(Paint.Style.FILL);

        if(mGraphicThread!=null){return;}
        mGraphicThread = new GraphicThread(surfaceHolder,this);
        mGraphicThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mGraphicThread.stopThread();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mSpinner.spin(SystemClock.elapsedRealtime());
        canvas.drawColor(Color.WHITE);
        drawSpinner(canvas);
        debugText(canvas);
        float rpm = mSpinner.getRpm();
        if(Math.abs(rpm)>1.5f){
            mSpeedListener.onCriticalSpeed(rpm);
        }
        //Make the drawing of the thing
    }

    private void debugText(Canvas canvas){
        canvas.drawText(String.valueOf("Rpm :" + mSpinner.getRpm()), 50, 30, mDebugTextPaint);
    }

    private void drawSpinner(Canvas canvas){
        PointF[] bearingCenters = mSpinner.getBearingCenters();
        float bearingRadius = mSpinner.getBearingRadius();
        PointF spinnerCenter = mSpinner.getCenter();

        //Draw connectors
        for(int i =0; i < bearingCenters.length; i++) {
            PointF bearingCenter = bearingCenters[i];
            canvas.drawLine(spinnerCenter.x, spinnerCenter.y, bearingCenter.x, bearingCenter.y, mBodyPaint);
        }

        //Draw Bearings
        for(int i =0; i< bearingCenters.length; i++){
            PointF bearingCenter = bearingCenters[i];
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius, mPaint);
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius/2, mPaint2);
        }
        canvas.drawCircle(spinnerCenter.x, spinnerCenter.y, mSpinner.getRadius()/4, mPaint2);
    }


    public void stopGraphicThread(){
        if(mGraphicThread!=null && mGraphicThread.isAlive()){
            mGraphicThread.stopThread();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        PointF actionEventTouch = new PointF();
        switch(action){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mStartActionTime = SystemClock.elapsedRealtime();
                actionEventTouch.set(event.getX(), event.getY());
                if(mSpinner.centerClicked(actionEventTouch)){
                    mSpinnerHeld = false;
                }else if (mSpinner.bodyClicked(actionEventTouch)){
                    mSpinner.stop();
                    mSpinner.setAngle(AppConstants.getAngle(mSpinner.getCenter(),event.getX(), event.getY()));
                    mSpinnerHeld=true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!mSpinnerHeld) {
                    actionEventTouch.set(event.getX(), event.getY());
                    long endActionTime = SystemClock.elapsedRealtime();
                    mSpinner.addTorque(mStartActionTime, endActionTime, actionEventTouch);
                    mStartActionTime = endActionTime;
                    break;
                }
                mSpinner.setAngle(AppConstants.getAngle(mSpinner.getCenter(),event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mSpinnerHeld= false;
                mSpinner.releaseLastTouch();
                break;
        }
        return true;
    }

    interface SpeedListener{
        void onCriticalSpeed(float rpm);
    }
}
