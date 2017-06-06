package shuvalov.nikita.digifidgispinner;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;

import java.util.Random;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class GameEngine  {
    private Helicopter mHelicopter;
    private int[] mMapHeights;
    private char[] mItemSpawn;
    private Difficulty mDifficulty;
    private Rect mScreenBounds;
    private float mStartPoint, mReloadPoint, mSectionLength;
    private long mLastUpdate;
    private Paint mTerrainPaint;

    public static final float REFRESH_RATE = 30/1000; //30fps

    public enum Difficulty{
        EASY, NORMAL, HARD
    }

    public GameEngine(Helicopter helicopter, int onScreenSections, Difficulty difficulty, Rect screenBounds){
        mHelicopter = helicopter;
        mMapHeights = new int[onScreenSections + 2]; //Add 1 offscreen in each direction
        mItemSpawn = new char[onScreenSections + 2]; //Add 1 offscreen in each direction
        mDifficulty = difficulty;
        mScreenBounds = screenBounds;
        generateStartMap();
        createPaint();
        mSectionLength = screenBounds.width()/onScreenSections;
        mStartPoint = -mSectionLength;
        mReloadPoint = -mSectionLength * onScreenSections/2;
        mLastUpdate = SystemClock.elapsedRealtime();
    }

    private void createPaint(){
        mTerrainPaint = new Paint();
        mTerrainPaint.setColor(Color.argb(255, 237, 178, 90));
    }

    private void generateStartMap(){
        int terrainMaxHeight = mScreenBounds.height()/2;
        Random rng = new Random();
        for(int i = 0; i< mMapHeights.length; i++){
            mMapHeights[i] = mScreenBounds.height() - rng.nextInt(terrainMaxHeight);
            mItemSpawn[i] = placeItem();
        }
    }

    private void loadNextSection(){
        int terrainMaxHeight = mScreenBounds.height()/2;
        Random rng = new Random();
        for(int i = 0; i< mMapHeights.length-1; i ++){
            mMapHeights[i] = mMapHeights[i+1];
            mItemSpawn[i] = mItemSpawn[i+1];
        }
        mMapHeights[mMapHeights.length-1] = mScreenBounds.height() - rng.nextInt(terrainMaxHeight);
        mItemSpawn[mItemSpawn.length-1] = placeItem();
    }

    private char placeItem(){
        Random rng = new Random();
        float roll=0;
        switch(mDifficulty){
            case EASY:
                roll = rng.nextFloat();
                break;
            case NORMAL:
                roll = rng.nextFloat() - .2f;
                break;
            case HARD:
                roll = rng.nextFloat() - .3f;
                break;
        }
        if(roll > .5f){
            return 'a';
        }else{
            return 'b';
        }
    }

    private void loadNextSectionsIfNecessary(){
        if(mStartPoint <= mReloadPoint){
            loadNextSection();
            refocus();
        }
    }

    private void refocus(){
        mStartPoint += mSectionLength;
        PointF copterPos = mHelicopter.getLocation();
        copterPos.offset(mSectionLength, 0);
        mHelicopter.setLocation(copterPos);
    }

    public void run(){
        long currentTime = SystemClock.elapsedRealtime();
        long elapsedTime = currentTime -mLastUpdate;
        if(elapsedTime > REFRESH_RATE){
            moveTheChopper(elapsedTime);
            //Other run logic
            mLastUpdate = currentTime;
        }

    }
    private void moveTheChopper(long elapsedTime){
        PointF chopperLoci = mHelicopter.getLocation();
        float horizontalForce = mHelicopter.getHorizontalVelocity();
        float distanceX = horizontalForce * elapsedTime;

        float weight = mHelicopter.getWeight();
        float gravityForce = weight * 10;
        float rpm =SpinnerHandler.getInstance().getSpinner().getRpm();
        float liftForce = rpm *10;
        float netVerticalForce = liftForce -gravityForce;
        float distanceY = netVerticalForce * elapsedTime;

        chopperLoci.offset(distanceX, distanceY);
        mStartPoint-= distanceX;
        int terrainHeight = getTerrainHeightAtX(getHelicopterRelativeX());
        if(mHelicopter.getLocation().y >= terrainHeight){
            //ToDo: Add horizontal force to damage as well;
            int netForce = (int)(netVerticalForce + horizontalForce);
            helicopterLanded(netForce);
        }
    }

    private void helicopterLanded(int netForce){
        mHelicopter.takeDamage(netForce);

        //Do whatever else should be done here. Stop moving the helicopter and shit.

    }
    private Path drawTerrainPath(){
        Path terrainPath = new Path();

        terrainPath.moveTo(mStartPoint , mScreenBounds.height());
        terrainPath.rLineTo(0, mMapHeights[0]);

        for(int i = 1; i< mMapHeights.length; i++){
            terrainPath.lineTo(mStartPoint + mSectionLength*i, mMapHeights[i]);
        }
        terrainPath.rLineTo(0, mMapHeights[mMapHeights.length-1]);
        terrainPath.close();
        return terrainPath;
    }

    private int getTerrainHeightAtX(float x){
        if (x == 0){
            return mMapHeights[0];
        }
        float sectionProgress = x % mSectionLength;
        if(0 == sectionProgress){
            return mMapHeights[(int)(x/mSectionLength)];
        }
        int section = (int)(x/mSectionLength);
        float incline = (mMapHeights[section] - mMapHeights[section+1])/mSectionLength;
        return (int)(mMapHeights[section] + (sectionProgress * incline));
    }

    private float getHelicopterRelativeX(){
        return mStartPoint + mHelicopter.getLocation().x;
    }

    public Paint getTerrainPaint() {
        return mTerrainPaint;
    }
}
