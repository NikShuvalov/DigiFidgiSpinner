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
                Log.d(TAG, "run: + sleep");
                sleep(100);
                run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(!mStop.get() && mCustomSurfaceView.isSurfaceReady()){
            Canvas c = null;
            try{
                synchronized (mSurfaceHolder){
                    c = mSurfaceHolder.lockCanvas();
                    if(!mStop.get() && mCustomSurfaceView.isSurfaceReady()) mCustomSurfaceView.onDraw(c);
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
