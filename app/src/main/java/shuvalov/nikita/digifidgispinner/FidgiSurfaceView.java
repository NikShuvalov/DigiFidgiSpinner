package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class FidgiSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicThread mGraphicThread;


    public FidgiSurfaceView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(mGraphicThread!=null){return;}
        mGraphicThread = new GraphicThread(surfaceHolder,this);
        mGraphicThread.start();
        //Draw the FidgetSpinner and start the engine I guess
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mGraphicThread.stopThread();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Make the drawing of the thing
    }

    public void stopGraphicThread(){
        if(mGraphicThread!=null && mGraphicThread.isAlive()){
            mGraphicThread.stopThread();
        }
    }
}
