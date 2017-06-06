package shuvalov.nikita.digifidgispinner;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FidgiSurfaceView.DigiFidgiWidgiCallback {
    private FrameLayout mMainFrame;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setUpOverlay();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void findViews(){
        mMainFrame = (FrameLayout) findViewById(R.id.fragment_container);
    }

    private void setUpOverlay(){
        FidgiSurfaceView fidgiSurfaceView= new FidgiSurfaceView(this, this);
        mMainFrame.addView(fidgiSurfaceView);
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
                Toast.makeText(this, "Gonna play that shit", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
