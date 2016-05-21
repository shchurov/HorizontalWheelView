package com.github.shchurov.horizontalwheelview;

import android.animation.ValueAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

class TouchHandler extends GestureDetector.SimpleOnGestureListener {

    private static final float SCROLL_ANGLE_MULTIPLIER = 0.002f;
    private static final float FLING_ANGLE_MULTIPLIER = 0.0003f;
    private static final float FLING_DURATION_MULTIPLIER = 0.2f;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator(1.4f);

    private HorizontalWheelView view;
    private GestureDetector gestureDetector;
    private ValueAnimator scrollAnimator;

    public TouchHandler(HorizontalWheelView view) {
        this.view = view;
        gestureDetector = new GestureDetector(view.getContext(), this);
    }

    boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (scrollAnimator != null) {
            scrollAnimator.cancel();
            scrollAnimator = null;
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        double newAngle = view.getRadiansAngle() + distanceX * SCROLL_ANGLE_MULTIPLIER;
        view.setRadiansAngle(newAngle);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        runFlingAnimation(velocityX);
        return true;
    }

    private void runFlingAnimation(float velocity) {
        int duration = (int) Math.abs(velocity * FLING_DURATION_MULTIPLIER);
        float startAngle = (float) view.getRadiansAngle();
        float endAngle = startAngle - velocity * FLING_ANGLE_MULTIPLIER;
        scrollAnimator = ValueAnimator.ofFloat(startAngle, endAngle)
                .setDuration(duration);
        scrollAnimator.setInterpolator(INTERPOLATOR);
        scrollAnimator.addUpdateListener(flingAnimatorListener);
        scrollAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener flingAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            view.setRadiansAngle((float) animation.getAnimatedValue());
        }
    };

}
