package com.github.shchurov.horizontalwheelview;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import java.util.Arrays;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

class Drawer {

    private static final int DEFAULT_MARKS_COUNT = 40;
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
    private static final float SHADE_RANGE = 0.7f;
    private static final float SCALE_RANGE = 0.1f;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private HorizontalWheelView view;
    private int marksCount;
    private int normalColor;
    private int activeColor;
    private boolean showActiveRange;
    private float[] gaps;
    private float[] shades;
    private float[] scales;
    private int[] colorSwitches = {-1, -1, -1};
    private int viewportHeight;
    private int normalMarkWidth;
    private int normalMarkHeight;
    private int zeroMarkWidth;
    private int zeroMarkHeight;
    private int cursorCornersRadius;
    private RectF cursorRect = new RectF();
    private int maxVisibleMarksCount;

    Drawer(HorizontalWheelView view, AttributeSet attrs) {
        this.view = view;
        readAttrs(attrs);
        initDpSizes();
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView);
        int marksCount = a.getInt(R.styleable.HorizontalWheelView_marksCount, DEFAULT_MARKS_COUNT);
        setMarksCount(marksCount);
        normalColor = a.getColor(R.styleable.HorizontalWheelView_normalColor, DEFAULT_NORMAL_COLOR);
        activeColor = a.getColor(R.styleable.HorizontalWheelView_activeColor, DEFAULT_ACTIVE_COLOR);
        showActiveRange = a.getBoolean(R.styleable.HorizontalWheelView_showActiveRange, DEFAULT_SHOW_ACTIVE_RANGE);
        a.recycle();
    }

    private void initDpSizes() {
        normalMarkWidth = convertToPx(DP_NORMAL_MARK_WIDTH);
        zeroMarkWidth = convertToPx(DP_ZERO_MARK_WIDTH);
        cursorCornersRadius = convertToPx(DP_CURSOR_CORNERS_RADIUS);
    }

    private int convertToPx(int dp) {
        return Utils.convertToPx(dp, view.getResources());
    }

    void setMarksCount(int marksCount) {
        this.marksCount = marksCount;
        maxVisibleMarksCount = (marksCount / 2) + 1;
        gaps = new float[maxVisibleMarksCount];
        shades = new float[maxVisibleMarksCount];
        scales = new float[maxVisibleMarksCount];
    }

    void setShowActiveRange(boolean show) {
        showActiveRange = show;
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

    void onDraw(Canvas canvas) {
        double step = 2 * PI / marksCount;
        double offset = (PI / 2 - view.getRadiansAngle()) % step;
        if (offset < 0) {
            offset += step;
        }
        setupGaps(step, offset);
        setupShadesAndScales(step, offset);
        int zeroIndex = calcZeroIndex(step);
        setupColorSwitches(step, offset, zeroIndex);
        drawMarks(canvas, zeroIndex);
        drawCursor(canvas);
    }

    private void setupGaps(double step, double offset) {
        gaps[0] = (float) sin(offset / 2);
        float sum = gaps[0];
        double angle = offset;
        int n = 1;
        while (angle + step <= PI) {
            gaps[n] = (float) sin(angle + step / 2);
            sum += gaps[n];
            angle += step;
            n++;
        }
        float lastGap = (float) sin((PI + angle) / 2);
        sum += lastGap;
        if (n != gaps.length) {
            gaps[gaps.length - 1] = -1;
        }
        float k = view.getWidth() / sum;
        for (int i = 0; i < gaps.length; i++) {
            if (gaps[i] != -1) {
                gaps[i] *= k;
            }
        }
    }

    private void setupShadesAndScales(double step, double offset) {
        double angle = offset;
        for (int i = 0; i < maxVisibleMarksCount; i++) {
            double sin = sin(angle);
            shades[i] = (float) (1 - SHADE_RANGE * (1 - sin));
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

    private void setupColorSwitches(double step, double offset, int zeroIndex) {
        if (!showActiveRange) {
            Arrays.fill(colorSwitches, -1);
            return;
        }
        double angle = view.getRadiansAngle();
        int afterMiddleIndex = 0;
        if (offset < PI / 2) {
            afterMiddleIndex = (int) ((PI / 2 - offset) / step) + 1;
        }
        if (angle > 3 * PI / 2) {
            colorSwitches[0] = 0;
            colorSwitches[1] = afterMiddleIndex;
            colorSwitches[2] = zeroIndex;
        } else if (angle >= 0) {
            colorSwitches[0] = Math.max(0, zeroIndex);
            colorSwitches[1] = afterMiddleIndex;
            colorSwitches[2] = -1;
        } else if (angle < -3 * PI / 2) {
            colorSwitches[0] = 0;
            colorSwitches[1] = zeroIndex;
            colorSwitches[2] = afterMiddleIndex;
        } else if (angle < 0) {
            colorSwitches[0] = afterMiddleIndex;
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
                drawNormalMark(canvas, x, scales[i], shades[i], color);
            } else {
                drawZeroMark(canvas, x, scales[i], shades[i]);
            }
        }
    }

    private void drawNormalMark(Canvas canvas, float x, float scale, float shade, int color) {
        float height = normalMarkHeight * scale;
        float top = view.getPaddingTop() + (viewportHeight - height) / 2;
        float bottom = top + height;
        paint.setStrokeWidth(normalMarkWidth);
        paint.setColor(applyShade(color, shade));
        canvas.drawLine(x, top, x, bottom, paint);
    }

    private int applyShade(int color, float shade) {
        int r = (int) (Color.red(color) * shade);
        int g = (int) (Color.green(color) * shade);
        int b = (int) (Color.blue(color) * shade);
        return Color.rgb(r, g, b);
    }

    private void drawZeroMark(Canvas canvas, float x, float scale, float shade) {
        float height = zeroMarkHeight * scale;
        float top = view.getPaddingTop() + (viewportHeight - height) / 2;
        float bottom = top + height;
        paint.setStrokeWidth(zeroMarkWidth);
        paint.setColor(applyShade(activeColor, shade));
        canvas.drawLine(x, top, x, bottom, paint);
    }

    private void drawCursor(Canvas canvas) {
        paint.setStrokeWidth(0);
        paint.setColor(activeColor);
        canvas.drawRoundRect(cursorRect, cursorCornersRadius, cursorCornersRadius, paint);
    }

}
