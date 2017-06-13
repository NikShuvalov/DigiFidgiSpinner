package shuvalov.nikita.digifidgispinner.runner_game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.AppConstants;
import shuvalov.nikita.digifidgispinner.CustomSurfaceView;
import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;

public class RunnerActivity extends AppCompatActivity implements RunnerEngine.ScoreCallback{
    private FrameLayout mGameContainer;
    private CustomSurfaceView mRunnerSurfaceView;
    private boolean mResumed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner);
        mResumed = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mResumed) {
            findViews();
            setUpSurfaceView();
        }
        mResumed = true;
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
        return new RunnerEngine(this, spinner, this, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunnerSurfaceView.stopGraphicThread();
        mGameContainer.removeAllViews();
        mResumed = false;
    }


    @Override
    public void saveIfHighScore(int score) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREFERENCES, Context.MODE_PRIVATE);
        int lastHighScore = sharedPreferences.getInt(AppConstants.PREF_HIGH_SCORE, 0);
        if(score> lastHighScore){
            sharedPreferences.edit().putInt(AppConstants.PREF_HIGH_SCORE, score).apply();
        }
    }
}
