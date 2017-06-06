package shuvalov.nikita.digifidgispinner.helicopter_game;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import shuvalov.nikita.digifidgispinner.FidgiSurfaceView;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class GameThread extends Thread {
    private final SurfaceHolder mSurfaceHolder;
    private GameSurfaceView mGameSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public GameThread(SurfaceHolder surfaceHolder, GameSurfaceView gameSurfaceView) {
        mSurfaceHolder = surfaceHolder;
        mGameSurfaceView = gameSurfaceView;
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
                    if(!mStop.get()) mGameSurfaceView.onDraw(c);
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
