package shuvalov.nikita.digifidgispinner;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class GraphicThread extends Thread {

    private final SurfaceHolder mSurfaceHolder;
    private FidgiSurfaceView mFidgiSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public GraphicThread(SurfaceHolder surfaceHolder, FidgiSurfaceView fidgiSurfaceView) {
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
    }

    public void stopThread(){
        mStop.set(true);
    }

}
