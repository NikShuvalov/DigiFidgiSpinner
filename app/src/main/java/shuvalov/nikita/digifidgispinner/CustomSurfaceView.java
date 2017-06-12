package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceView;

/**
 * Created by NikitaShuvalov on 6/12/17.
 */

public abstract class CustomSurfaceView extends SurfaceView {
    private GraphicThread mGraphicThread;
    private Rect mScreenBounds;
    private boolean mSurfaceReady;

    public CustomSurfaceView(Context context) {
        super(context);
        mSurfaceReady = false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setSurfaceReady(boolean surfaceReady){
        mSurfaceReady = surfaceReady;
    }
    public boolean isSurfaceReady(){
        return mSurfaceReady;
    }

    public void setGraphicThread(GraphicThread graphicThead){
        mGraphicThread = graphicThead;
    }

    public GraphicThread getGraphicThread(){
        return mGraphicThread;
    }

    public void stopGraphicThread(){
        if(mGraphicThread!=null && mGraphicThread.isAlive()){
            mGraphicThread.stopThread();
        }
    }

    public void startGraphicThread(){
        mGraphicThread.start();
    }

    public void setScreenBounds(Rect screenBounds){
        mScreenBounds = screenBounds;
    }

    public Rect getScreenBounds(){
        return mScreenBounds;
    }

}
