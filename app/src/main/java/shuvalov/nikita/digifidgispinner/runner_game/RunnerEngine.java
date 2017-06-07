package shuvalov.nikita.digifidgispinner.runner_game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
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
    private boolean mIsJumping, mAirborne;
    private boolean mGameOver;
    private int mJumpDuration;
    private int mEndHeight; //Height of the last platform, going to be used to keep from creating platforms that are too high for the player to hop.
    private float mDistance;
    private long mTimeLeft;
    private boolean mGameStarted;

    private static final int MAX_HEIGHT_DIFF = 380; //Total jump height capable is 400. So I should allow a window.
    private static final float REFRESH_RATE = 30/1000;
    private static final float GRAVITY = 4f;

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
        adjustSpinner();
    }

    private void adjustSpinner(){
        mSpinner.setRadius(mScreenBounds.height()/18);
        mSpinner.setCenter(new PointF(mSectionLength, mMapHeights.get(0) - mSpinner.getCombinedRadius()));
        mAirborne = false;
        mJumpDuration = 0;
        mGameOver = false;
    }

    private void generateStartMap(){
        int startPlatformHeight = (int)(mScreenBounds.centerY() *1.5f);
        for(int i = 0; i<10; i ++){
            mEndHeight = startPlatformHeight;
            mMapHeights.add(startPlatformHeight);
        }
        preloadNextSection();
        mDistance = 0;
    }

    private void preloadNextSection(){
        int maxPlatformHeight = mScreenBounds.centerY();
        Random rng = new Random();
        boolean platform = mMapHeights.get(mMapHeights.size()-1) >=0;
        while(mMapHeights.size()<40){
            if(!platform) {
                int platLength = rng.nextInt(10) + 2;
                int platHeight = (int)(mScreenBounds.height()*.9) - rng.nextInt(maxPlatformHeight);
                while(Math.abs(platHeight-mEndHeight) > MAX_HEIGHT_DIFF){
                    platHeight =  (int)(mScreenBounds.height()*.9)- rng.nextInt(maxPlatformHeight);
                }
                for (int j = 0; j < platLength; j++) {
                    mMapHeights.add(platHeight);
                }
                platform = true;
                mEndHeight = platHeight;
            }else{
                int gapLength = rng.nextInt(2)+1; //FixMe: Increase size of gaps, or increase it as player progresses.
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
        if(elapsedTime > REFRESH_RATE && !mGameOver && mGameStarted){
            mLastUpdate = currentTime;
            float distanceCovered = mSpinner.getRpm() * elapsedTime/4;
            mStartPoint+= distanceCovered;
            mDistance+= distanceCovered;
            mTimeLeft-= elapsedTime;
            if(mTimeLeft<0){
                mGameOver = true;
                mSpinner.stop();
            }
            loadNextSectionIfNecessary();
            moveSpinner(mSpinner, elapsedTime);
        }else if (mGameOver){

            //Start a gameover animation/sound or something. But how?
        }
    }

    private void moveSpinner(Spinner spinner, long elapsedTime){
        float  terrainHeight = getTerrainHeightAtX(getRelativePositionX());
        if(terrainHeight == -1){
            terrainHeight = mScreenBounds.height();
        }
        PointF spinnerCenter = spinner.getCenter();
        float stableYCenter = getTerrainHeightAtX(getRelativePositionX()) - spinner.getCombinedRadius();
        if(stableYCenter < 0){
            stableYCenter = mScreenBounds.height() - spinner.getCombinedRadius();
        }
        if(spinnerCenter.y>terrainHeight){ //FixMe: Adjust if this is too unforgiving;
//            mGameOver = true;
            mSpinner.stop();
        }else if (terrainHeight == mScreenBounds.height() && spinnerCenter.y == stableYCenter){
//            mGameOver = true;
            mSpinner.stop();
        }
        if(mIsJumping && mJumpDuration < 20 && !mGameOver){
            spinner.getCenter().offset(0,-20); //FixMe: This should change based on screensize, other screens will not work so well with this.
            spinner.clearYVelocity();
            mJumpDuration++;
        }else if (stableYCenter>spinnerCenter.y && !mGameOver){
            spinner.addYVelocity(GRAVITY * elapsedTime/1000);
        }
        float deltaY = spinner.getYVelocity() * elapsedTime;
        float newYPosition = deltaY + spinnerCenter.y;
        if(newYPosition >stableYCenter && !mGameOver){
            spinner.getCenter().set(mSectionLength, stableYCenter);
            mIsJumping= false;
            mAirborne = false;
            mJumpDuration = 0;
        }else{
            spinner.getCenter().set(mSectionLength, newYPosition);
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
        return canvas;
    }

    public void startJump(){
        if(!mIsJumping && !mAirborne){
            mIsJumping= true;
            mAirborne = true;
        }
    }

    public void stopJump(){
        mIsJumping = false;
    }


    private int getTerrainHeightAtX(float x){
        if (x == 0){
            return mMapHeights.get(0);
        }
        int section = (int)(x/mSectionLength);
        return mMapHeights.get(section+1);
    }

    private float getRelativePositionX(){
        return Math.abs(mStartPoint - mSpinner.getCenter().x);
    }

    public boolean isGameOver(){
        return mGameOver;
    }

    public float getDistance(){
        return mDistance/mSectionLength;
    }

    public void startGame(){
        if(!mGameStarted) {
            mTimeLeft = 60000;
            mGameStarted = true;
            mLastUpdate = SystemClock.elapsedRealtime();
        }
    }

    public long getTimeLeft() {
        return mTimeLeft;
    }
}
