package shuvalov.nikita.digifidgispinner;

import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.helicopter_game.HelicopterActivity;

public class MainActivity extends AppCompatActivity implements FidgiSurfaceView.DigiFidgiWidgiCallback {
    private FrameLayout mMainFrame;
    private Vibrator mVibrator;
    private FidgiSurfaceView mFidgiSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpOverlay();
    }

    private void findViews(){
        mMainFrame = (FrameLayout) findViewById(R.id.fragment_container);
    }

    private void setUpOverlay(){
        mFidgiSurfaceView= new FidgiSurfaceView(this, this);
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
            case 2:
                mFidgiSurfaceView.stopGraphicThread();
                Intent heliIntent = new Intent(this, HelicopterActivity.class);
                startActivity(heliIntent);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainFrame.removeAllViews();
    }
}
