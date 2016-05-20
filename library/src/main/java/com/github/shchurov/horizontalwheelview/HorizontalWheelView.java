package com.github.shchurov.horizontalwheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.PI;

public class HorizontalWheelView extends View {

    private Drawer drawer;
    private TouchHandler touchHandler;
    private double angle;
    private Listener listener;

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawer = new Drawer(this, attrs);
        touchHandler = new TouchHandler(this);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setRadiansAngle(double radians) {
        this.angle = radians % (2 * PI);
        invalidate();
        if (listener != null) {
            listener.onRotationChanged(this.angle);
        }
    }

    public void setDegreesAngle(double degrees) {
        double radians = degrees * PI / 180;
        setRadiansAngle(radians);
    }

    public double getRadiansAngle() {
        return angle;
    }

    public double getDegreesAngle() {
        return getRadiansAngle() * 180 / PI;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        drawer.onSizeChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawer.onDraw(canvas);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.angle = angle;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        angle = ss.angle;
        invalidate();
    }

    public interface Listener {
        void onRotationChanged(double radians);
    }

}
