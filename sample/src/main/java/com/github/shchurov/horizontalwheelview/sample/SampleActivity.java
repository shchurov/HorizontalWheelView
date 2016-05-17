package com.github.shchurov.horizontalwheelview.sample;

import android.app.Activity;
import android.os.Bundle;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

public class SampleActivity extends Activity {

    private HorizontalWheelView horizontalScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        horizontalScrollView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
    }

}
