package com.github.shchurov.horizontalwheelview;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

class Drawer {

    private static final int DEFAULT_MAX_VISIBLE_MARKS = 21;
    private static final int DEFAULT_NORMAL_COLOR = 0xffffffff;
    private static final int DEFAULT_ACTIVE_COLOR = 0xff54acf0;
    private static final boolean DEFAULT_SHOW_ACTIVE_RANGE = true;
    private static final int DP_CURSOR_CORNERS_RADIUS = 1;
    private static final int DP_NORMAL_MARK_WIDTH = 1;
    private static final int DP_ZERO_MARK_WIDTH = 2;
    private static final int DP_CURSOR_WIDTH = 3;
    private static final float NORMAL_MARK_RELATIVE_HEIGHT = 0.6f;
    private static final float ZERO_MARK_RELATIVE_HEIGHT = 0.8f;
    private static final float CURSOR_RELATIVE_HEIGHT = 1f;
    private static final float ALPHA_RANGE = 0.7f;
    private static final float SCALE_RANGE = 0.1f;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private HorizontalWheelView view;
    private int maxVisibleMarks;
    private int normalColor;
    private int activeColor;
    private boolean showActiveRange;
    private float[] gaps;
    private int[] alphas;
    private float[] scales;
    private int[] colorSwitches = {-1, -1, -1};
    private int viewportHeight;
    private int normalMarkWidth;
    private int normalMarkHeight;
    private int zeroMarkWidth;
    private int zeroMarkHeight;
    private int cursorCornersRadius;
    private RectF cursorRect = new RectF();

    Drawer(HorizontalWheelView view, AttributeSet attrs) {
        this.view = view;
        readAttrs(attrs);
        gaps = new float[maxVisibleMarks];
        alphas = new int[maxVisibleMarks];
        scales = new float[maxVisibleMarks];
        initDpSizes();
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView);
        maxVisibleMarks = a.getInt(R.styleable.HorizontalWheelView_maxVisibleMarks, DEFAULT_MAX_VISIBLE_MARKS);
        normalColor = a.getColor(R.styleable.HorizontalWheelView_normalColor, DEFAULT_NORMAL_COLOR);
        activeColor = a.getColor(R.styleable.HorizontalWheelView_activeColor, DEFAULT_ACTIVE_COLOR);
        showActiveRange = a.getBoolean(R.styleable.HorizontalWheelView_showActiveRange, DEFAULT_SHOW_ACTIVE_RANGE);
        a.recycle();
        validateMaxVisibleMarks(maxVisibleMarks);
    }

    private void validateMaxVisibleMarks(int maxVisibleMarks) {
        if (maxVisibleMarks < 3) {
            throw new IllegalArgumentException("maxVisibleMarks must be >= 3");
        }
        if (maxVisibleMarks % 2 == 0) {
            throw new IllegalArgumentException("maxVisibleMarks must be an odd number");
        }
    }

    private void initDpSizes() {
        normalMarkWidth = convertToPx(DP_NORMAL_MARK_WIDTH);
        zeroMarkWidth = convertToPx(DP_ZERO_MARK_WIDTH);
        cursorCornersRadius = convertToPx(DP_CURSOR_CORNERS_RADIUS);
    }

    private int convertToPx(int dp) {
        DisplayMetrics dm = view.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    void onDraw(Canvas canvas) {
        double step = PI / (maxVisibleMarks - 1);
        double offset = (2 * PI - view.getRadiansAngle()) % step;
        setupGaps(step, offset);
        setupAlphasAndScales(step, offset);
        int zeroIndex = calcZeroIndex(step);
        setupColorSwitches(zeroIndex);
        drawMarks(canvas, zeroIndex);
        drawCursor(canvas);
    }

    void onSizeChanged() {
        viewportHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();
        normalMarkHeight = (int) (viewportHeight * NORMAL_MARK_RELATIVE_HEIGHT);
        zeroMarkHeight = (int) (viewportHeight * ZERO_MARK_RELATIVE_HEIGHT);
        setupCursorRect();
    }

    private void setupCursorRect() {
        int cursorHeight = (int) (viewportHeight * CURSOR_RELATIVE_HEIGHT);
        cursorRect.top = view.getPaddingTop() + (viewportHeight - cursorHeight) / 2;
        cursorRect.bottom = cursorRect.top + cursorHeight;
        int cursorWidth = convertToPx(DP_CURSOR_WIDTH);
        cursorRect.left = (view.getWidth() - cursorWidth) / 2;
        cursorRect.right = cursorRect.left + cursorWidth;
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
        float k = view.getWidth() / sum;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] != -1) {
                gaps[i] *= k;
            }
        }
    }

    private void setupAlphasAndScales(double step, double offset) {
        double angle = offset;
        for (int i = 0; i < maxVisibleMarks; i++) {
            double sin = sin(angle);
            alphas[i] = (int) (255 * (1 - ALPHA_RANGE * (1 - sin)));
            scales[i] = (float) (1 - SCALE_RANGE * (1 - sin));
            angle += step;
        }
    }

    private int calcZeroIndex(double step) {
        double twoPi = 2 * PI;
        double normalizedAngle = (view.getRadiansAngle() + PI / 2 + twoPi) % twoPi;
        if (normalizedAngle > PI) {
            return -1;
        }
        return (int) ((PI - normalizedAngle) / step);
    }

    private void setupColorSwitches(int zeroIndex) {
        if (!showActiveRange) {
            return;
        }
        double middle = (maxVisibleMarks - 1) / 2d;
        int middleCeil = (int) Math.ceil(middle);
        int middleFloor = (int) Math.floor(middle);
        double angle = view.getRadiansAngle();
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
        float x = view.getPaddingLeft();
        int color = normalColor;
        int colorPointer = 0;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] == -1) {
                break;
            }
            x += gaps[i];
            while (colorPointer < 3 && i == colorSwitches[colorPointer]) {
                color = color == normalColor ? activeColor : normalColor;
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
        float height = normalMarkHeight * scale;
        float left = x - normalMarkWidth / 2;
        float top = view.getPaddingTop() + (viewportHeight - height) / 2;
        float right = left + normalMarkWidth;
        float bottom = top + height;
        paint.setColor(color);
        paint.setAlpha(alpha);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawZeroMark(Canvas canvas, float x, float scale, int alpha) {
        float height = zeroMarkHeight * scale;
        float left = x - zeroMarkWidth / 2;
        float top = view.getPaddingTop() + (viewportHeight - height) / 2;
        float right = left + zeroMarkWidth;
        float bottom = top + height;
        paint.setColor(activeColor);
        paint.setAlpha(alpha);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawCursor(Canvas canvas) {
        paint.setColor(activeColor);
        canvas.drawRoundRect(cursorRect, cursorCornersRadius, cursorCornersRadius, paint);
    }

}
