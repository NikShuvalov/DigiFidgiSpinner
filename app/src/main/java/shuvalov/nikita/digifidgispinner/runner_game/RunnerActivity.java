package shuvalov.nikita.digifidgispinner.runner_game;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;

public class RunnerActivity extends AppCompatActivity {
    private FrameLayout mGameContainer;
    private RunnerSurfaceView mRunnerSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner);

    }

    @Override
    protected void onResume() {
        super.onResume();
        findViews();
        setUpSurfaceView();

    }

    private void findViews(){
        mGameContainer = (FrameLayout)findViewById(R.id.game_container);
    }

    private void setUpSurfaceView(){
        mRunnerSurfaceView = new RunnerSurfaceView(this, getRunnerEngine());
        mGameContainer.addView(mRunnerSurfaceView);
    }

    private RunnerEngine getRunnerEngine(){
        Spinner spinner = new Spinner(new PointF(0, 0),50f, 3);
        return new RunnerEngine(spinner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGameContainer.removeAllViews();
        mRunnerSurfaceView.stopThread();
    }
}
