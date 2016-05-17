package com.github.shchurov.horizontalwheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class HorizontalWheelView extends View {

    private static final int DEFAULT_MAX_VISIBLE_MARKS = 21;
    private static final int DEFAULT_NORMAL_COLOR = 0xffffffff;
    private static final int DEFAULT_ACTIVE_COLOR = 0xff54acf0;
    private static final int DP_CURSOR_CORNERS_RADIUS = 1;
    private static final int DP_NORMAL_MARK_WIDTH = 1;
    private static final int DP_ZERO_MARK_WIDTH = 2;
    private static final int DP_CURSOR_WIDTH = 3;
    private static final float NORMAL_MARK_RELATIVE_HEIGHT = 0.38f;
    private static final float ZERO_MARK_RELATIVE_HEIGHT = 0.5f;
    private static final float CURSOR_RELATIVE_HEIGHT = 0.69f;
    private static final float TOUCH_ANGLE_MULTIPLIER = 0.002f;
    private int MAX_VISIBLE_MARKS;
    private int NORMAL_COLOR;
    private int ACTIVE_COLOR;
    private int NORMAL_MARK_WIDTH;
    private int NORMAL_MARK_HEIGHT;
    private int ZERO_MARK_WIDTH;
    private int ZERO_MARK_HEIGHT;
    private int CURSOR_CORNERS_RADIUS;

    private Paint paint = new Paint();
    private float[] gaps;
    private int[] alphas;
    private float[] scales;
    private RectF cursorRect = new RectF();
    private double angle;
    private float prevTouchX;
    private Listener listener;

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttrs(attrs);
        init();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void init() {
        gaps = new float[MAX_VISIBLE_MARKS];
        alphas = new int[MAX_VISIBLE_MARKS];
        scales = new float[MAX_VISIBLE_MARKS];
        initSizes();
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView);
        MAX_VISIBLE_MARKS = a.getInt(R.styleable.HorizontalWheelView_maxVisibleMarks, DEFAULT_MAX_VISIBLE_MARKS);
        NORMAL_COLOR = a.getColor(R.styleable.HorizontalWheelView_normalColor, DEFAULT_NORMAL_COLOR);
        ACTIVE_COLOR = a.getColor(R.styleable.HorizontalWheelView_activeColor, DEFAULT_ACTIVE_COLOR);
        a.recycle();
    }

    private void initSizes() {
        post(new Runnable() {
            @Override
            public void run() {
                NORMAL_MARK_WIDTH = convertToPx(DP_NORMAL_MARK_WIDTH);
                NORMAL_MARK_HEIGHT = (int) (getHeight() * NORMAL_MARK_RELATIVE_HEIGHT);
                ZERO_MARK_WIDTH = convertToPx(DP_ZERO_MARK_WIDTH);
                ZERO_MARK_HEIGHT = (int) (getHeight() * ZERO_MARK_RELATIVE_HEIGHT);
                CURSOR_CORNERS_RADIUS = convertToPx(DP_CURSOR_CORNERS_RADIUS);
                int cursorWidth = convertToPx(DP_CURSOR_WIDTH);
                int cursorHeight = (int) (getHeight() * CURSOR_RELATIVE_HEIGHT);
                cursorRect.left = (getWidth() - cursorWidth) / 2;
                cursorRect.right = cursorRect.left + cursorWidth;
                cursorRect.top = (getHeight() - cursorHeight) / 2;
                cursorRect.bottom = cursorRect.top + cursorHeight;
            }
        });
    }

    private int convertToPx(int dp) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevTouchX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                setAngle(angle + (prevTouchX - x) * TOUCH_ANGLE_MULTIPLIER);
                prevTouchX = x;
                break;
        }
        return true;
    }

    private void setAngle(double angle) {
        this.angle = simplifyAngle(angle);
        invalidate();
        if (listener != null) {
            listener.onRotationChanged(this.angle);
        }
    }

    private double simplifyAngle(double angle) {
        if (angle < -PI) {
            return angle + 2 * PI;
        } else if (angle > PI) {
            return angle - 2 * PI;
        } else {
            return angle;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double angleStep = PI / (MAX_VISIBLE_MARKS - 1);
        double offset = (2 * PI - angle) % angleStep;
        setupGaps(angleStep, offset);
        setupAlphasAndScales(angleStep, offset);
        int zeroIndex = calcZeroIndex(angleStep);
        drawNotches(canvas, zeroIndex);
        drawCursor(canvas);
    }

    private void setupGaps(double step, double offset) {
        float sum = 0;
        double angle = offset + step / 2;
        for (int i = 1; i < gaps.length - 1; i++) {
            gaps[i] = (float) sin(angle);
            sum += gaps[i];
            angle += step;
        }
        gaps[0] = (float) sin(offset / 2);
        gaps[gaps.length - 1] = (float) sin((PI - (step - offset) / 2));
        sum += gaps[0] + gaps[gaps.length - 1];
        if (offset != 0) {
            gaps[gaps.length - 1] = -1;
        }
        float k = getWidth() / sum;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] != -1) {
                gaps[i] *= k;
            }
        }
    }

    private void setupAlphasAndScales(double step, double offset) {
        double angle = offset;
        for (int i = 0; i < MAX_VISIBLE_MARKS; i++) {
            double sin = sin(angle);
            alphas[i] = (int) (255 * (0.3f + 0.7f * sin));
            scales[i] = (float) (0.9f + 0.1f * sin);
            angle += step;
        }
    }

    private int calcZeroIndex(double step) {
        double halfPi = PI / 2;
        if (angle < -halfPi) {
            return MAX_VISIBLE_MARKS + 1;
        }
        if (angle > halfPi) {
            return -1;
        }
        double twoPi = PI * 2;
        double normalizedAngle = (angle + twoPi) % twoPi;
        normalizedAngle = (halfPi - normalizedAngle + twoPi) % twoPi;
        return (int) (normalizedAngle / step);
    }

    private void drawNotches(Canvas canvas, int zeroIndex) {
        float middle = getWidth() / 2;
        float x = 0;
        float halfWidth = NORMAL_MARK_WIDTH / 2;
        boolean zeroPassed = zeroIndex == -1;
        boolean middlePassed = false;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] == -1) {
                break;
            }
            x += gaps[i];
            if (!middlePassed && x >= middle) {
                middlePassed = true;
            }
            float height = NORMAL_MARK_HEIGHT * scales[i];
            float top = (getHeight() - height) / 2;
            float bottom = top + height;
            if (i != zeroIndex) {
                if ((zeroPassed && !middlePassed) || (middlePassed && !zeroPassed)) {
                    paint.setColor(ACTIVE_COLOR);
                } else {
                    paint.setColor(NORMAL_COLOR);
                }
                paint.setAlpha(alphas[i]);
            } else {
                drawZeroNotch(canvas, x, alphas[i]);
                zeroPassed = true;
                continue;
            }
            float left = x - halfWidth;
            float right = left + NORMAL_MARK_WIDTH;
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    private void drawZeroNotch(Canvas canvas, float x, int alpha) {
        paint.setColor(ACTIVE_COLOR);
        paint.setAlpha(alpha);
        float left = x - ZERO_MARK_WIDTH / 2;
        float right = left + ZERO_MARK_WIDTH;
        float height = ZERO_MARK_HEIGHT;
        float top = (getHeight() - height) / 2;
        float bottom = top + height;
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawCursor(Canvas canvas) {
        paint.setColor(ACTIVE_COLOR);
        canvas.drawRoundRect(cursorRect, CURSOR_CORNERS_RADIUS, CURSOR_CORNERS_RADIUS, paint);
    }

    public void rotate(double angle) {
        setAngle(this.angle + angle);
    }

    public void reset() {
        setAngle(0);
    }

    public double getAngle() {
        return angle;
    }

    public interface Listener {
        void onRotationChanged(double angle);
    }

}
