package shuvalov.nikita.digifidgispinner;

import android.graphics.PointF;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class AppConstants {

    public static double getAngle(PointF circleCenter, float x, float y){
        float adjacent = x- circleCenter.x;
        float opposite = y - circleCenter.y;
        if(opposite == 0 && adjacent>=0){
            return 0f;
        }else if (opposite ==  0 && adjacent <0){
            return 270f;
        }
        return Math.atan(opposite/adjacent);
    }
}
