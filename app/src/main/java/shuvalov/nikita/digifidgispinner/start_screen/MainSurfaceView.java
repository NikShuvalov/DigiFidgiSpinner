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
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import shuvalov.nikita.digifidgispinner.CustomSurfaceView;
import shuvalov.nikita.digifidgispinner.GraphicThread;
import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;

/**
 * Created by NikitaShuvalov on 6/8/17.
 */

public class MainSurfaceView extends CustomSurfaceView implements SurfaceHolder.Callback {
    private int mBackgroundColor;
    private RectF mGameModeRect, mCasualModeRect;
    private Paint mFramePaint;
    private Bitmap mGamePreview;
    private Spinner mDemoSpinner;
    private boolean mGameHover, mCasualHover;
    private String mTapDownOption;
    private Paint mHoverPaint;

//    private RunnerEngine mRunnerEngine;

    public Paint mDebugPaint;
    private MainSurfaceView.Callback mCallback;

    private static final String GAME = "Game";
    private static final String CASUAL = "Casual";

    private static final float BUTTON_MARGIN_PERCENT = .1f;
    private static final float BUTTON_SIZE_PERCENT = .4f;


    public MainSurfaceView(Context context, Callback mainSurfaceViewCallback) {
        super(context);
        setSurfaceReady(false);
        mTapDownOption = "";
        mGameHover = false;
        mCasualHover = false;
        mCallback = mainSurfaceViewCallback;
        mBackgroundColor = Color.argb(255, 230, 230,230);
        createPaints();
        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.WHITE);


        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mGamePreview = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_preview);
//        mRunnerEngine = new RunnerEngine(true);
    }

    private void createPaints(){
        mFramePaint = new Paint();
        mFramePaint.setColor(Color.DKGRAY);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(20f);

        mHoverPaint = new Paint();
        mHoverPaint.setColor(Color.argb(100, 0, 0, 0));
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if( getGraphicThread()!= null){
            return;}
        setGraphicThread(new GraphicThread(surfaceHolder, this));
        setScreenBounds(surfaceHolder.getSurfaceFrame());
        createButtonRects();
//        Rect r = new Rect();
//        mGameModeRect.round(r);
//        mRunnerEngine.setScreen(r);
        createDemoSpinner();
        setSurfaceReady(true);
        startGraphicThread();
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
        canvas.drawColor(mBackgroundColor);
        drawOptions(canvas);
//        mRunnerEngine.runDemoMode();
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
        Rect screenBounds = getScreenBounds();
        float left = screenBounds.width() * BUTTON_MARGIN_PERCENT;
        float top = screenBounds.height() * BUTTON_MARGIN_PERCENT;
        float right = screenBounds.width() *  (1 - BUTTON_MARGIN_PERCENT);
        float bottom = screenBounds.height() * BUTTON_SIZE_PERCENT;
        mGameModeRect = new RectF(left, top, right, bottom);
        mCasualModeRect = new RectF(left, screenBounds.centerY() + top, right, bottom + screenBounds.centerY());
    }

    private void drawOptions(Canvas canvas){
        canvas.drawBitmap(mGamePreview, null, mGameModeRect, null);
        if(mGameHover) {
            canvas.drawRect(mGameModeRect,mHoverPaint);
        }
        canvas.drawRect(mGameModeRect, mFramePaint);

        canvas.drawRect(mCasualModeRect, mCasualHover ? mHoverPaint : mDebugPaint);
        if(mDemoSpinner!=null) {
            mDemoSpinner.drawOnToCanvas(canvas);
        }
        canvas.drawRect(mCasualModeRect, mFramePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(mGameModeRect.contains(event.getX(), event.getY())){
                    mTapDownOption = GAME;
                    mGameHover = true;
                }else if (mCasualModeRect.contains(event.getX(), event.getY())){
                    mTapDownOption = CASUAL;
                    mCasualHover = true;

                }else{
                    mTapDownOption = "";
                    mCasualHover = false;
                    mGameHover = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mGameModeRect.contains(event.getX(), event.getY())){
                    mGameHover = true;
                }else if (mCasualModeRect.contains(event.getX(), event.getY())){
                    mCasualHover = true;
                }else{
                    mCasualHover = false;
                    mGameHover = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                mGameHover = false;
                mCasualHover = false;
                if(mGameModeRect.contains(event.getX(), event.getY()) && mTapDownOption.equals(GAME)){
                    mTapDownOption = "";
                    stopGraphicThread();
                    mCallback.onGameSelected();
                }else if (mCasualModeRect .contains(event.getX(), event.getY()) && mTapDownOption.equals(CASUAL)){
                    mTapDownOption = "";
                    stopGraphicThread();
                    mCallback.onCasualSelected();
                }else{
                    mTapDownOption = "";
                }
                break;
        }
        return true;
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
