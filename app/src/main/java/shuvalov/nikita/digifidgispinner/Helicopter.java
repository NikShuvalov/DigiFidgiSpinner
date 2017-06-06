package shuvalov.nikita.digifidgispinner;

import android.graphics.PointF;

/**
 * Created by NikitaShuvalov on 6/6/17.
 */

public class Helicopter {
    private float mHorizontalVelocity;
    private float mFuel, mMaxFuel;
    private int mHealth, mMaxHealth;
    private int mCash;
    private float mWeight;
    private PointF mLocation;
    private static final int START_CASH =50;

    public Helicopter(float maxFuel,int maxHealth, float weight) {
        mMaxFuel = maxFuel;
        mFuel = maxFuel;
        mMaxHealth = maxHealth;
        mHealth = maxHealth;
        mCash = START_CASH;
        mHorizontalVelocity = 0;
        mWeight = weight;
    }

    //========================================= Getter & Setter Methods =============================================

    public float getHorizontalVelocity() {
        return mHorizontalVelocity;
    }

    public float getFuel() {
        return mFuel;
    }

    public float getMaxFuel() {
        return mMaxFuel;
    }

    public int getHealth() {
        return mHealth;
    }

    public int getMaxHealth() {
        return mMaxHealth;
    }

    public int getCash() {
        return mCash;
    }

    public float getWeight() {
        return mWeight;
    }

    public PointF getLocation() {
        return mLocation;
    }

    public void setLocation(PointF location) {
        mLocation = location;
    }

    //======================================== Adjustment Methods ==========================================

    public boolean spendCash(int cost){
        if(cost > mCash){
            return false;
        }
        mCash -= cost;
        return true;
    }

    public int takeDamage(int damage){
        mHealth -= damage;
        return mHealth;
    }

    public int repair(int repairAmount){
        return mHealth += repairAmount;
    }

    public float adjustHorizontalSpeed(float horizontalVelocity){
        return mHorizontalVelocity += horizontalVelocity;
    }

    public float burnFuel(float fuelBurn){
        return mFuel = fuelBurn > mFuel ? 0 : mFuel - fuelBurn;
    }
}
