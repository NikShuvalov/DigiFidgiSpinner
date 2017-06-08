package shuvalov.nikita.digifidgispinner.start_screen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;

import static android.content.ContentValues.TAG;

/**
 * Created by NikitaShuvalov on 6/8/17.
 */

public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicStartThread mGraphicStartThread;
    private Rect mScreenBounds;
    private int mBackgroundColor;
    private RectF mGameModeRect, mCasualModeRect;
    private Paint mFramePaint;
    private Bitmap mGamePreview, mCasualPreview;
    private Spinner mDemoSpinner;

    public Paint mDebugPaint;
    private MainSurfaceView.Callback mCallback;

    private static final float BUTTON_MARGIN_PERCENT = .1f;
    private static final float BUTTON_SIZE_PERCENT = .4f;


    public MainSurfaceView(Context context, Callback mainSurfaceViewCallback) {
        super(context);

        mCallback = mainSurfaceViewCallback;
        mBackgroundColor = Color.argb(255, 230, 230,230);
        createPaints();
        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.WHITE);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mGamePreview = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_preview);
    }

    private void createPaints(){
        mFramePaint = new Paint();
        mFramePaint.setColor(Color.DKGRAY);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(20f);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if( mGraphicStartThread!= null){
            return;}
        mGraphicStartThread = new GraphicStartThread(surfaceHolder, this);
        mScreenBounds = surfaceHolder.getSurfaceFrame();
        createButtonRects();
        createDemoSpinner();
        mGraphicStartThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        drawOptions(canvas);
        demonstrateSpinner();
    }


    private void createDemoSpinner(){
        PointF demoCenter = new PointF(mCasualModeRect.centerX(), mCasualModeRect.centerY());
        float radius = mCasualModeRect.height() > mCasualModeRect.width() ?
                mCasualModeRect.width() *.3f : mCasualModeRect.height() *.3f;

        mDemoSpinner = new Spinner(demoCenter,radius, 3);
        mDemoSpinner.changeFriction(Spinner.Friction.STICKY);
    }

    private void createButtonRects(){
        float left = mScreenBounds.width() * BUTTON_MARGIN_PERCENT;
        float top = mScreenBounds.height() * BUTTON_MARGIN_PERCENT;
        float right = mScreenBounds.width() *  (1 - BUTTON_MARGIN_PERCENT);
        float bottom = mScreenBounds.height() * BUTTON_SIZE_PERCENT;
        mGameModeRect = new RectF(left, top, right, bottom);
        mCasualModeRect = new RectF(left, mScreenBounds.centerY() + top, right, bottom + mScreenBounds.centerY());
    }

    private void drawOptions(Canvas canvas){
        canvas.drawBitmap(mGamePreview,null, mGameModeRect, null);
        canvas.drawRect(mGameModeRect, mFramePaint);

        canvas.drawRect(mCasualModeRect, mDebugPaint);
        if(mDemoSpinner!=null) {
            mDemoSpinner.drawOnToCanvas(canvas);
        }
        canvas.drawRect(mCasualModeRect, mFramePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                PointF touchLoc = new PointF(event.getX(), event.getY());
                if(mGameModeRect.contains(touchLoc.x, touchLoc.y)){
                    stopThread();
                    mCallback.onGameSelected();
                }else if (mCasualModeRect.contains(touchLoc.x, touchLoc.y)){
                    stopThread();
                    mCallback.onCasualSelected();
                }
        }
        return true;
    }


    public void stopThread(){
        if(mGraphicStartThread!=null && mGraphicStartThread.isAlive()) {
            mGraphicStartThread.stopThread();
        }
    }

    public void demonstrateSpinner(){
        mDemoSpinner.spin(SystemClock.elapsedRealtime());
        if(mDemoSpinner.getRpm()==0){
            mDemoSpinner.addRpm(2);
        }
    }

    interface Callback{
        void onGameSelected();
        void onCasualSelected();
    }
}
