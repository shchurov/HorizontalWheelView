package com.github.shchurov.horizontalwheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.PI;

public class HorizontalWheelView extends View {

    private static final int DP_DEFAULT_WIDTH = 200;
    private static final int DP_DEFAULT_HEIGHT = 32;
    private static final boolean DEFAULT_END_LOCK = false;
    private static final boolean DEFAULT_ONLY_POSITIVE_VALUES = false;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private Drawer drawer;
    private TouchHandler touchHandler;
    private double angle;
    private boolean onlyPositiveValues;
    private boolean endLock;
    private Listener listener;

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttrs(attrs);
        drawer = new Drawer(this, attrs);
        touchHandler = new TouchHandler(this, attrs);
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView);
        endLock = a.getBoolean(R.styleable.HorizontalWheelView_endLock, DEFAULT_END_LOCK);
        onlyPositiveValues = a.getBoolean(R.styleable.HorizontalWheelView_onlyPositiveValues,
                DEFAULT_ONLY_POSITIVE_VALUES);
        a.recycle();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        touchHandler.setListener(listener);
    }

    public void setRadiansAngle(double radians) {
        if (!checkEndLock(radians)) {
            angle = radians % (2 * PI);
        }
        if (onlyPositiveValues && angle < 0) {
            angle += 2 * PI;
        }
        invalidate();
        if (listener != null) {
            listener.onRotationChanged(this.angle);
        }
    }

    private boolean checkEndLock(double radians) {
        if (!endLock) {
            return false;
        }
        boolean hit = false;
        if (radians >= 2 * PI) {
            angle = Math.nextAfter(2 * PI, Double.NEGATIVE_INFINITY);
            hit = true;
        } else if (onlyPositiveValues && radians < 0) {
            angle = 0;
            hit = true;
        } else if (radians <= -2 * PI) {
            angle = Math.nextAfter(-2 * PI, Double.POSITIVE_INFINITY);
            hit = true;
        }
        if (hit) {
            touchHandler.cancelFling();
        }
        return hit;
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

    public void setOnlyPositiveValues(boolean onlyPositiveValues) {
        this.onlyPositiveValues = onlyPositiveValues;
    }

    public void setEndLock(boolean lock) {
        this.endLock = lock;
    }

    public void setMarksCount(int marksCount) {
        drawer.setMarksCount(marksCount);
        invalidate();
    }

    public void setShowActiveRange(boolean show) {
        drawer.setShowActiveRange(show);
        invalidate();
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

    int getMarksCount() {
        return drawer.getMarksCount();
    }

    public static class Listener {
        public void onRotationChanged(double radians) {
        }

        public void onScrollStateChanged(int state) {
        }
    }

}
