package com.github.shchurov.horizontalwheelview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

public class SampleActivity extends Activity {

    private HorizontalWheelView horizontalScrollView;
    private TextView tvAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        horizontalScrollView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
        tvAngle = (TextView) findViewById(R.id.tvAngle);
        horizontalScrollView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateAngleText();
            }
        });
        updateAngleText();
    }

    private void updateAngleText() {
        String text = String.format(Locale.US, "%.1f°", horizontalScrollView.getDegreesAngle());
        tvAngle.setText(text);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateAngleText();
    }

}
