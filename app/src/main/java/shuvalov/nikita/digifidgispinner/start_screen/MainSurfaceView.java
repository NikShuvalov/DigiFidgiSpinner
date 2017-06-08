package shuvalov.nikita.digifidgispinner.start_screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by NikitaShuvalov on 6/8/17.
 */

public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicStartThread mGraphicStartThread;
    private Rect mScreenBounds;
    private int mBackgroundColor;
    private RectF mGameModeRect, mCasualModeRect;
    private Paint mFramePaint;

    public Paint mDebugPaint;
    private MainSurfaceView.Callback mCallback;

    private static final float BUTTON_MARGIN_PERCENT = .1f;
    private static final float BUTTON_SIZE_PERCENT = .4f;


    public MainSurfaceView(Context context, Callback mainSurfaceViewCallback) {
        super(context);

        mCallback = mainSurfaceViewCallback;
        mBackgroundColor = Color.argb(255, 0, 25,50);
        createPaints();
        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.WHITE);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    private void createPaints(){
        mFramePaint = new Paint();
        mFramePaint.setColor(Color.DKGRAY);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(4f);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if( mGraphicStartThread!= null){return;}
        mGraphicStartThread = new GraphicStartThread(surfaceHolder, this);
        mScreenBounds = surfaceHolder.getSurfaceFrame();
        createButtonRects();
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

        canvas.drawRect(mGameModeRect, mDebugPaint);
        canvas.drawRect(mGameModeRect, mFramePaint);

        canvas.drawRect(mCasualModeRect, mDebugPaint);
        canvas.drawRect(mCasualModeRect, mFramePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                PointF touchLoc = new PointF(event.getX(), event.getY());
                if(mGameModeRect.contains(touchLoc.x, touchLoc.y)){
                    mCallback.onGameSelected();
                }else if (mCasualModeRect.contains(touchLoc.x, touchLoc.y)){
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

    interface Callback{
        void onGameSelected();
        void onCasualSelected();
    }

}
