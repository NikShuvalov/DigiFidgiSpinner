package shuvalov.nikita.digifidgispinner.runner_game;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;
import shuvalov.nikita.digifidgispinner.SpinnerHandler;

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
        Paint paint1 = new Paint();
        paint1.setColor(Color.RED);

        Paint paint2 = new Paint();
        paint2.setColor(Color.BLACK);


        Paint bodyPaint = new Paint();
        bodyPaint.setColor(Color.argb(255,100,100,100));
        bodyPaint.setStyle(Paint.Style.FILL);

        Spinner spinner = new Spinner(new PointF(500, 500),10f, 3, bodyPaint, paint1, paint2);
        return new RunnerEngine(spinner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunnerSurfaceView.stopThread();
        mGameContainer.removeAllViews();
        SpinnerHandler.getInstance().stopSpinner();
    }
}
