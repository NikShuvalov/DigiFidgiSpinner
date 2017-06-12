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
    private CustomSurfaceView mCustomSurfaceView;
    private AtomicBoolean mStop = new AtomicBoolean();

    public GraphicThread(SurfaceHolder surfaceHolder, CustomSurfaceView customSurfaceView) {
        mSurfaceHolder = surfaceHolder;
        mCustomSurfaceView = customSurfaceView;
        mStop.set(false);
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        if(!mStop.get() && !mCustomSurfaceView.isSurfaceReady()){
            try {
                sleep(100);
                run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(!mStop.get() && mCustomSurfaceView.isSurfaceReady()){
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder){
                    if(!mStop.get() && mCustomSurfaceView.isSurfaceReady()) mCustomSurfaceView.onDraw(c);
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
