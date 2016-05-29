package com.github.shchurov.horizontalwheelview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

public class SampleActivity extends Activity {

    private HorizontalWheelView horizontalWheelView;
    private TextView tvAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initViews();
        setupListeners();
        updateAngleText(horizontalWheelView, tvAngle);
    }

    private void initViews() {
        horizontalWheelView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
        tvAngle = (TextView) findViewById(R.id.tvAngle);
    }

    private void setupListeners() {
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateAngleText(horizontalWheelView, tvAngle);
            }
        });
    }

    private void updateAngleText(HorizontalWheelView horizontalWheelView, TextView tvAngle) {
        String text = String.format(Locale.US, "%.0f°", horizontalWheelView.getDegreesAngle());
        tvAngle.setText(text);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateAngleText(horizontalWheelView, tvAngle);
    }

}
