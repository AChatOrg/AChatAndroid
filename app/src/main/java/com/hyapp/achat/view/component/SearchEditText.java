package com.hyapp.achat.view.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.hyapp.achat.R;
import com.hyapp.achat.view.utils.UiUtils;


public class SearchEditText extends AppCompatEditText implements View.OnFocusChangeListener {

    private int grey35Color;
    private int orgWidth;
    private int dp8, dp6;

    public SearchEditText(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SearchEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        orgWidth = -1;
        dp6 = UiUtils.dp2px(context, 6);
        dp8 = UiUtils.dp2px(context, 8);
        grey35Color = ContextCompat.getColor(context, R.color.grey_35);

        setMaxLines(1);
        setSingleLine(true);
        setLines(1);
        setTypeface(ResourcesCompat.getFont(context, R.font.iran_sans_regular));
        setOnFocusChangeListener(this);
        setBackgroundResource(R.drawable.toolbar_action_bg);
        setClickable(true);
        setFocusable(true);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp6, 0, dp6, 0);
        setCompoundDrawablePadding(dp8);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.action_search, 0, 0, 0);
        } else {
            setCompoundDrawables(ContextCompat.getDrawable(context, R.drawable.action_search), null, null, null);
        }
        setCompoundDrawablesTint(Color.BLACK);
    }

    private void setCompoundDrawablesTint(int color) {
        for (Drawable drawable : getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (orgWidth == -1) {
            orgWidth = w;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getLayoutParams();
        if (hasFocus) {
            changeWidthAnimated(orgWidth, ((View) getParent()).getWidth() - (params.leftMargin + params.rightMargin), 100);
            setHint(R.string.search);
            setPadding(dp8, 0, dp8, 0);
            setCompoundDrawablesTint(grey35Color);
            setBackgroundResource(R.drawable.toolbar_action_bg_light);
        } else {
            changeWidthAnimated(((View) getParent()).getWidth() - (params.leftMargin + params.rightMargin), orgWidth, 100);
            setHint("");
            setText("");
            setPadding(dp6, 0, dp6, 0);
            setCompoundDrawablesTint(Color.BLACK);
            setBackgroundResource(R.drawable.toolbar_action_bg);
        }
    }

    private void changeWidthAnimated(int fromWidth, int toWidth, long duration) {
        ValueAnimator widthAnimator = ValueAnimator.ofInt(fromWidth, toWidth);
        widthAnimator.setDuration(duration);
        widthAnimator.setInterpolator(new DecelerateInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            getLayoutParams().width = (int) animation.getAnimatedValue();
            requestLayout();
        });
        widthAnimator.start();
    }
}
