package shuvalov.nikita.digifidgispinner.runner_game;

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

import shuvalov.nikita.digifidgispinner.Spinner;


/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class RunnerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private RunnerThread mRunnerThread;
    private RunnerEngine mRunnerEngine;
    private int mSkyColor;
    private long mStartActionTime;
    private Paint mDebugPaint;
    private Paint mGameOverPaint, mEndStatsPaint;
    private int mBlinkDuration;
    private boolean mBlinking;

    public static final int BLINK_DURATION = 20;

    public RunnerSurfaceView(Context context, RunnerEngine runnerEngine) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mRunnerEngine = runnerEngine;
        mBlinkDuration  = 0;
        createPaints();

    }

    private void createPaints(){
        mSkyColor = Color.argb(255, 175, 200, 235);

        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.YELLOW);
        mDebugPaint.setTextSize(30f);

        mGameOverPaint = new Paint();
        mGameOverPaint.setColor(Color.WHITE);
        mGameOverPaint.setTextSize(100f);

        mEndStatsPaint = new Paint();
        mEndStatsPaint.setColor(Color.WHITE);
        mEndStatsPaint.setTextSize(60f);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(mRunnerThread!=null){
            createPaints();
            return;}
        mRunnerThread = new RunnerThread(surfaceHolder, this);
        mRunnerThread.start();
        mRunnerEngine.setScreen(surfaceHolder.getSurfaceFrame());

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mSkyColor);
        mRunnerEngine.getSpinner().spin(SystemClock.elapsedRealtime());
        mRunnerEngine.run();
        mRunnerEngine.drawTerrain(canvas);
        float rpm = Math.abs(mRunnerEngine.getSpinner().getRpm());
        int remainingTime = (int)(mRunnerEngine.getTimeLeft()/1000);
        int distance = (int)Math.abs(mRunnerEngine.getDistance());
        canvas.drawText("RPM:" + rpm, 50, 50, mDebugPaint);
        canvas.drawText("Distance: " + distance, 50, 100, mDebugPaint);
        canvas.drawText("Time left: " + remainingTime, canvas.getWidth()*.75f,50, mDebugPaint);
        mRunnerEngine.getSpinner().drawOnToCanvasRunner(canvas);
        if(mRunnerEngine.isGameOver()){
            drawGameOverOverlay(canvas);
        }
    }

    private void drawGameOverOverlay(Canvas canvas){
        canvas.drawColor(Color.argb(100, 0, 0, 0));
        Rect screenBounds = mRunnerEngine.getScreenBounds();
        canvas.drawText("Game Over", screenBounds.width() * .28f, screenBounds.height() * .25f, mGameOverPaint);
        String highScoreText = "High score: " + 0;
        String distanceText = "Distance: "+ (int)mRunnerEngine.getDistance();
        String timeText = "Time Remaining: " + (int) mRunnerEngine.getTimeLeft()/1000;
        canvas.drawText(highScoreText,screenBounds.width() * .3f, screenBounds.height() *.35f, mEndStatsPaint);
        canvas.drawText(distanceText,screenBounds.width() * .3f, screenBounds.height() *.45f, mEndStatsPaint);
        canvas.drawText(timeText,screenBounds.width() * .3f, screenBounds.height() *.55f, mEndStatsPaint);
        mBlinkDuration++;
        if(mBlinking){
            String promptText = "Tap to Restart";
            canvas.drawText(promptText, screenBounds.width() *.28f, screenBounds.height() *.75f, mGameOverPaint);
        }
        if(mBlinkDuration >= BLINK_DURATION){
            mBlinkDuration = 0;
            mBlinking = !mBlinking;
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        PointF actionEventTouch = new PointF();
        Spinner spinner = mRunnerEngine.getSpinner();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(!mRunnerEngine.isGameActive()) {
                    mRunnerEngine.startGame();
                }
                mStartActionTime = SystemClock.elapsedRealtime();
                actionEventTouch.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mRunnerEngine.startJump();
                break;
            case MotionEvent.ACTION_MOVE:
                actionEventTouch.set(event.getX(), event.getY());
                long endActionTime = SystemClock.elapsedRealtime();
                if(!mRunnerEngine.isGameOver()) {
                    spinner.applyRunnerMotion(mStartActionTime, endActionTime, actionEventTouch);
                }
                mStartActionTime = endActionTime;
                break;
            case MotionEvent.ACTION_UP:
                spinner.releaseLastTouch();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mRunnerEngine.stopJump();
                break;
        }
        return true;
    }

    public void stopThread() {
        if(mRunnerThread!=null && mRunnerThread.isAlive()) {
            mRunnerThread.stopThread();
        }
    }
}
