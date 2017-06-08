package shuvalov.nikita.digifidgispinner;

import android.graphics.PointF;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by NikitaShuvalov on 5/30/17.
 */

public class AppConstants {
    public static final String PREFERENCES = "Shared Preferences";
    public static final String PREF_HIGH_SCORE = "High Score";

    public static float getAngle(PointF circleCenter, float x, float y){
        float adjacent = x- circleCenter.x;
        float opposite = y - circleCenter.y;
        if(opposite == 0 && adjacent>=0){
            return 0f;
        }else if (opposite ==  0 && adjacent <0){
            return 270f;
        }
        float angle = (float)(Math.toDegrees((Math.atan(opposite/adjacent))));
        if(x <= circleCenter.x && y<=circleCenter.y){
            angle = angle + 180;
        }else if (x< circleCenter.x && y> circleCenter.y){
            angle = 180 + angle;
        }else if (x> circleCenter.x && y<=circleCenter.y){
            angle = 360 + angle;
        }
        return 360 - angle;
    }

    public static PointF getCoords(PointF spinnerCenter, float degrees, float radius){
        float radians = (float)Math.toRadians(degrees);

        float x =(float) Math.sin(radians) * radius;
        float y =(float) Math.cos(radians) * radius;

        x+= spinnerCenter.x;
        y+= spinnerCenter.y;

        return new PointF(x,y);
    }


}
