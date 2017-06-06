package shuvalov.nikita.digifidgispinner.helicopter_game;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import shuvalov.nikita.digifidgispinner.R;

public class HelicopterActivity extends AppCompatActivity {
    private FrameLayout mContainer;
    private GameSurfaceView mGameSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helicopter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        findViews();
        setUpSurfaceView(getGameEngine());
    }

    private void findViews(){
        mContainer = (FrameLayout)findViewById(R.id.container);
    }

    private void setUpSurfaceView(GameEngine gameEngine){
        mGameSurfaceView = new GameSurfaceView(this, gameEngine);
        mContainer.addView(mGameSurfaceView);
    }

    private GameEngine getGameEngine(){
        Helicopter helicopter = new Helicopter(100, 100, 1);
        return new GameEngine(helicopter, GameEngine.Difficulty.EASY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGameSurfaceView.stopThread();
    }
}
