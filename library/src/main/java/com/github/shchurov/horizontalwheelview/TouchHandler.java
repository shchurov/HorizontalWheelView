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
import static java.lang.Math.PI;

class TouchHandler extends GestureDetector.SimpleOnGestureListener {

    private static final float SCROLL_ANGLE_MULTIPLIER = 0.002f;
    private static final float FLING_ANGLE_MULTIPLIER = 0.0002f;
    private static final int SETTLING_DURATION_MULTIPLIER = 1000;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator(2.5f);

    private HorizontalWheelView view;
    private HorizontalWheelView.Listener listener;
    private GestureDetector gestureDetector;
    private ValueAnimator settlingAnimator;
    private boolean snapToMarks;
    private int scrollState = SCROLL_STATE_IDLE;

    TouchHandler(HorizontalWheelView view) {
        this.view = view;
        gestureDetector = new GestureDetector(view.getContext(), this);
    }

    void setListener(HorizontalWheelView.Listener listener) {
        this.listener = listener;
    }

    void setSnapToMarks(boolean snapToMarks) {
        this.snapToMarks = snapToMarks;
    }

    boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        if (scrollState != SCROLL_STATE_SETTLING
                && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            if (snapToMarks) {
                playSettlingAnimation(findNearestMarkAngle(view.getRadiansAngle()));
            } else {
                updateScrollStateIfRequired(SCROLL_STATE_IDLE);
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        cancelFling();
        return true;
    }

    void cancelFling() {
        if (scrollState == SCROLL_STATE_SETTLING) {
            settlingAnimator.cancel();
        }
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
        double endAngle = view.getRadiansAngle() - velocityX * FLING_ANGLE_MULTIPLIER;
        if (snapToMarks) {
            endAngle = (float) findNearestMarkAngle(endAngle);
        }
        playSettlingAnimation(endAngle);
        return true;
    }

    private double findNearestMarkAngle(double angle) {
        double step = 2 * PI / view.getMarksCount();
        return Math.round(angle / step) * step;
    }

    private void playSettlingAnimation(double endAngle) {
        updateScrollStateIfRequired(SCROLL_STATE_SETTLING);
        double startAngle = view.getRadiansAngle();
        int duration = (int) (Math.abs(startAngle - endAngle) * SETTLING_DURATION_MULTIPLIER);
        settlingAnimator = ValueAnimator.ofFloat((float) startAngle, (float) endAngle)
                .setDuration(duration);
        settlingAnimator.setInterpolator(INTERPOLATOR);
        settlingAnimator.addUpdateListener(flingAnimatorListener);
        settlingAnimator.addListener(animatorListener);
        settlingAnimator.start();
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
