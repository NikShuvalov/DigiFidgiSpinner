package shuvalov.nikita.digifidgispinner.helicopter_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import shuvalov.nikita.digifidgispinner.Spinner;
import shuvalov.nikita.digifidgispinner.SpinnerHandler;

import static android.content.ContentValues.TAG;


/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread mGameThread;
    private GameEngine mGameEngine;
    private int mSkyColor;
    private Paint mOutlinePaint;
    private Paint mDebugPaint, mBodyPaint, mPaint, mPaint2;
    private long mStartActionTime;
    private boolean mRightSideTouch, mLeftSideTouch;
    private Rect mScreenBounds;

    public GameSurfaceView(Context context, GameEngine gameEngine) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mGameEngine = gameEngine;
        makePaint();
    }

    private void makePaint(){
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();

        mOutlinePaint = new Paint();
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(5f);

        mSkyColor = Color.argb(255, 175, 200, 235);

        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.RED);

        mBodyPaint = new Paint();
        mBodyPaint.setColor(Color.argb(255,100,100,100));
        mBodyPaint.setStrokeWidth(spinner.getBearingRadius()*1.5f);
        mBodyPaint.setStyle(Paint.Style.FILL);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.BLACK);
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mScreenBounds = surfaceHolder.getSurfaceFrame();
        mGameEngine.setScreen(mScreenBounds,12);
        if(mGameThread!=null){return;}
        mGameThread = new GameThread(surfaceHolder,this);
        mGameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path terrainPath = mGameEngine.getTerrainPath();
        canvas.drawColor(mSkyColor);
        canvas.drawPath(terrainPath, mGameEngine.getTerrainPaint());
        canvas.drawPath(terrainPath, mOutlinePaint);
        mGameEngine.run();
        PointF p = mGameEngine.getHelicopter().getLocation();
        Rect r = new Rect((int)p.x - 50, (int)p.y - 50,(int)p.x + 50, (int)p.y + 50);
        canvas.drawRect(r, mDebugPaint);
//        drawSpinner(canvas);
    }


//    private void drawSpinner(Canvas canvas){
//        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
//        PointF[] bearingCenters = spinner.getBearingCenters();
//        float bearingRadius = spinner.getBearingRadius();
//        PointF spinnerCenter = spinner.getCenter();
//
//        //Draw connectors
//        for(int i =0; i < bearingCenters.length; i++) {
//            PointF bearingCenter = bearingCenters[i];
//            canvas.drawLine(spinnerCenter.x, spinnerCenter.y, bearingCenter.x, bearingCenter.y, mBodyPaint);
//        }
//
//        //Draw Bearings
//        for(int i =0; i< bearingCenters.length; i++){
//            PointF bearingCenter = bearingCenters[i];
//            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius, mPaint);
//            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius/2, mPaint2);
//        }
//        canvas.drawCircle(spinnerCenter.x, spinnerCenter.y, SpinnerHandler.getInstance().getSpinner().getRadius()/4, mPaint2);
//    }

    public void stopThread(){
        mGameThread.stopThread();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        PointF actionEventTouch = new PointF();
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mStartActionTime = SystemClock.elapsedRealtime();
                if(event.getX() > mScreenBounds.width() * .9f){
                    mGameEngine.getHelicopter().adjustHorizontalSpeed(1);
                }else if (event.getX() < mScreenBounds.width() * .1f){
                    mGameEngine.getHelicopter().adjustHorizontalSpeed(-1);
                }else{
                    actionEventTouch.set(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                actionEventTouch.set(event.getX(), event.getY());
                long endActionTime = SystemClock.elapsedRealtime();
                spinner.addTorque(mStartActionTime, endActionTime, actionEventTouch);
                mStartActionTime = endActionTime;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                spinner.releaseLastTouch();
                break;
        }
        return true;
    }
}
