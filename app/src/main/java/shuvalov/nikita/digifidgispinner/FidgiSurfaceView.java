package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
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
    private Spinner mSpinner;
    private RectF mOval;



    public FidgiSurfaceView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mCirclePosition = new PointF(500,500);
        mSpinner = new Spinner(null, mCirclePosition,200);
        mOval = new RectF(mCirclePosition.x - 100, mCirclePosition.y -100, mCirclePosition.x+100, mCirclePosition.y+100);
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

        canvas.drawArc(mOval,0, mSpinner.getAngle(),true, mPaint);
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
                mSpinner.setAngle(AppConstants.getAngle(mSpinner.getCenter(),event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_MOVE:
                mSpinner.setAngle(AppConstants.getAngle(mSpinner.getCenter(),event.getX(), event.getY()));
                break;
        }
        return true;
    }
}
