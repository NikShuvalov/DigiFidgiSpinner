package shuvalov.nikita.digifidgispinner.runner_game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Random;

import shuvalov.nikita.digifidgispinner.Spinner;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class RunnerEngine {
    private Spinner mSpinner;
    private long mLastUpdate;
    private float mStartPoint, mReloadPoint, mSectionLength;
    private Paint mTerrainPaint, mOutlinePaint;
    private Rect mScreenBounds;
    private ArrayList<Integer> mMapHeights;
    private boolean mIsJumping;

    public static final float REFRESH_RATE = 30/1000;

    public RunnerEngine(Spinner spinner) {
        mSpinner = spinner;
        mLastUpdate = SystemClock.elapsedRealtime();
        mIsJumping = false;
    }

    public void setScreen(Rect screenBounds){
        mScreenBounds = screenBounds;
        mLastUpdate = SystemClock.elapsedRealtime();
        mMapHeights = new ArrayList<>();
        mSectionLength = mScreenBounds.width()/10;
        mStartPoint = 0;
        mReloadPoint = -mSectionLength *20;
        createPaints();
        generateStartMap();
    }

    private void generateStartMap(){
        int maxPlatformHeight = mScreenBounds.centerY();
        for(int i = 0; i<10; i ++){
            mMapHeights.add((int)(maxPlatformHeight*1.5));
        }
        preloadNextSection();

    }

    private void preloadNextSection(){
        int maxPlatformHeight = mScreenBounds.centerY();
        Random rng = new Random();
        boolean platform = mMapHeights.get(mMapHeights.size()-1) >=0;
        while(mMapHeights.size()<40){
            if(!platform) {
                int platLength = rng.nextInt(10) + 2;
                int platHeight = mScreenBounds.height() - rng.nextInt(maxPlatformHeight);
                for (int j = 0; j < platLength; j++) {
                    mMapHeights.add(platHeight);
                }
                platform = true;
            }else{
                int gapLength = rng.nextInt(2)+1;
                for(int j =0; j < gapLength; j++){
                    mMapHeights.add(-1);
                }
                platform = false;
            }
        }
    }

    private void loadNextSectionIfNecessary(){
        if(mStartPoint <= mReloadPoint){
            for(int i =19; i >-1; i--){
                mMapHeights.remove(i);
                mStartPoint+=mSectionLength;
            }
            preloadNextSection();
        }
    }

    private void createPaints(){
        mTerrainPaint = new Paint();
        mTerrainPaint.setColor(Color.argb(255, 237, 178, 90));

        mOutlinePaint = new Paint();
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(4f);
    }

    public void run(){
        long currentTime = SystemClock.elapsedRealtime();
        long elapsedTime = currentTime - mLastUpdate;
        if(elapsedTime > REFRESH_RATE){
            mLastUpdate = currentTime;
        }
    }

    public Paint getTerrainPaint() {
        return mTerrainPaint;
    }

    public Rect getScreenBounds() {
        return mScreenBounds;
    }

    public Spinner getSpinner() {
        return mSpinner;
    }

    public Canvas drawTerrain(Canvas canvas){
        Path path  = new Path();
        path.moveTo(mStartPoint, mScreenBounds.height());
        boolean dropOff = true;
        for(int i = 0; i< mMapHeights.size(); i ++){
            int val = mMapHeights.get(i);
            if (val == -1 && dropOff){
                path.lineTo(mStartPoint + mSectionLength * (i-1), mScreenBounds.height());
                dropOff= false;
            }else if (val !=-1 && !dropOff){
                path.lineTo(mStartPoint+mSectionLength* (i-1), val);
                dropOff = true;
            }
            path.lineTo(mStartPoint + mSectionLength*i, val == -1 ? mScreenBounds.height() : val);
        }
        path.lineTo(mStartPoint + mSectionLength*mMapHeights.size()-1,mScreenBounds.height());
        path.close();
        canvas.drawPath(path, mTerrainPaint);
        canvas.drawPath(path, mOutlinePaint);
        mStartPoint-=3;
        loadNextSectionIfNecessary();
        return canvas;
    }

}
