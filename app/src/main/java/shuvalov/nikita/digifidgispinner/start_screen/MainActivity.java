package shuvalov.nikita.digifidgispinner.start_screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import shuvalov.nikita.digifidgispinner.AppConstants;
import shuvalov.nikita.digifidgispinner.CustomSurfaceView;
import shuvalov.nikita.digifidgispinner.DigiFidgiSurfaceView;
import shuvalov.nikita.digifidgispinner.FidgetActivity;
import shuvalov.nikita.digifidgispinner.R;
import shuvalov.nikita.digifidgispinner.Spinner;
import shuvalov.nikita.digifidgispinner.SpinnerHandler;
import shuvalov.nikita.digifidgispinner.runner_game.RunnerActivity;
import shuvalov.nikita.digifidgispinner.runner_game.RunnerEngine;
import shuvalov.nikita.digifidgispinner.runner_game.RunnerSurfaceView;

public class MainActivity extends AppCompatActivity
        implements MainSurfaceView.Callback, RunnerEngine.ScoreCallback, DigiFidgiSurfaceView.DigiFidgiWidgiCallback
{
    private FrameLayout mContainer;
    private CustomSurfaceView mCustomSurfaceView;
    private Vibrator mVibrator;
    private boolean mSelectMode;

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
        mSelectMode = true;
        if(mContainer.getChildCount()>0) {
            mContainer.removeView(mCustomSurfaceView);
        }
        mCustomSurfaceView = new MainSurfaceView(this, this);
        mContainer.addView(mCustomSurfaceView);
    }

    //==============================================================================================
    // Launch Screen Callbacks
    // ==============================================================================================

    @Override
    public void onGameSelected() {
        mCustomSurfaceView.stopGraphicThread();
        mSelectMode = false;
//        startGameView();
        Intent intent = new Intent(this, RunnerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCasualSelected() {
        mCustomSurfaceView.stopGraphicThread();
        switchToSpinnerMode();
//        Intent intent = new Intent(this, FidgetActivity.class);
//        startActivity(intent);
    }

    // ==============================================================================================
    // Fidget Spinner Callbacks
    // ==============================================================================================
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
    // ==============================================================================================
    // Runner CallBacks
    // ==============================================================================================

    @Override
    public void saveIfHighScore(int score) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREFERENCES, Context.MODE_PRIVATE);
        int lastHighScore = sharedPreferences.getInt(AppConstants.PREF_HIGH_SCORE, 0);
        if(score> lastHighScore){
            sharedPreferences.edit().putInt(AppConstants.PREF_HIGH_SCORE, score).apply();
        }
    }

    // ==============================================================================================
    // Runner Set-Up Methods
    // ==============================================================================================
    private void startGameView(){
        mContainer.removeView(mCustomSurfaceView);
        mCustomSurfaceView = new RunnerSurfaceView(this, getRunnerEngine());
        mContainer.addView(mCustomSurfaceView);
        Log.d("Hi", "startGameView: " + mContainer.getChildCount());
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

    // ==============================================================================================
    // Spinner Set-Up Methods
    // ==============================================================================================
    private void switchToSpinnerMode(){
        if(mVibrator==null) {
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        SpinnerHandler.getInstance().stopSpinner();
        mContainer.removeView(mCustomSurfaceView);
        mCustomSurfaceView = new DigiFidgiSurfaceView(this, this);
        mContainer.addView(mCustomSurfaceView);
        mSelectMode = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCustomSurfaceView.stopGraphicThread();
        mContainer.removeView(mCustomSurfaceView);
    }

    @Override
    public void onBackPressed() {
        if(!mSelectMode){
            setUpSurfaceView();
        }else{
            super.onBackPressed();
        }
    }
}

