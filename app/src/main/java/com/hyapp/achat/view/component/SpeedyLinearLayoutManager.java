package com.hyapp.achat.view.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SpeedyLinearLayoutManager extends LinearLayoutManager {

    private int smoothScrollDuration;

    public SpeedyLinearLayoutManager(Context context, int smoothScrollDuration) {
        super(context);
        this.smoothScrollDuration = smoothScrollDuration;
    }

    public SpeedyLinearLayoutManager(Context context, int orientation, boolean reverseLayout, int smoothScrollDuration) {
        super(context, orientation, reverseLayout);
        this.smoothScrollDuration = smoothScrollDuration;
    }

    public SpeedyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int smoothScrollDuration) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.smoothScrollDuration = smoothScrollDuration;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return smoothScrollDuration;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public int getSmoothScrollDuration() {
        return smoothScrollDuration;
    }

    public void setSmoothScrollDuration(int smoothScrollDuration) {
        this.smoothScrollDuration = smoothScrollDuration;
    }
}