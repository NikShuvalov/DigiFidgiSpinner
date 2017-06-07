package shuvalov.nikita.digifidgispinner.runner_game;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import shuvalov.nikita.digifidgispinner.DigiFidgiSurfaceView;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class RunnerThread extends Thread {

    private final SurfaceHolder mSurfaceHolder;
    private RunnerSurfaceView mRunnerSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public RunnerThread(SurfaceHolder surfaceHolder, RunnerSurfaceView runnerSurfaceView) {
        mSurfaceHolder = surfaceHolder;
        mRunnerSurfaceView = runnerSurfaceView;
        mStop.set(false);
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        while(!mStop.get()){
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder){
                    if(!mStop.get()) mRunnerSurfaceView.onDraw(c);
                }
            }finally {
                if(c != null)mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }

    public void stopThread(){
        mStop.set(true);
    }
}
