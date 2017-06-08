package shuvalov.nikita.digifidgispinner.start_screen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.FidgetActivity;
import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.SpinnerHandler;
import shuvalov.nikita.digifidgispinner.runner_game.RunnerActivity;

public class MainActivity extends AppCompatActivity implements MainSurfaceView.Callback {
    private FrameLayout mContainer;
    private MainSurfaceView mMainSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        findViews();
        setUpSurfaceView();
    }

    private void findViews(){
        mContainer = (FrameLayout)findViewById(R.id.surface_view_container);
    }

    private void setUpSurfaceView(){
        mMainSurfaceView = new MainSurfaceView(this, this);
        mContainer.addView(mMainSurfaceView);
    }

    @Override
    public void onGameSelected() {
        Intent intent = new Intent(this, RunnerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCasualSelected() {
        Intent intent = new Intent(this, FidgetActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainSurfaceView.stopThread();
        mContainer.removeView(mMainSurfaceView);
    }

}

