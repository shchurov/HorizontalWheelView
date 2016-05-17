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
    private static final float ALPHA_RANGE = 0.7f;
    private static final float SCALE_RANGE = 0.1f;
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
    private int[] colorSwitches = new int[3];
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
        checkMaxVisibleMarks();
    }

    private void checkMaxVisibleMarks() {
        if (MAX_VISIBLE_MARKS < 3) {
            throw new IllegalArgumentException("maxVisibleMarks must be >= 3");
        }
        if (MAX_VISIBLE_MARKS % 2 == 0) {
            throw new IllegalArgumentException("maxVisibleMarks must be an odd number");
        }
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

    public void setAngle(double angle) {
        this.angle = angle % (2 * PI);
        invalidate();
        if (listener != null) {
            listener.onRotationChanged(this.angle);
        }
    }

    public double getAngle() {
        return angle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double step = PI / (MAX_VISIBLE_MARKS - 1);
        double offset = (2 * PI - angle) % step;
        setupGaps(step, offset);
        setupAlphasAndScales(step, offset);
        int zeroIndex = calcZeroIndex(step);
        setupColorSwitches(zeroIndex);
        drawMarks(canvas, zeroIndex);
        drawCursor(canvas);
    }

    private void setupGaps(double step, double offset) {
        float sum = 0;
        double a = offset + step / 2;
        for (int i = 1; i < gaps.length - 1; i++) {
            gaps[i] = (float) sin(a);
            sum += gaps[i];
            a += step;
        }
        gaps[0] = (float) sin(offset / 2);
        float lastGap = (float) sin((PI - (step - offset) / 2));
        sum += gaps[0] + lastGap;
        gaps[gaps.length - 1] = offset == 0 ? lastGap : -1;
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
            alphas[i] = (int) (255 * (1 - ALPHA_RANGE * (1 - sin)));
            scales[i] = (float) (1 - SCALE_RANGE * (1 - sin));
            angle += step;
        }
    }

    private int calcZeroIndex(double step) {
        double twoPi = 2 * PI;
        double normalizedAngle = (angle + PI / 2 + twoPi) % twoPi;
        if (normalizedAngle > PI) {
            return -1;
        }
        return (int) ((PI - normalizedAngle) / step);
    }

    private void setupColorSwitches(int zeroIndex) {
        double middle = (MAX_VISIBLE_MARKS - 1) / 2d;
        int middleCeil = (int) Math.ceil(middle);
        int middleFloor = (int) Math.floor(middle);
        if (angle > 3 * PI / 2) {
            colorSwitches[0] = 0;
            colorSwitches[1] = middleFloor;
            colorSwitches[2] = zeroIndex;
        } else if (angle >= 0) {
            colorSwitches[0] = Math.max(0, zeroIndex);
            colorSwitches[1] = middleFloor;
            colorSwitches[2] = -1;
        } else if (angle < -3 * PI / 2) {
            colorSwitches[0] = 0;
            colorSwitches[1] = zeroIndex;
            colorSwitches[2] = middleCeil;
        } else if (angle < 0) {
            colorSwitches[0] = middleCeil;
            colorSwitches[1] = zeroIndex;
            colorSwitches[2] = -1;
        }
    }

    private void drawMarks(Canvas canvas, int zeroIndex) {
        float x = 0;
        int color = NORMAL_COLOR;
        int colorPointer = 0;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] == -1) {
                break;
            }
            x += gaps[i];
            while (colorPointer < 3 && i == colorSwitches[colorPointer]) {
                color = color == NORMAL_COLOR ? ACTIVE_COLOR : NORMAL_COLOR;
                colorPointer++;
            }
            if (i != zeroIndex) {
                drawNormalMark(canvas, x, scales[i], alphas[i], color);
            } else {
                drawZeroMark(canvas, x, scales[i], alphas[i]);
            }
        }
    }

    private void drawNormalMark(Canvas canvas, float x, float scale, int alpha, int color) {
        float height = NORMAL_MARK_HEIGHT * scale;
        float left = x - NORMAL_MARK_WIDTH / 2;
        float top = (getHeight() - height) / 2;
        float right = left + NORMAL_MARK_WIDTH;
        float bottom = top + height;
        paint.setColor(color);
        paint.setAlpha(alpha);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawZeroMark(Canvas canvas, float x, float scale, int alpha) {
        float height = ZERO_MARK_HEIGHT * scale;
        float left = x - ZERO_MARK_WIDTH / 2;
        float top = (getHeight() - height) / 2;
        float right = left + ZERO_MARK_WIDTH;
        float bottom = top + height;
        paint.setColor(ACTIVE_COLOR);
        paint.setAlpha(alpha);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawCursor(Canvas canvas) {
        paint.setColor(ACTIVE_COLOR);
        canvas.drawRoundRect(cursorRect, CURSOR_CORNERS_RADIUS, CURSOR_CORNERS_RADIUS, paint);
    }

    public interface Listener {
        void onRotationChanged(double angle);
    }

}
