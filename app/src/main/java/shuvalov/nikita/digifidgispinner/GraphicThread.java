package shuvalov.nikita.digifidgispinner;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.ContentValues.TAG;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class GraphicThread extends Thread {

    private final SurfaceHolder mSurfaceHolder;
    private DigiFidgiSurfaceView mFidgiSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public GraphicThread(SurfaceHolder surfaceHolder, DigiFidgiSurfaceView fidgiSurfaceView) {
        mSurfaceHolder = surfaceHolder;
        mFidgiSurfaceView = fidgiSurfaceView;
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
                    if(!mStop.get()) mFidgiSurfaceView.onDraw(c);
                }
            }finally {
                if(c != null)mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopThread(){
        mStop.set(true);
    }

}
