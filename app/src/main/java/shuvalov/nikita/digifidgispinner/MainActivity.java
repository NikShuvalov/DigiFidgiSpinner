package shuvalov.nikita.digifidgispinner;

import android.media.AudioAttributes;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements FidgiSurfaceView.SpeedListener {
    private FrameLayout mFrameLayout;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
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
        mFrameLayout = (FrameLayout) findViewById(R.id.fragment_container);
    }

    private void setUpOverlay(){
        FidgiSurfaceView fidgiSurfaceView= new FidgiSurfaceView(this, this);
        mFrameLayout.addView(fidgiSurfaceView);
    }

    @Override
    public void onCriticalSpeed(float rpm) {
        mVibrator.vibrate(50);
    }
}
