package com.github.shchurov.horizontalwheelview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import java.util.Locale;

public class SampleActivity extends Activity {

    private HorizontalWheelView horizontalWheelView;
    private TextView tvAngle;
    private ImageView ivRocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initViews();
        setupListeners();
        updateUi();
    }

    private void initViews() {
        horizontalWheelView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
        tvAngle = (TextView) findViewById(R.id.tvAngle);
        ivRocket = (ImageView) findViewById(R.id.ivRocket);
    }

    private void setupListeners() {
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateUi();
            }
        });
    }

    private void updateUi() {
        updateText();
        updateImage();
    }

    private void updateText() {
        String text = String.format(Locale.US, "%.0f°", horizontalWheelView.getDegreesAngle());
        tvAngle.setText(text);
    }

    private void updateImage() {
        float angle = (float) horizontalWheelView.getDegreesAngle();
        ivRocket.setRotation(angle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateUi();
    }

}
