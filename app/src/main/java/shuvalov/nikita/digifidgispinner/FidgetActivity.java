package shuvalov.nikita.digifidgispinner;

import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

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
        if(SpinnerHandler.getInstance().getSpinner()!=null) {
            SpinnerHandler.getInstance().stopSpinner();
        }
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
        mVibrator.vibrate(50);
    }

    @Override
    public void onOptionSelected(int i) {
        switch(i){
            case 0:
                SpinnerHandler.getInstance().getSpinner().addCorner();
                break;
            case 1:
                SpinnerHandler.getInstance().getSpinner().removeCorner();
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
