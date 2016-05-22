package com.github.shchurov.horizontalwheelview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

public class SampleActivity extends Activity {

    private HorizontalWheelView horizontalWheelView1;
    private TextView tvAngle1;
    private HorizontalWheelView horizontalWheelView2;
    private TextView tvAngle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initViews();
        setupListeners();
        updateAngleText(horizontalWheelView1, tvAngle1);
        updateAngleText(horizontalWheelView2, tvAngle2);
    }

    private void initViews() {
        horizontalWheelView1 = (HorizontalWheelView) findViewById(R.id.horizontalWheelView1);
        tvAngle1 = (TextView) findViewById(R.id.tvAngle1);
        horizontalWheelView2 = (HorizontalWheelView) findViewById(R.id.horizontalWheelView2);
        tvAngle2 = (TextView) findViewById(R.id.tvAngle2);
    }

    private void setupListeners() {
        horizontalWheelView1.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateAngleText(horizontalWheelView1, tvAngle1);
            }
        });
        horizontalWheelView2.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateAngleText(horizontalWheelView2, tvAngle2);
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
        updateAngleText(horizontalWheelView1, tvAngle1);
        updateAngleText(horizontalWheelView2, tvAngle2);
    }

}
