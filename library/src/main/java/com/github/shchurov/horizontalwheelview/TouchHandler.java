package com.github.shchurov.horizontalwheelview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import static com.github.shchurov.horizontalwheelview.HorizontalWheelView.SCROLL_STATE_DRAGGING;
import static com.github.shchurov.horizontalwheelview.HorizontalWheelView.SCROLL_STATE_IDLE;
import static com.github.shchurov.horizontalwheelview.HorizontalWheelView.SCROLL_STATE_SETTLING;

class TouchHandler extends GestureDetector.SimpleOnGestureListener {

    private static final float SCROLL_ANGLE_MULTIPLIER = 0.002f;
    private static final float FLING_ANGLE_MULTIPLIER = 0.0004f;
    private static final float FLING_DURATION_MULTIPLIER = 0.2f;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator(1.4f);

    private HorizontalWheelView view;
    private HorizontalWheelView.Listener listener;
    private GestureDetector gestureDetector;
    private ValueAnimator scrollAnimator;
    private int scrollState = SCROLL_STATE_IDLE;

    TouchHandler(HorizontalWheelView view) {
        this.view = view;
        gestureDetector = new GestureDetector(view.getContext(), this);
    }

    void setListener(HorizontalWheelView.Listener listener) {
        this.listener = listener;
    }

    boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP && scrollState != SCROLL_STATE_SETTLING) {
            updateScrollStateIfRequired(SCROLL_STATE_IDLE);
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        double newAngle = view.getRadiansAngle() + distanceX * SCROLL_ANGLE_MULTIPLIER;
        view.setRadiansAngle(newAngle);
        updateScrollStateIfRequired(SCROLL_STATE_DRAGGING);
        return true;
    }

    private void updateScrollStateIfRequired(int newState) {
        if (listener != null && scrollState != newState) {
            scrollState = newState;
            listener.onScrollStateChanged(newState);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        runFlingAnimation(velocityX);
        updateScrollStateIfRequired(SCROLL_STATE_SETTLING);
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
        scrollAnimator.addListener(animatorListener);
        scrollAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener flingAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            view.setRadiansAngle((float) animation.getAnimatedValue());
        }
    };

    private Animator.AnimatorListener animatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            updateScrollStateIfRequired(SCROLL_STATE_IDLE);
        }
    };

}
