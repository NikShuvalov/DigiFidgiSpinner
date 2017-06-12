package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static android.content.ContentValues.TAG;


/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class DigiFidgiSurfaceView extends CustomSurfaceView implements SurfaceHolder.Callback {
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
    private Bitmap mPlusIcon, mMinusIcon, mVibration, mVibrationOff;

    public DigiFidgiSurfaceView(Context context, DigiFidgiWidgiCallback digiFidgiWidgiCallback) {
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

        mPlusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.add_corner);
        mMinusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.remove_corner);
        mVibration = BitmapFactory.decodeResource(getResources(), R.drawable.ic_vibration);
        mVibrationOff = BitmapFactory.decodeResource(getResources(), R.drawable.vibrate_off);
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Rect screenBounds = surfaceHolder.getSurfaceFrame();
        setScreenBounds(screenBounds);
        mCirclePosition = new PointF(screenBounds.centerX(),screenBounds.centerY());

        float width = screenBounds.width();

        float radius = screenBounds.width() * .3f;
        SpinnerHandler spinnerHandler = SpinnerHandler.getInstance();

        mBodyPaint = new Paint();
        mBodyPaint.setColor(Color.argb(255,100,100,100));
        mBodyPaint.setStyle(Paint.Style.FILL);

        spinnerHandler.setSpinner(new Spinner(mCirclePosition,radius, 3, mBodyPaint, mPaint, mPaint2));

        createOptionRects(width, spinnerHandler.getSpinner().getBearingRadius());

        mColorUnselected = Color.argb(255, 200, 255, 255);
        mColorSelected = Color.argb(255, 125, 200, 200);

        mIconButtonUnselectedPaint = new Paint();
        mIconButtonUnselectedPaint.setColor(mColorUnselected);
        mIconButtonSelectedPaint = new Paint();
        mIconButtonSelectedPaint.setColor(mColorSelected);

        mIconOutlinePaint = new Paint();
        mIconOutlinePaint.setColor(mColorSelected);
        mIconOutlinePaint.setStyle(Paint.Style.STROKE);
        mIconOutlinePaint.setStrokeWidth(3f);
        if(getGraphicThread()!=null){
            return;}
        setGraphicThread( new GraphicThread(surfaceHolder,this));
        setSurfaceReady(true);
        startGraphicThread();
    }

    private void createOptionRects(float screenWidth, float bearingRadius){
        int buttonSideLength = (int)(bearingRadius*1.25);
        int margin = 24;
        int endOptionLeft = (int)(screenWidth*.8);
        mOptionsRects = new Rect[3];
        Rect r = new Rect(endOptionLeft, margin, (int)(screenWidth - margin), margin + buttonSideLength);
        mOptionsRects[0] = r;
        Rect r1 = new Rect(r);
        r1.offset(- buttonSideLength - (margin*2), 0);
        mOptionsRects[1] = r1;
        Rect r2 = new Rect(r1);
        r2.offset(-buttonSideLength - (margin *2), 0);
        mOptionsRects[2] = r2;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        setSurfaceReady(false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Spinner spinner = SpinnerHandler.getInstance().getSpinner();
        spinner.spin(SystemClock.elapsedRealtime());
        canvas.drawColor(Color.WHITE);
        drawButtons(canvas);
        spinner.drawOnToCanvas(canvas);
        debugText(canvas);
        float rpm = spinner.getRpm();
        if (Math.abs(rpm) > 1.5f) {
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
            if(i<2) {
                canvas.drawBitmap(i == 0 ? mPlusIcon : mMinusIcon,
                        null, r, null);

            }else{
                canvas.drawBitmap(getContext().
                        getSharedPreferences(AppConstants.PREFERENCES, Context.MODE_PRIVATE).
                        getBoolean(AppConstants.PREF_VIBRATE, true) ?
                                mVibration :
                                mVibrationOff,
                        null, r, null);
            }
            canvas.drawRect(r, mIconOutlinePaint);
        }
    }

    private void debugText(Canvas canvas){
        canvas.drawText(String.valueOf("Rpm :" + SpinnerHandler.getInstance().getSpinner().getRpm()), 50, 30, mDebugTextPaint);
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
