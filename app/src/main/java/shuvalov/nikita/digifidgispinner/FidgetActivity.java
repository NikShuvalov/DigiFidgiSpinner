package shuvalov.nikita.digifidgispinner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import shuvalov.nikita.digifidgispinner.runner_game.RunnerActivity;

public class FidgetActivity extends AppCompatActivity implements DigiFidgiSurfaceView.DigiFidgiWidgiCallback {
    private FrameLayout mMainFrame;
    private Vibrator mVibrator;
    private DigiFidgiSurfaceView mFidgiSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fidget);
        findViews();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpOverlay();
        SpinnerHandler.getInstance().stopSpinner();

    }

    private void findViews(){
        mMainFrame = (FrameLayout) findViewById(R.id.fragment_container);
    }

    private void setUpOverlay(){
        mFidgiSurfaceView= new DigiFidgiSurfaceView(this, this);
        mMainFrame.addView(mFidgiSurfaceView);
    }

    @Override
    public void onCriticalSpeed(float rpm) {
        if(getSharedPreferences(AppConstants.PREFERENCES, MODE_PRIVATE).getBoolean(AppConstants.PREF_VIBRATE, true)) {
            mVibrator.vibrate(50);
        }
    }

    @Override
    public void onOptionSelected(int i) {
        switch(i){
            case 0:
                if(!SpinnerHandler.getInstance().getSpinner().addCorner()){
                    Toast.makeText(this, "I can't let you do that", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if(!SpinnerHandler.getInstance().getSpinner().removeCorner()){
                    Toast.makeText(this, "Those are rookie numbers; you're not a rookie!", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREFERENCES, MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(AppConstants.PREF_VIBRATE, !sharedPreferences.getBoolean(AppConstants.PREF_VIBRATE, true)).apply();
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFidgiSurfaceView.stopGraphicThread();
        mMainFrame.removeView(mFidgiSurfaceView);
    }
}
