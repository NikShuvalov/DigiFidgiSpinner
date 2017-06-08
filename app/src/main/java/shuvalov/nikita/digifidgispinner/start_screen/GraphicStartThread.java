package shuvalov.nikita.digifidgispinner.start_screen;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import shuvalov.nikita.digifidgispinner.GraphicThread;

/**
 * Created by NikitaShuvalov on 6/8/17.
 */

public class GraphicStartThread extends Thread {
    private final SurfaceHolder mSurfaceHolder;
    private MainSurfaceView mSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public GraphicStartThread(SurfaceHolder surfaceHolder, MainSurfaceView mainSurfaceView){
        mSurfaceHolder = surfaceHolder;
        mSurfaceView = mainSurfaceView;
        mStop.set(false);
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        super.run();
        while(!mStop.get()){
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder){
                    if(!mStop.get()) mSurfaceView.onDraw(c);
                }
            }finally {
                if( c!=null) mSurfaceHolder.unlockCanvasAndPost(c);
            }

        }
    }
    public void stopThread(){mStop.set(true);}
}
