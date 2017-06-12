package shuvalov.nikita.digifidgispinner.runner_game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.Nullable;


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
    private boolean mGameOver, mLastWords;
    private int mJumpDuration;
    private int mEndHeight; //Height of the last platform, going to be used to keep from creating platforms that are too high for the player to hop.
    private float mDistance;
    private long mTimeLeft;
    private boolean mGameActive;
    private boolean mDemoMode;
    private ScoreCallback mScoreCallback;

    private static final int MAX_HEIGHT_DIFF = 380; //Total jump height capable is 400. So I should allow a window.
    private static final float REFRESH_RATE = 30/1000;
    private static final float GRAVITY = 4f;

    //====================================== Constructors/Set-up =======================================================
    public RunnerEngine(Spinner spinner, @Nullable ScoreCallback scoreCallback, boolean demoMode) {
        mDemoMode = demoMode;
        mSpinner = spinner;
        mLastUpdate = SystemClock.elapsedRealtime();
        mScoreCallback = scoreCallback;
        createPaints();
    }

    public RunnerEngine(@Nullable ScoreCallback scoreCallback, boolean demoMode){
        mDemoMode = demoMode;
        mScoreCallback = scoreCallback;
        mSpinner = new Spinner(new PointF(0, 0), 0, 3);
        mLastUpdate = SystemClock.elapsedRealtime();
        createPaints();

    }

    public void setScreen(Rect screenBounds){
        mScreenBounds = screenBounds;
        finishStartUp();
    }

    private void finishStartUp(){
        mLastUpdate = SystemClock.elapsedRealtime();
        mMapHeights = new ArrayList<>();
        mSectionLength = mScreenBounds.width()/10;
        mStartPoint = 0;
        mReloadPoint = -mSectionLength *20;
        if(mDemoMode){
            mSpinner.setRpm(5.5f);
            generateDemoMap();
        }else {
            generateStartMap();
        }
        adjustSpinner();
    }

    //===================================== Map Generation/Graphics ========================================================
    private void createPaints(){
        mTerrainPaint = new Paint();
        mTerrainPaint.setColor(Color.argb(255, 237, 178, 90));

        mOutlinePaint = new Paint();
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(4f);
    }

    private void generateStartMap(){
        mMapHeights.clear();
        int startPlatformHeight = (int)(mScreenBounds.centerY() *1.5f);
        for(int i = 0; i<15; i ++){
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


    // ===================================== Demo methods ============================================
    //FixMe: Needs major tinkering to work properly, due to the face I usually just draw everything offscreen, for the Demo I need to draw everything within a fixed window.
    private void generateDemoMap(){
        int startPlatformHeight = (int)(mScreenBounds.centerY() *1.5f);
        int maxPlatformHeight = mScreenBounds.centerY();
        Random rng = new Random();
        int platHeight =  (int)(mScreenBounds.height()*.9)- rng.nextInt(maxPlatformHeight);
        for(int i = 0; i<15; i ++){
            mEndHeight = startPlatformHeight;
            mMapHeights.add(startPlatformHeight);
        }
        for(int i =0; i < 3; i ++){
            mMapHeights.add(-1);
        }
        for(int i = 0; i <10; i ++){
            mMapHeights.add(platHeight);
        }
        for(int i =0; i < 3; i ++){
            mMapHeights.add(-1);
        }
    }

    public void runDemoMode() {
        long currentTime = SystemClock.elapsedRealtime();
        long elapsedTime = currentTime - mLastUpdate;
        if (elapsedTime > REFRESH_RATE && !mMapHeights.isEmpty()) {
            mLastUpdate = currentTime;
            float distanceCovered = mSpinner.getRpm() * elapsedTime / 4;
            mStartPoint += distanceCovered;
            demoMoveSpinner(mSpinner, elapsedTime);
            if(mStartPoint >= (mMapHeights.size()/2) * mSectionLength){
                while(mStartPoint>mSectionLength){
                    mStartPoint+=mSectionLength;
                    mMapHeights.set(mMapHeights.size()-1, mMapHeights.get(0));
                    mMapHeights.remove(0);
                }
                mSpinner.setRpm(5.5f);
            }
        }
    }

    private void demoMoveSpinner(Spinner spinner, long elapsedTime){
        float terrainHeight = getTerrainHeightAtX(getRelativePositionX());
        float stableYCenter = terrainHeight - spinner.getCombinedRadius();
        if(terrainHeight <0){
            mSpinner.getCenter().offset(0, -20);
        }else{
            mSpinner.addYVelocity(GRAVITY*elapsedTime/10000);
            float deltaY = mSpinner.getYVelocity() * elapsedTime;
            float newYPosition = deltaY + mSpinner.getCenter().y;
            if(newYPosition>stableYCenter){
                newYPosition = stableYCenter;
            }
            mSpinner.setCenter(mSectionLength, newYPosition);
        }
    }

    public Canvas drawDemoTerrain(Canvas canvas){
        Path path  = new Path();
        path.moveTo(mScreenBounds.left, mScreenBounds.bottom);
        boolean dropOff = true;
        float xTracking;
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
        path.lineTo(mScreenBounds.right, mScreenBounds.bottom);
        path.close();
        canvas.drawPath(path, mTerrainPaint);
        canvas.drawPath(path, mOutlinePaint);
        return canvas;
    }

    //=================================== Main Game Methods =========================================================
    public void run(){
        long currentTime = SystemClock.elapsedRealtime();
        long elapsedTime = currentTime - mLastUpdate;
        if(elapsedTime > REFRESH_RATE){
            if(!mGameOver && mGameActive) {
                mLastUpdate = currentTime;
                float distanceCovered = mSpinner.getRpm() * elapsedTime / 4;
                mStartPoint += distanceCovered;
                mDistance += distanceCovered;
                mTimeLeft -= elapsedTime;
                if (mTimeLeft < 0) {
                    onGameOver();
                }
                loadNextSectionIfNecessary();
                moveSpinner(mSpinner, elapsedTime);
            }
            else if (mGameOver && mGameActive){
                if(!mLastWords){
                    mJumpDuration=0;
                    mLastWords = true;
                }
                if(mJumpDuration< 20) {
                    mSpinner.getCenter().offset(0, -20);
                    mJumpDuration++;
                } else{
                    mSpinner.addYVelocity(GRAVITY*elapsedTime/10000);
                    float deltaY = mSpinner.getYVelocity() * elapsedTime;
                    float newYPosition = deltaY + mSpinner.getCenter().y;
                    mSpinner.setCenter(mSectionLength, newYPosition);
                    if(newYPosition> mSpinner.getCombinedRadius() + mScreenBounds.height()){
                        mGameActive=false;
                    }
                }
            }
        }
    }

    private void onGameOver(){
        mGameOver = true;
        mSpinner.stop();
        mSpinner.clearYVelocity();
        mScoreCallback.saveIfHighScore((int)getDistance());
    }
    private void moveSpinner(Spinner spinner, long elapsedTime){
        PointF spinnerCenter = spinner.getCenter();
        float stableYCenter = getTerrainHeightAtX(getRelativePositionX()) - spinner.getCombinedRadius();
        if(stableYCenter < 0){
            stableYCenter = mScreenBounds.height() - spinner.getCombinedRadius();
        }
        if(checkIfFellInPit(spinnerCenter, stableYCenter)){
            onGameOver();
        }else {
            if (mIsJumping && mJumpDuration < 20) {
                spinner.getCenter().offset(0, -20); //FixMe: This should change based on screensize, other screens will not work so well with this.
                spinner.clearYVelocity();
                mJumpDuration++;
            } else if (stableYCenter > spinnerCenter.y) {
                spinner.addYVelocity(GRAVITY * elapsedTime / 1000);
            }
            float deltaY = spinner.getYVelocity() * elapsedTime;
            float newYPosition = deltaY + spinnerCenter.y;
            if (newYPosition > stableYCenter) {
                spinner.getCenter().set(mSectionLength, stableYCenter);
                mIsJumping = false;
                mAirborne = false;
                mJumpDuration = 0;
            } else {
                spinner.getCenter().set(mSectionLength, newYPosition);
            }
        }
    }

    public void startGame(){
        mTimeLeft = 60000;
        mLastUpdate = SystemClock.elapsedRealtime();
        mStartPoint = 0;
        mLastWords = false;
        mJumpDuration = 0;
        mAirborne = false;
        mIsJumping = false;
        mSpinner.clearYVelocity();
        mSpinner.stop();
        generateStartMap();
        adjustSpinner();
        mGameOver = false;
        mGameActive = true;

    }

    public void startJump(){
        if(!mIsJumping && !mAirborne){
            mIsJumping= true;
            mAirborne = true;
        }
    }

    //=================================== Getter/Setters =========================================================
    public Rect getScreenBounds() {
        return mScreenBounds;
    }

    public Spinner getSpinner() {
        return mSpinner;
    }

    public long getTimeLeft() {
        return mTimeLeft;
    }

    public boolean isGameActive() {
        return mGameActive;
    }


    public boolean isGameOver(){
        return mGameOver;
    }

    public float getDistance(){
        return Math.abs(mDistance/mSectionLength);
    }

    public void stopJump(){
        mIsJumping = false;
    }


    //=================================== Utils =========================================================

    private int getTerrainHeightAtX(float x){
        if (x == 0){
            return mMapHeights.get(0);
        }
        int section = (int)(x/mSectionLength);
        return mMapHeights.get(section+1);
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

    private boolean checkIfFellInPit(PointF spinnerCenter, float stableYCenter){
        if(mGameActive) {
            float terrainHeight = getTerrainHeightAtX(getRelativePositionX());
            if (terrainHeight == -1) {
                terrainHeight = mScreenBounds.height();
            }
            return (spinnerCenter.y > terrainHeight || (terrainHeight == mScreenBounds.height() && spinnerCenter.y == stableYCenter)); //First half asks if the player is under the terrain surface, other asks if they've landed in a pit.
        }
        return false;
    }

    private void adjustSpinner(){
        mSpinner.setRadius(mScreenBounds.height()/18);
        mSpinner.setCenter(new PointF(mSectionLength, mMapHeights.get(0) - mSpinner.getCombinedRadius()));
    }

    private float getRelativePositionX(){
        return Math.abs(mStartPoint - mSpinner.getCenter().x);
    }

    public interface ScoreCallback{
        void saveIfHighScore(int score);
    }
}
