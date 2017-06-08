package shuvalov.nikita.digifidgispinner;

import android.os.SystemClock;

/**
 * Created by NikitaShuvalov on 6/1/17.
 */

public class SpinnerHandler {
    private Spinner mSpinner;


    private SpinnerHandler(){
    }
    private static SpinnerHandler sSpinnerHandler;

    public static SpinnerHandler getInstance() {
        if(sSpinnerHandler== null){
            sSpinnerHandler = new SpinnerHandler();
        }
        return sSpinnerHandler;
    }

    public Spinner getSpinner() {
        return mSpinner;
    }

    public void setSpinner(Spinner spinner) {
        mSpinner = spinner;
    }

    public void stopSpinner(){
        mSpinner.stop();
    }
}
