package com.github.shchurov.horizontalwheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.PI;

public class HorizontalWheelView extends View {

    private static final int DP_DEFAULT_WIDTH = 200;
    private static final int DP_DEFAULT_HEIGHT = 32;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

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
        touchHandler.setListener(listener);
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

    public void setCompleteTurnFraction(double fraction) {
        double radians = fraction * 2 * PI;
        setRadiansAngle(radians);
    }

    public double getRadiansAngle() {
        return angle;
    }

    public double getDegreesAngle() {
        return getRadiansAngle() * 180 / PI;
    }

    public double getCompleteTurnFraction() {
        return getRadiansAngle() / (2 * PI);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        drawer.onSizeChanged();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resolvedWidthSpec = resolveMeasureSpec(widthMeasureSpec, DP_DEFAULT_WIDTH);
        int resolvedHeightSpec = resolveMeasureSpec(heightMeasureSpec, DP_DEFAULT_HEIGHT);
        super.onMeasure(resolvedWidthSpec, resolvedHeightSpec);
    }

    private int resolveMeasureSpec(int measureSpec, int dpDefault) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return measureSpec;
        }
        int defaultSize = Utils.convertToPx(dpDefault, getResources());
        if (mode == MeasureSpec.AT_MOST) {
            defaultSize = Math.min(defaultSize, MeasureSpec.getSize(measureSpec));
        }
        return MeasureSpec.makeMeasureSpec(defaultSize, MeasureSpec.EXACTLY);
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

    public static class Listener {
        public void onRotationChanged(double radians) {
        }

        public void onScrollStateChanged(int state) {
        }
    }

}
