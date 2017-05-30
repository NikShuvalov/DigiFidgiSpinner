package shuvalov.nikita.digifidgispinner;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private FrameLayout mFrameLayout;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setUpOverlay();
    }

    private void findViews(){
        mFrameLayout = (FrameLayout) findViewById(R.id.fragment_container);
    }

    private void setUpOverlay(){
        FidgiSurfaceView fidgiSurfaceView= new FidgiSurfaceView(this);
        mFrameLayout.addView(fidgiSurfaceView);
    }
}
