package shuvalov.nikita.digifidgispinner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class FidgiSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GraphicThread mGraphicThread;
    private Paint mPaint, mPaint2;
    private PointF mCirclePosition;
    private Spinner mSpinner;
    private RectF mOval;



    public FidgiSurfaceView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint2 = new Paint();
        mPaint2.setColor(Color.BLACK);
        mCirclePosition = new PointF(500,500);
        float radius = 250;
        mSpinner = new Spinner(mCirclePosition,radius, 3);
        mOval = new RectF(mCirclePosition.x - radius, mCirclePosition.y -radius, mCirclePosition.x+radius, mCirclePosition.y+radius);
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
        drawSpinner(canvas);
        //Make the drawing of the thing
    }

    private void drawSpinner(Canvas canvas){
        PointF[] bearingCenters = mSpinner.getBearingCenters();
        float bearingRadius = mSpinner.getBearingRadius();
        PointF spinnerCenter = mSpinner.getCenter();
        for(int i =0; i< bearingCenters.length; i++){
            PointF bearingCenter = bearingCenters[i];
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius, mPaint);
            canvas.drawCircle(bearingCenter.x, bearingCenter.y, bearingRadius/2, mPaint2);
        }
        canvas.drawCircle(spinnerCenter.x, spinnerCenter.y, mSpinner.getRadius()/4, mPaint2);
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
