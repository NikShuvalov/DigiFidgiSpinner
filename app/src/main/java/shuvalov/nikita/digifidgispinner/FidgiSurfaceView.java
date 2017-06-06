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


/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class FidgiSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicThread mGraphicThread;
    private Paint mPaint, mPaint2;
    private Paint mBodyPaint;
    private Paint mDebugTextPaint;
    private PointF mCirclePosition;
    private long mStartActionTime;
    private DigiFidgiWidgiCallback mDigiFidgiWidgiCallback;
    private boolean mHoveringOption;
    private Paint mIconButtonSelectedPaint, mIconButtonUnselectedPaint, mIconOutlinePaint;
    private Rect[] mOptionsRects;
    private int mOptionSelected;
    private int mColorSelected, mColorUnselected;

    public FidgiSurfaceView(Context context, DigiFidgiWidgiCallback digiFidgiWidgiCallback) {
        super(context);
        mOptionSelected = -1;
        mHoveringOption = false;

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        mDebugTextPaint = new Paint();
        mDebugTextPaint.setColor(Color.GREEN);
        mDebugTextPaint.setTextSize(30);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.BLACK);

        mDigiFidgiWidgiCallback = digiFidgiWidgiCallback;
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Rect screenBounds = new Rect();
        getHitRect(screenBounds);
        mCirclePosition = new PointF(screenBounds.centerX(),screenBounds.centerY());

        float width = screenBounds.width();
        mOptionsRects = new Rect[]{
                new Rect((int)(width*.8), 16, (int)(width - 16), 100),
                new Rect((int)(width *.6), 16, (int)(width*.8 - 16), 100),
                new Rect((int)(width *.3), 16, (int)(width*.6 -16), 100)
        };
        float radius = screenBounds.width() * .3f;
        Spinner spinner;
        SpinnerHandler spinnerHandler = SpinnerHandler.getInstance();
        if((spinner = spinnerHandler.getSpinner()) == null){
            spinnerHandler.setSpinner(spinner = new Spinner(mCirclePosition,radius, 3));
        }

        mColorUnselected = Color.argb(255, 200, 255, 255);
        mColorSelected = Color.argb(255, 125, 200, 200);

        mBodyPaint = new Paint();
        mBodyPaint.setColor(Color.argb(255,100,100,100));
        mBodyPaint.setStrokeWidth(spinner.getBearingRadius()*1.5f);
        mBodyPaint.setStyle(Paint.Style.FILL);

        mIconButtonUnselectedPaint = new Paint();
        mIconButtonUnselectedPaint.setColor(mColorUnselected);
        mIconButtonSelectedPaint = new Paint();
        mIconButtonSelectedPaint.setColor(mColorSelected);

        mIconOutlinePaint = new Paint();
        mIconOutlinePaint.setColor(mColorSelected);
        mIconOutlinePaint.setStyle(Paint.Style.STROKE);
        mIconOutlinePaint.setStrokeWidth(3f);
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
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
        spinner.spin(SystemClock.elapsedRealtime());
        canvas.drawColor(Color.WHITE);
        drawButtons(canvas);
        drawSpinner(canvas);
        debugText(canvas);
        float rpm = spinner.getRpm();
        if(Math.abs(rpm)>1.5f){
            mDigiFidgiWidgiCallback.onCriticalSpeed(rpm);
        }
    }

    private void drawButtons(Canvas canvas){
        for(int i =0; i< mOptionsRects.length; i++){
            Rect r = mOptionsRects[i];
            if(mHoveringOption) {
                canvas.drawRect(r, i == mOptionSelected ? mIconButtonSelectedPaint : mIconButtonUnselectedPaint);
            }else{
                canvas.drawRect(r, mIconButtonUnselectedPaint);
            }
            canvas.drawRect(r, mIconOutlinePaint);
        }
    }

    private void debugText(Canvas canvas){
        canvas.drawText(String.valueOf("Rpm :" + SpinnerHandler.getInstance().getSpinner().getRpm()), 50, 30, mDebugTextPaint);
    }

    private void drawSpinner(Canvas canvas){
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
        PointF[] bearingCenters = spinner.getBearingCenters();
        float bearingRadius = spinner.getBearingRadius();
        PointF spinnerCenter = spinner.getCenter();

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
        canvas.drawCircle(spinnerCenter.x, spinnerCenter.y, SpinnerHandler.getInstance().getSpinner().getRadius()/4, mPaint2);
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
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mStartActionTime = SystemClock.elapsedRealtime();
                actionEventTouch.set(event.getX(), event.getY());
                for (int i = 0; i < mOptionsRects.length; i++){
                    Rect r = mOptionsRects[i];
                    if (mHoveringOption = r.contains((int) actionEventTouch.x, (int) actionEventTouch.y)) {
                        mOptionSelected = i;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                    if (mOptionSelected>-1) {
                        Rect r = mOptionsRects[mOptionSelected];
                        if (r.contains((int) event.getX(), (int) event.getY())) {
                            mHoveringOption = true;
                            break;
                        }else{
                            mHoveringOption = false;
                            break;
                        }
                    } else {
                        mHoveringOption = false;
                        actionEventTouch.set(event.getX(), event.getY());
                        long endActionTime = SystemClock.elapsedRealtime();
                        spinner.addTorque(mStartActionTime, endActionTime, actionEventTouch);
                        mStartActionTime = endActionTime;
                        break;
                    }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if(mOptionSelected>-1) {
                    Rect r = mOptionsRects[mOptionSelected];
                    if(r.contains((int)event.getX(), (int)event.getY()) && mOptionSelected>-1) {
                        mDigiFidgiWidgiCallback.onOptionSelected(mOptionSelected);
                    }
                    mOptionSelected = -1;
                    break;
                }
                spinner.releaseLastTouch();
                break;
        }
        return true;
    }

    interface DigiFidgiWidgiCallback{
        void onCriticalSpeed(float rpm);
        void onOptionSelected(int i);
    }
}
