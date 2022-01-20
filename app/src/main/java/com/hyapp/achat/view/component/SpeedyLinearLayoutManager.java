package com.hyapp.achat.view.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SpeedyLinearLayoutManager extends LinearLayoutManager {

    private boolean isSmoothScrollSpeedDefault;
    private final int smoothScrollDuration;
    private OnScrollStop onScrollStop;

    public interface OnScrollStop {
        void onStop(SpeedyLinearLayoutManager layoutManager);
    }

    public SpeedyLinearLayoutManager(Context context, int smoothScrollDuration) {
        super(context);
        this.smoothScrollDuration = smoothScrollDuration;
        isSmoothScrollSpeedDefault = false;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            protected int calculateTimeForScrolling(int dx) {
                if (!isSmoothScrollSpeedDefault) {
                    return smoothScrollDuration;
                }
                return super.calculateTimeForScrolling(dx);
            }

            @Override
            protected void onStop() {
                super.onStop();
                if (onScrollStop != null) {
                    onScrollStop.onStop(SpeedyLinearLayoutManager.this);
                }
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public void setSmoothScrollSpeedDefault(boolean smoothScrollSpeedDefault) {
        isSmoothScrollSpeedDefault = smoothScrollSpeedDefault;
    }

    public void setOnScrollStop(OnScrollStop onScrollStop) {
        this.onScrollStop = onScrollStop;
    }
}