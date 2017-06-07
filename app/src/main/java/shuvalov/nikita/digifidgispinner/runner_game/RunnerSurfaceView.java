package shuvalov.nikita.digifidgispinner.runner_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class RunnerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private RunnerThread mRunnerThread;
    private RunnerEngine mRunnerEngine;
    private int mSkyColor;


    public RunnerSurfaceView(Context context, RunnerEngine runnerEngine) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mRunnerEngine = runnerEngine;
        createPaints();
    }

    private void createPaints(){
        mSkyColor = Color.argb(255, 175, 200, 235);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(mRunnerThread!=null){return;}
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
        mRunnerEngine.drawTerrain(canvas);
    }

    public void stopThread(){
        mRunnerThread.stopThread();
    }
}
