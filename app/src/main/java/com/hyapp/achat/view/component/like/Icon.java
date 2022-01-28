package com.hyapp.achat.view.component.like;


import androidx.annotation.DrawableRes;

/**
 * Created by Joel on 23/12/2015.
 */
public class Icon {
    private int onIconResourceId;
    private int offIconResourceId;

    public Icon(@DrawableRes int onIconResourceId, @DrawableRes int offIconResourceId) {
        this.onIconResourceId = onIconResourceId;
        this.offIconResourceId = offIconResourceId;
    }

    public int getOffIconResourceId() {
        return offIconResourceId;
    }

    public void setOffIconResourceId(@DrawableRes int offIconResourceId) {
        this.offIconResourceId = offIconResourceId;
    }

    public int getOnIconResourceId() {
        return onIconResourceId;
    }

    public void setOnIconResourceId(@DrawableRes int onIconResourceId) {
        this.onIconResourceId = onIconResourceId;
    }
}
