package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static android.content.ContentValues.TAG;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class FidgiSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicThread mGraphicThread;
    private Paint mPaint;
    private PointF mCirclePosition;


    public FidgiSurfaceView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mCirclePosition = new PointF(500,500);
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
        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(mCirclePosition.x, mCirclePosition.y,200, mPaint);
        //Make the drawing of the thing
    }

    public void stopGraphicThread(){
        if(mGraphicThread!=null && mGraphicThread.isAlive()){
            mGraphicThread.stopThread();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = MotionEventCompat.getActionIndex(event);
        int id = event.getPointerId(index);
        int action = MotionEventCompat.getActionMasked(event);
        switch(action){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mCirclePosition.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mCirclePosition.set(event.getX(), event.getY());
                break;
        }
        return true;
    }
}
