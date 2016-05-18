package com.github.shchurov.horizontalwheelview;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class SavedState extends View.BaseSavedState {

    double angle;

    SavedState(Parcelable superState) {
        super(superState);
    }

    private SavedState(Parcel in) {
        super(in);
        angle = (Double) in.readValue(null);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeValue(angle);
    }

    @Override
    public String toString() {
        return "HorizontalWheelView.SavedState{"
                + Integer.toHexString(System.identityHashCode(this))
                + " angle=" + angle + "}";
    }

    public static final Parcelable.Creator<SavedState> CREATOR
            = new Parcelable.Creator<SavedState>() {
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

}
